package com.group.MatchService.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import com.group.MatchService.external.client.ChatService;
import com.group.MatchService.external.client.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.group.MatchService.constants.MatchConstants;
import com.group.MatchService.model.Match;
import com.group.MatchService.model.MatchHistory;
import com.group.MatchService.model.MatchCreateRequest;
import com.group.MatchService.service.RedisService;
import com.group.MatchService.repository.MatchRepository;
import com.group.MatchService.repository.MatchHistoryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;



@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisService redisService;

    public List<Match> allMatches() {
        return matchRepository.findAll();
    }

    public Optional<List<Match>> allSuccessMatches() {
        return matchRepository.findByStatus(MatchConstants.STATUS.MATCHED.ordinal());
    }

    public Match create(Map<String, Object> matchMap) {
        Match match = new Match(matchMap);
        matchRepository.insert(match);

        List<String> userIds = TypeUtil.jsonStringArray(matchMap.get("userIds").toString());
        for (String userId: userIds) {
            MatchCreateRequest request = new MatchCreateRequest();
            request.setUserId(userId);
            request.setMatch(match);
            userService.createMatch(request);
        }
        return match;
    }

    public Match matchByUserIds(String id1, String id2) {
        List<ObjectId> userIds = Arrays.asList(TypeUtil.objectIdConverter(id1), TypeUtil.objectIdConverter(id2));
        return matchByUserIds(userIds).orElseThrow();
    }

    private Optional<Match> matchByUserIds(List<ObjectId> userIds) {
        Optional<Match> match = matchRepository.findByUserIds(userIds);
        if (match.isEmpty()) {
            Collections.reverse(userIds);
            match = matchRepository.findByUserIds(userIds);
        }
        return match;
    }


    public Match updateMatchHistory(Map<String, Object> matchMap) {
        // 1. find match 
        // convert to object list
        String senderId = matchMap.get("senderId").toString();
        String receiverId = matchMap.get("receiverId").toString();
        String behavior = matchMap.get("behavior").toString();

        List<String> ids = Arrays.asList(senderId, receiverId);

        List<ObjectId> userIds = ids.stream()
            .map(ObjectId::new) // Fix the method reference here
            .collect(Collectors.toList());

        Match match = matchByUserIds(userIds)
            .orElseGet(() -> {
                Match newMatch = new Match(userIds);
                matchRepository.insert(newMatch);
                return newMatch;
            });

        // 2. update match history
        MatchHistory matchHistory = new MatchHistory(senderId, receiverId, behavior);
        updateMatchHistory(match, matchHistory);
        updateMatchStatus(match);
        matchRepository.save(match);
        return match;
    }

    private void updateMatchHistory(Match match, MatchHistory matchHistory) {
        matchHistoryRepository.insert(matchHistory);
        List<MatchHistory> histories = match.getMatchHistories() == null ? new ArrayList<>() : new ArrayList<>(match.getMatchHistories());
        histories.add(matchHistory);
        match.setMatchHistories(histories);
        matchRepository.save(match);
    }


    private void updateMatchStatus(Match match) {
        long acceptCount = match.getMatchHistories().stream()
            .filter(matchHistory -> matchHistory.getBehavior() == MatchConstants.BEHAVIOR.ACCEPT.ordinal())
            .count();

        long rejectCount = match.getMatchHistories().stream()
            .filter(matchHistory -> matchHistory.getBehavior() == MatchConstants.BEHAVIOR.REJECT.ordinal())
            .count();

        if (acceptCount == 2) {
            match.setStatus(MatchConstants.STATUS.MATCHED.ordinal());
            List<ObjectId> UserIdsObjectIdList = match.getUserIds();
            List<String> UserIdsStringList = UserIdsObjectIdList.stream()
                    .map(ObjectId::toString).toList();

            chatService.createConversation(UserIdsStringList);
            redisService.publishMatchUpdate(UserIdsStringList.get(0), UserIdsStringList.get(1));
        } else if (rejectCount == 2) {
            match.setStatus(MatchConstants.STATUS.FAILED.ordinal());
        } else {
            match.setStatus(MatchConstants.STATUS.AWAIT.ordinal());
        }
    }


    public List<String> getMatchedUsersId(ObjectId userId) {

        String userIdString = userId.toString();
        List<String> matchedUserIds = redisService.getMatchedUsersIdFromCache(userIdString);

        // Check if the cache returned any data
        if (matchedUserIds == null || matchedUserIds.isEmpty()) {
            // If not, retrieve from database
            List<Match> matches = matchRepository.findByStatusAndUserIdsContaining(1, userId);

            // Extract the userIds and remove the provided userId
            matchedUserIds = matches.stream()
                    .flatMap(match -> match.getUserIds().stream())
                    .distinct()
                    .filter(ids -> !ids.equals(userId))
                    .map(ObjectId::toString)
                    .collect(Collectors.toList());

            // Cache the retrieved data
            redisService.cacheMatchedUsersIds(userIdString, matchedUserIds);
        }

        return matchedUserIds;
    }
}

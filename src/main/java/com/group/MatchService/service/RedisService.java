package com.group.MatchService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.List;


@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void cacheMatchedUsersIds(String userId, List<String> matchedUserIds) {
        String cacheKey = "match:matchedUsers:" + userId;
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();

        // Optional: Remove existing cache for this user to refresh the data
        stringRedisTemplate.delete(cacheKey);

        // Add all matched user IDs to the list in Redis
        for (String matchedUserId : matchedUserIds) {
            listOps.rightPush(cacheKey, matchedUserId);
        }

        stringRedisTemplate.expire(cacheKey, 1, TimeUnit.DAYS);
    }

    public List<String> getMatchedUsersIdFromCache(String userId) {
        String cacheKey = "matchedUsers:" + userId;
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
        Long size = listOps.size(cacheKey);

        return listOps.range(cacheKey, 0, size - 1);
    }

    public void publishMatchUpdate(String userId, String matchedUserId) {
        String userChannel_1 = "user_channel:" + userId;
        String message_1 = "New match with userId: " + matchedUserId;
        stringRedisTemplate.convertAndSend(userChannel_1, message_1);

        String userChannel_2 = "user_channel:" + matchedUserId;
        String message_2 = "New match with userId: " + userId;
        stringRedisTemplate.convertAndSend(userChannel_2, message_2);

    }
}

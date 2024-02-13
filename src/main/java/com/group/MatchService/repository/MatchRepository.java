package com.group.MatchService.repository;

import com.group.MatchService.model.Match;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository  extends MongoRepository<Match, ObjectId> {
    Optional<List<Match>> findByStatus(Integer status);
    Optional<Match> findByUserIds(List<ObjectId> userIds);
    List<Match> findByUserIdsContains(ObjectId userId);
    List<Match> findByStatusAndUserIdsContaining(Integer status, ObjectId userId);
}

package com.group.MatchService.repository;
import com.group.MatchService.model.MatchHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MatchHistoryRepository extends MongoRepository<MatchHistory, ObjectId> {
}

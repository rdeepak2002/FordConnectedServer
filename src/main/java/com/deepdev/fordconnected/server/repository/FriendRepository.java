package com.deepdev.fordconnected.server.repository;

import java.util.List;
import java.util.Optional;

import com.deepdev.fordconnected.server.model.Friend;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FriendRepository extends MongoRepository<Friend, String> {
  @Query("{'id' : ?0}")
  Optional<Friend> findById(String id);

  @Query("{pairStr: {$all: [?0, ?1]}}")
  Optional<Friend> findByUserIds(String userId1, String userId2);

  @Query("{$and: [{pairStr: {$all: [?0]}}, {status: 'ACCEPTED'}]}")
  List<Friend> findAllByUserId(String userId);
}

package com.deepdev.fordconnected.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

import com.deepdev.fordconnected.server.model.User;

public interface UserRepository extends MongoRepository<User, String> {
  @Query("{'id' : ?0}")
  Optional<User> findById(String id);

  @Query("{'username' : ?0}")
  Optional<User> findByUsername(String username);

  @Query("{'fordProfileId' : ?0}")
  Optional<User> findByFordProfileId(String fordProfileId);
}

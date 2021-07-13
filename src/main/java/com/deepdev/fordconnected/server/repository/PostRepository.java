package com.deepdev.fordconnected.server.repository;

import java.util.List;
import java.util.Optional;

import com.deepdev.fordconnected.server.model.Post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<Post, String> {
  @Query("{'id' : ?0}")
  Optional<Post> findById(String id);

  @Query("{'userId' : ?0}")
  List<Post> findAllByUserId(String userId);

  @Query("{'fordProfileId' : ?0}")
  Optional<Post> findByFordProfileId(String fordProfileId);
}
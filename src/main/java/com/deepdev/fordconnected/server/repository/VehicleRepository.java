package com.deepdev.fordconnected.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

import com.deepdev.fordconnected.server.model.Vehicle;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
  @Query("{'id' : ?0}")
  Optional<Vehicle> findById(String id);

  @Query("{'userId' : ?0}")
  Iterable<Vehicle> findByUserId(String userId);

  @Query("{'fordProfileId' : ?0}")
  Optional<Vehicle> findByFordProfileId(String fordProfileId);
}

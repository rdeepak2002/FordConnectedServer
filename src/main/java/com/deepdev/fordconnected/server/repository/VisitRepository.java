package com.deepdev.fordconnected.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

import com.deepdev.fordconnected.server.model.Visit;

public interface VisitRepository extends MongoRepository<Visit, String> {
    @Query("{ $or: [ {'ip' : ?0, 'browserId' : ?1, 'isMobile' : ?2}, {'deviceId' : ?3} ] }")
    Optional<Visit> findByIpAndBrowserIdAndIsMobileOrDeviceId(String ip, String browserId, Boolean isMobile,
            String deviceId);
}

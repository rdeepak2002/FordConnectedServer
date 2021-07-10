package com.deepdev.fordconnected.server.repository;

import java.util.Optional;

import com.deepdev.fordconnected.server.model.AccessToken;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends CrudRepository<AccessToken, String> {
  Optional<AccessToken> findByAccessToken(String accessToken);
}
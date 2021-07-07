package com.deepdev.fordconnected.server.resolver;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.util.Optional;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.deepdev.fordconnected.server.exception.CustomException;
import com.deepdev.fordconnected.server.model.AccessToken;
import com.deepdev.fordconnected.server.model.User;
import com.deepdev.fordconnected.server.model.UserWithToken;
import com.deepdev.fordconnected.server.repository.UserRepository;
import com.deepdev.fordconnected.server.repository.AccessTokenRepository;

public class VehicleMutation implements GraphQLMutationResolver {
  private UserRepository userRepository;
  private AccessTokenRepository accessTokenRepository;

  @Autowired
  public VehicleMutation(UserRepository userRepository, AccessTokenRepository accessTokenRepository) {
    this.userRepository = userRepository;
    this.accessTokenRepository = accessTokenRepository;
  }

  
}

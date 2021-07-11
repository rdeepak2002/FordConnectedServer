package com.deepdev.fordconnected.server.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.deepdev.fordconnected.server.exception.CustomException;
import com.deepdev.fordconnected.server.model.AccessToken;
import com.deepdev.fordconnected.server.model.Friend;
import com.deepdev.fordconnected.server.model.User;
import com.deepdev.fordconnected.server.model.Vehicle;
import com.deepdev.fordconnected.server.repository.AccessTokenRepository;
import com.deepdev.fordconnected.server.repository.FriendRepository;
import com.deepdev.fordconnected.server.repository.UserRepository;
import com.deepdev.fordconnected.server.repository.VehicleRepository;

@Component
public class Query implements GraphQLQueryResolver {
  private UserRepository userRepository;
  private VehicleRepository vehicleRepository;
  private AccessTokenRepository accessTokenRepository;
  private FriendRepository friendRepository;

  @Autowired
  public Query(UserRepository userRepository, VehicleRepository vehicleRepository,
      AccessTokenRepository accessTokenRepository, FriendRepository friendRepository) {
    this.userRepository = userRepository;
    this.vehicleRepository = vehicleRepository;
    this.accessTokenRepository = accessTokenRepository;
    this.friendRepository = friendRepository;
  }

  public List<Friend> getFriends(String accessToken) {
    // search for the access token and user from databases
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);

    if (possibleAccessToken.isPresent()) {
      Optional<User> possibleUser = userRepository.findByFordProfileId(possibleAccessToken.get().getFordProfileId());
      if(possibleUser.isPresent()) {
        return friendRepository.findAllByUserId(possibleUser.get().getId());
      }
    }

    throw new CustomException(400, "getFriends Error: invalid access token");
  }

  public List<Vehicle> getVehicles(String accessToken) {
    // check if the access token was generated on this server
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);
    if (possibleAccessToken.isPresent()) {
      try {
        // get the access token object
        AccessToken accessTokenObj = possibleAccessToken.get();
        // get the user the access token belongs to
        User userObj = userRepository.findByFordProfileId(accessTokenObj.getFordProfileId()).get();
        return vehicleRepository.findAllByUserId(userObj.getId());
      } catch (Exception e) {
        e.printStackTrace();
        throw new CustomException(400, "getUserVehicles Error: user does not exist");
      }
    }
    throw new CustomException(400, "getUserVehicles Error: invalid access token");
  }
}
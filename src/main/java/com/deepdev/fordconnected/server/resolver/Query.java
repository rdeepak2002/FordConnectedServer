package com.deepdev.fordconnected.server.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.deepdev.fordconnected.server.exception.CustomException;
import com.deepdev.fordconnected.server.model.AccessToken;
import com.deepdev.fordconnected.server.model.Friend;
import com.deepdev.fordconnected.server.model.Post;
import com.deepdev.fordconnected.server.model.User;
import com.deepdev.fordconnected.server.model.Vehicle;
import com.deepdev.fordconnected.server.repository.AccessTokenRepository;
import com.deepdev.fordconnected.server.repository.FriendRepository;
import com.deepdev.fordconnected.server.repository.PostRepository;
import com.deepdev.fordconnected.server.repository.UserRepository;
import com.deepdev.fordconnected.server.repository.VehicleRepository;

@Component
public class Query implements GraphQLQueryResolver {
  private UserRepository userRepository;
  private VehicleRepository vehicleRepository;
  private AccessTokenRepository accessTokenRepository;
  private FriendRepository friendRepository;
  private PostRepository postRepository;

  @Autowired
  public Query(UserRepository userRepository, VehicleRepository vehicleRepository,
      AccessTokenRepository accessTokenRepository, FriendRepository friendRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.vehicleRepository = vehicleRepository;
    this.accessTokenRepository = accessTokenRepository;
    this.friendRepository = friendRepository;
    this.postRepository = postRepository;
  }

  public List<Friend> getFriends(String accessToken) {
    // search for the access token and user from databases
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);

    if (possibleAccessToken.isPresent()) {
      Optional<User> possibleUser = userRepository.findByFordProfileId(possibleAccessToken.get().getFordProfileId());
      if (possibleUser.isPresent()) {
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

  public List<Post> getPosts(String accessToken) {
    // check if the access token was generated on this server
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);
    if (possibleAccessToken.isPresent()) {
      try {
        // get the access token object
        AccessToken accessTokenObj = possibleAccessToken.get();
        // get the user the access token belongs to
        User userObj = userRepository.findByFordProfileId(accessTokenObj.getFordProfileId()).get();

        // get all the friends of the user
        List<Friend> friends = getFriends(accessToken);

        // get all public posts, posts from user, and post from friends and sort them
        Set<Post> posts = new HashSet<Post>();

        posts.addAll(postRepository.findAllByVisibility("public"));

        posts.addAll(postRepository.findAllByUserId(userObj.getId()));

        for (Friend friend : friends) {
          if (friend.getStatus().equals("ACCEPTED")) {
            // find which user in the pair is not the current user
            User user1 = friend.getPair().get(0);
            User user2 = friend.getPair().get(1);

            if (user1.getId().equals(userObj.getId())) {
              // user1 is the user, and user2 is the friend
              posts.addAll(postRepository.findAllByUserId(user2.getId()));
            } else {
              posts.addAll(postRepository.findAllByUserId(user1.getId()));
            }
          }
        }

        // convert set to a list
        List<Post> postList = new ArrayList<Post>();

        for (Post post : posts) {
          postList.add(post);
        }

        Collections.sort(postList, new Comparator<Post>() {
          @Override
          public int compare(Post p1, Post p2) {
            return -1 * p1.getUpdatedAt().compareTo(p2.getUpdatedAt());
          }
        });

        return postList;
      } catch (Exception e) {
        e.printStackTrace();
        throw new CustomException(400, "getPosts Error: user does not exist");
      }
    }
    throw new CustomException(400, "getPosts Error: invalid access token");
  }

  public User getUser(String accessToken) {
    // check if the access token was generated on this server
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);

    if (possibleAccessToken.isPresent()) {
      // get the access token
      AccessToken accessTokenObj = possibleAccessToken.get();

      // get the user the access token belongs to
      Optional<User> possibleUser = userRepository.findByFordProfileId(accessTokenObj.getFordProfileId());

      // update profile photo of user
      if (possibleUser.isPresent()) {
        User user = possibleUser.get();
        return user;
      }
    }

    throw new CustomException(400, "getUser Error: invalid access token");
  }
}
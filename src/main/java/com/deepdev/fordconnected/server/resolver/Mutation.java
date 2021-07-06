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

@Component
public class Mutation implements GraphQLMutationResolver {
  private UserRepository userRepository;
  private AccessTokenRepository accessTokenRepository;

  @Autowired
  public Mutation(UserRepository userRepository, AccessTokenRepository accessTokenRepository) {
    this.userRepository = userRepository;
    this.accessTokenRepository = accessTokenRepository;
  }

  public UserWithToken loginUser(String username, String firstName, String lastName, String code) {
    // get the current time
    LocalDateTime currentTime = LocalDateTime.now();
    long currentTimestampSeconds = System.currentTimeMillis() / 1000;

    // make request to ford api
    OkHttpClient client = new OkHttpClient().newBuilder().build();
    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
    RequestBody body = RequestBody.create(mediaType,
        "grant_type=authorization_code&client_id=30990062-9618-40e1-a27b-7c6bcb23658a&client_secret=T_Wk41dx2U9v22R5sQD4Z_E1u-l2B-jXHE&code="
            + code + "&redirect_uri=https%3A%2F%2Flocalhost%3A3000");
    Request request = new Request.Builder().url(
        "https://dah2vb2cprod.b2clogin.com/914d88b1-3523-4bf6-9be4-1b96b4f6f919/oauth2/v2.0/token?p=B2C_1A_signup_signin_common")
        .method("POST", body).addHeader("Content-Type", "application/x-www-form-urlencoded").build();

    try {
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);

      // get variables from response of request made to ford api
      String fordProfileId = Jobject.get("profile_info").toString(); // profile_info field
      String accessToken = Jobject.get("access_token").toString(); // access_token field
      long accessExpiresAtSeconds = Long.parseLong(Jobject.get("expires_on").toString()); // expires_on field
      String refreshToken = Jobject.get("refresh_token").toString(); // refresh_token field
      long refreshExpiresAtSeconds = currentTimestampSeconds
          + Long.parseLong(Jobject.get("refresh_token_expires_in").toString()); // refresh_token_expires_in field

      // create the user or get the current user with the username passed in
      Optional<User> userWithSameUsername = userRepository.findByUsername(username);
      User user = userWithSameUsername.isPresent() ? userWithSameUsername.get() : new User();

      // declare response
      UserWithToken response = new UserWithToken();

      // update the user
      if (!userWithSameUsername.isPresent()) {
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(currentTime);
        user.setFordProfileId(fordProfileId);
      }

      // update the response with the user and access token
      response.setUserId(user.getId());
      response.setFordProfileId(fordProfileId);
      response.setAccessToken(accessToken);
      response.setAccessExpiresAtSeconds(accessExpiresAtSeconds);
      response.setRefreshToken(refreshToken);
      response.setRefreshExpiresAtSeconds(refreshExpiresAtSeconds);

      // save user in database
      user.setUpdatedAt(currentTime);
      userRepository.save(user);

      // cache access token
      AccessToken accessTokenObj = new AccessToken();
      accessTokenObj.setAccessToken(accessToken);
      accessTokenObj.setFordProfileId(fordProfileId);
      accessTokenRepository.save(accessTokenObj);

      // retun the tokens, expiry times, and ford user id
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(500, "loginUser Error: invalid code");
    }
  }

  public UserWithToken refreshTokens(String refreshToken) {
    // get the current time
    long currentTimestampSeconds = System.currentTimeMillis() / 1000;

    // declare response
    UserWithToken response = new UserWithToken();

    // make request to ford api
    OkHttpClient client = new OkHttpClient().newBuilder()
      .build();
    MediaType mediaType = MediaType.parse("text/plain");
    RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
      .addFormDataPart("grant_type", "refresh_token")
      .addFormDataPart("refresh_token", refreshToken)
      .addFormDataPart("client_id", "30990062-9618-40e1-a27b-7c6bcb23658a")
      .addFormDataPart("client_secret", "T_Wk41dx2U9v22R5sQD4Z_E1u-l2B-jXHE")
      .build();
    Request request = new Request.Builder()
      .url("https://dah2vb2cprod.b2clogin.com/914d88b1-3523-4bf6-9be4-1b96b4f6f919/oauth2/v2.0/token?p=B2C_1A_signup_signin_common")
      .method("POST", body)
      .build();

    try{
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);
  
      // get variables from response of request made to ford api
      String fordProfileId = Jobject.get("profile_info").toString(); // profile_info field
      String accessToken = Jobject.get("access_token").toString(); // access_token field
      long accessExpiresAtSeconds = Long.parseLong(Jobject.get("expires_on").toString()); // expires_on field
      String newRefreshToken = Jobject.get("refresh_token").toString(); // refresh_token field
      long refreshExpiresAtSeconds = currentTimestampSeconds
            + Long.parseLong(Jobject.get("refresh_token_expires_in").toString()); // refresh_token_expires_in field
  
      // get the user from the ford profile id
      Optional<User> user = userRepository.findByFordProfileId(fordProfileId);

      if(user.isPresent()) {
        String userId = user.get().getId();
  
        // update the response with the user and access token
        response.setUserId(userId);
        response.setFordProfileId(fordProfileId);
        response.setAccessToken(accessToken);
        response.setAccessExpiresAtSeconds(accessExpiresAtSeconds);
        response.setRefreshToken(newRefreshToken);
        response.setRefreshExpiresAtSeconds(refreshExpiresAtSeconds);
    
        // cache access token
        AccessToken accessTokenObj = new AccessToken();
        accessTokenObj.setAccessToken(accessToken);
        accessTokenObj.setFordProfileId(fordProfileId);
        accessTokenRepository.save(accessTokenObj);
        
        // retun the tokens, expiry times, and ford user id
        return response;
      }
      else {
        throw new CustomException(500, "refreshTokens Error: user not registered");
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      throw new CustomException(500, "refreshTokens Error: invalid refresh token");
    }
  }
}
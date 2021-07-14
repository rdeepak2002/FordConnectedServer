package com.deepdev.fordconnected.server.resolver;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.deepdev.fordconnected.server.exception.CustomException;
import com.deepdev.fordconnected.server.model.AccessToken;
import com.deepdev.fordconnected.server.model.Friend;
import com.deepdev.fordconnected.server.model.Post;
import com.deepdev.fordconnected.server.model.User;
import com.deepdev.fordconnected.server.model.UserWithToken;
import com.deepdev.fordconnected.server.model.Vehicle;
import com.deepdev.fordconnected.server.repository.UserRepository;
import com.deepdev.fordconnected.server.repository.VehicleRepository;
import com.deepdev.fordconnected.server.repository.AccessTokenRepository;
import com.deepdev.fordconnected.server.repository.FriendRepository;
import com.deepdev.fordconnected.server.repository.PostRepository;

@Component
@SuppressWarnings("deprecation")
public class Mutation implements GraphQLMutationResolver {
  private UserRepository userRepository;
  private VehicleRepository vehicleRepository;
  private AccessTokenRepository accessTokenRepository;
  private FriendRepository friendRepository;
  private PostRepository postRepository;

  @Autowired
  public Mutation(UserRepository userRepository, VehicleRepository vehicleRepository,
      AccessTokenRepository accessTokenRepository, FriendRepository friendRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.vehicleRepository = vehicleRepository;
    this.accessTokenRepository = accessTokenRepository;
    this.friendRepository = friendRepository;
    this.postRepository = postRepository;
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
      String fordProfileId = Jobject.getString("profile_info"); // profile_info field
      String accessToken = Jobject.getString("access_token"); // access_token field
      long accessExpiresAtSeconds = Jobject.getLong("expires_on"); // expires_on field
      String refreshToken = Jobject.getString("refresh_token"); // refresh_token field
      long refreshExpiresAtSeconds = currentTimestampSeconds + Jobject.getLong("refresh_token_expires_in"); // refresh_token_expires_in
                                                                                                            // field

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
      user.setLastActive(currentTime);
      user.setUpdatedAt(currentTime);
      userRepository.save(user);

      // cache access token
      AccessToken accessTokenObj = new AccessToken();
      accessTokenObj.setId(accessToken);
      accessTokenObj.setAccessToken(accessToken);
      accessTokenObj.setFordProfileId(fordProfileId);
      accessTokenRepository.save(accessTokenObj);

      // update the list of vehicles for the user
      updateUserVehicles(accessToken);

      // retun the tokens, expiry times, and ford user id
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(400, "loginUser Error: invalid code");
    }
  }

  public UserWithToken refreshTokens(String refreshToken) {
    // get the current time
    LocalDateTime currentTime = LocalDateTime.now();
    long currentTimestampSeconds = System.currentTimeMillis() / 1000;

    // declare response
    UserWithToken response = new UserWithToken();

    // make request to ford api
    OkHttpClient client = new OkHttpClient().newBuilder().build();
    RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("grant_type", "refresh_token").addFormDataPart("refresh_token", refreshToken)
        .addFormDataPart("client_id", "30990062-9618-40e1-a27b-7c6bcb23658a")
        .addFormDataPart("client_secret", "T_Wk41dx2U9v22R5sQD4Z_E1u-l2B-jXHE").build();
    Request request = new Request.Builder().url(
        "https://dah2vb2cprod.b2clogin.com/914d88b1-3523-4bf6-9be4-1b96b4f6f919/oauth2/v2.0/token?p=B2C_1A_signup_signin_common")
        .method("POST", body).build();

    try {
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);

      // get variables from response of request made to ford api
      String fordProfileId = Jobject.getString("profile_info"); // profile_info field
      String accessToken = Jobject.getString("access_token"); // access_token field
      long accessExpiresAtSeconds = Jobject.getLong("expires_on"); // expires_on field
      String newRefreshToken = Jobject.getString("refresh_token"); // refresh_token field
      long refreshExpiresAtSeconds = currentTimestampSeconds + Jobject.getLong("refresh_token_expires_in"); // refresh_token_expires_in
                                                                                                            // field

      // get the user from the ford profile id
      Optional<User> user = userRepository.findByFordProfileId(fordProfileId);

      if (user.isPresent()) {
        String userId = user.get().getId();

        // update the response with the user and access token
        response.setUserId(userId);
        response.setFordProfileId(fordProfileId);
        response.setAccessToken(accessToken);
        response.setAccessExpiresAtSeconds(accessExpiresAtSeconds);
        response.setRefreshToken(newRefreshToken);
        response.setRefreshExpiresAtSeconds(refreshExpiresAtSeconds);

        // save user in database
        User userObj = user.get();
        userObj.setLastActive(currentTime);
        userObj.setUpdatedAt(currentTime);
        userRepository.save(userObj);

        // cache access token
        Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);
        AccessToken accessTokenObj = possibleAccessToken.isPresent() ? possibleAccessToken.get() : new AccessToken();
        accessTokenObj.setId(accessToken);
        accessTokenObj.setAccessToken(accessToken);
        accessTokenObj.setFordProfileId(fordProfileId);
        accessTokenRepository.save(accessTokenObj);

        // retun the tokens, expiry times, and ford user id
        return response;
      } else {
        throw new CustomException(400, "refreshTokens Error: user not registered");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new CustomException(400, "refreshTokens Error: invalid refresh token");
    }
  }

  public Post createPost(String accessToken, String visibility, String title, String body, ArrayList<String> files, String type) {
    // get the current time
    LocalDateTime currentTime = LocalDateTime.now();

    // search for the access token from databases
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);

    if (possibleAccessToken.isPresent()) {
      Optional<User> possibleUser = userRepository.findByFordProfileId(possibleAccessToken.get().getFordProfileId());
      if (possibleUser.isPresent()) {
        // get the current user and the friend to add
        User user = possibleUser.get();

        // create the new post and save it to db
        Post newPost = new Post();

        newPost.setUserId(user.getId());
        newPost.setFordProfileId(user.getFordProfileId());
        newPost.setVisibility(visibility);
        newPost.setTitle(title);
        newPost.setBody(body);
        newPost.setFiles(files);
        newPost.setUser(user);
        newPost.setCreatedAt(currentTime);
        newPost.setUpdatedAt(currentTime);
        newPost.setType(type);

        postRepository.save(newPost);
        
        return newPost;
      }
    }

    throw new CustomException(400, "createPost Error: invalid access token or user does not exist");
  }

  public String addFriend(String accessToken, String username) {
    // get the current time
    LocalDateTime currentTime = LocalDateTime.now();

    // search for the access token and user from databases
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);
    Optional<User> possibleFriend = userRepository.findByUsername(username);

    if (possibleAccessToken.isPresent()) {
      Optional<User> possibleUser = userRepository.findByFordProfileId(possibleAccessToken.get().getFordProfileId());

      if (possibleUser.isPresent()) {
        // get the current user and the friend to add
        User user = possibleUser.get();
        User friend = possibleFriend.get();

        // get user's id
        String userId = user.getId();

        Optional<Friend> existingFriendPair = friendRepository.findByUserIds(user.getId(), userId);
        Friend friendPair = existingFriendPair.isPresent() ? existingFriendPair.get() : new Friend(user, friend);
        if (existingFriendPair.isPresent() && !existingFriendPair.get().getRequesterUserId().equals(user.getId())) {
          friendPair.setStatus("ACCEPTED");
        } else {
          friendPair.setCreatedAt(currentTime);
        }
        friendPair.setUpdatedAt(currentTime);
        friendRepository.save(friendPair);

        return friendPair.getStatus();
      }
    }

    throw new CustomException(400, "addFriend Error: invalid access token or user does not exist");
  }

  public List<Vehicle> updateUserVehicles(String accessToken) {
    // get the current time
    LocalDateTime currentTime = LocalDateTime.now();

    // check if the access token was generated on this server
    Optional<AccessToken> possibleAccessToken = accessTokenRepository.findById(accessToken);

    ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

    if (possibleAccessToken.isPresent()) {
      try {
        // get the access token object
        AccessToken accessTokenObj = possibleAccessToken.get();

        // get the user the access token belongs to
        User userObj = userRepository.findByFordProfileId(accessTokenObj.getFordProfileId()).get();

        // make request to ford api
        JSONObject vehiclesList = getVehiclesList(accessToken);

        // get variables from response of request made to ford api
        String vehiclesArrStr = vehiclesList.get("vehicles").toString(); // vehicles field
        JSONArray vehiclesArr = new JSONArray(vehiclesArrStr);

        // update each vehicle the user owns
        for (int i = 0; i < vehiclesArr.length(); i++) {
          JSONObject vehicleJson = vehiclesArr.getJSONObject(i);

          String id = vehicleJson.getString("vehicleId");
          String userId = userObj.getId();
          String fordProfileId = userObj.getFordProfileId();
          String make = vehicleJson.getString("make");
          String modelName = vehicleJson.getString("modelName");
          String modelYear = vehicleJson.getString("modelYear");
          String color = vehicleJson.getString("color");
          String nickname = vehicleJson.getString("nickName");
          Boolean modemEnabled = vehicleJson.getBoolean("modemEnabled");
          Integer vehicleAuthorizationIndicator = vehicleJson.getInt("vehicleAuthorizationIndicator");
          Boolean serviceCompatible = vehicleJson.getBoolean("serviceCompatible");

          // get vehicle if it already exists otherwise create a new object
          Optional<Vehicle> vehicleWithSameId = vehicleRepository.findById(id);
          Vehicle vehicle = vehicleWithSameId.isPresent() ? vehicleWithSameId.get() : new Vehicle();

          // update the vehicle info
          updateVehicleInformation(accessToken, id);

          // get the latest vehicle info
          JSONObject vehicleInfo = getVehicleInformation(accessToken);
          JSONObject vehicleInfoJson = vehicleInfo.getJSONObject("vehicle");

          JSONObject vehicleDetails = vehicleInfoJson.getJSONObject("vehicleDetails");
          JSONObject fuelLevel = vehicleDetails.getJSONObject("fuelLevel");

          JSONObject vehicleStatus = vehicleInfoJson.getJSONObject("vehicleStatus");
          JSONObject chargingStatus = vehicleStatus.getJSONObject("chargingStatus");
          JSONObject ignitionStatus = vehicleStatus.getJSONObject("ignitionStatus");

          JSONObject vehicleLocation = vehicleInfoJson.getJSONObject("vehicleLocation");

          LocalDateTime lastUpdated = currentTime;
          String engineType = vehicleInfoJson.getString("engineType");
          Double fuelLevelValue = fuelLevel.getDouble("value");
          Double fuelLevelDistanceToEmpty = fuelLevel.getDouble("distanceToEmpty");
          Double mileage = vehicleDetails.getDouble("mileage");
          Double odometer = vehicleDetails.getDouble("odometer");
          String remoteStartStatus = vehicleStatus.getJSONObject("remoteStartStatus").toString();
          String chargingStatusValue = chargingStatus.getString("value");
          String ignitionStatusValue = ignitionStatus.getString("value");
          String doorStatus = vehicleStatus.getJSONArray("doorStatus").toString();
          Double vehicleLocationLongitude = vehicleLocation.getDouble("longitude");
          Double vehicleLocationLatitude = vehicleLocation.getDouble("latitude");
          Double vehicleLocationSpeed = vehicleLocation.getDouble("speed");
          String vehicleLocationDirection = vehicleLocation.getString("direction");
          LocalDateTime createdAt = currentTime;
          LocalDateTime updatedAt = currentTime;

          vehicle.setId(id);
          vehicle.setUserId(userId);
          vehicle.setFordProfileId(fordProfileId);
          vehicle.setMake(make);
          vehicle.setModelName(modelName);
          vehicle.setModelYear(modelYear);
          vehicle.setColor(color);
          vehicle.setNickname(nickname);
          vehicle.setModemEnabled(modemEnabled);
          vehicle.setVehicleAuthorizationIndicator(vehicleAuthorizationIndicator);
          vehicle.setServiceCompatible(serviceCompatible);
          vehicle.setLastUpdated(lastUpdated);
          vehicle.setEngineType(engineType);
          vehicle.setFuelLevelValue(fuelLevelValue);
          vehicle.setFuelLevelDistanceToEmpty(fuelLevelDistanceToEmpty);
          vehicle.setMileage(mileage);
          vehicle.setOdometer(odometer);
          vehicle.setRemoteStartStatus(remoteStartStatus);
          vehicle.setChargingStatusValue(chargingStatusValue);
          vehicle.setIgnitionStatusValue(ignitionStatusValue);
          vehicle.setDoorStatus(doorStatus);
          vehicle.setVehicleLocationLongitude(vehicleLocationLongitude);
          vehicle.setVehicleLocationLatitude(vehicleLocationLatitude);
          vehicle.setVehicleLocationSpeed(vehicleLocationSpeed);
          vehicle.setVehicleLocationDirection(vehicleLocationDirection);
          if (!vehicleWithSameId.isPresent()) {
            vehicle.setCreatedAt(createdAt);
          }
          vehicle.setUpdatedAt(updatedAt);

          // save vehicle in db
          vehicleRepository.save(vehicle);

          // add vehicle to vehicles array
          vehicles.add(vehicle);
        }

        // update the user's vehicles
        userObj.setVehicles(vehicles);
        userObj.setUpdatedAt(currentTime);
        userObj.setLastActive(currentTime);
        userRepository.save(userObj);

        // return all the user's vehicles
        return vehicleRepository.findAllByUserId(userObj.getId());
      } catch (Exception e) {
        e.printStackTrace();
        throw new CustomException(400, "updateUserVehicles Error: invalid access token");
      }
    }
    throw new CustomException(400, "updateUserVehicles Error: invalid access token");
  }

  private JSONObject getVehiclesList(String accessToken) {
    try {
      OkHttpClient client = new OkHttpClient().newBuilder().build();
      Request request = new Request.Builder().url("https://api.mps.ford.com/api/fordconnect/vehicles/v1")
          .method("GET", null).addHeader("Accept", "application/json").addHeader("Content-Type", "application/json")
          .addHeader("api-version", "2020-06-01").addHeader("Application-Id", "afdc085b-377a-4351-b23e-5e1d35fb3700")
          .addHeader("Authorization", "Bearer " + accessToken).build();
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);

      if (!Jobject.optString("status").equals("SUCCESS")) {
        throw new CustomException(400, "getVehiclesList Error: error getting vehicle list");
      }

      return Jobject;
    } catch (Exception e) {
      throw new CustomException(400, "getVehiclesList Error: invalid access token");
    }
  }

  private void updateVehicleInformation(String accessToken, String vehicleId) {
    try {
      OkHttpClient client = new OkHttpClient().newBuilder().build();
      MediaType JSON = MediaType.parse("application/json; charset=utf-8");
      RequestBody body = RequestBody.create(JSON, "{}");
      Request request = new Request.Builder()
          .url("https://api.mps.ford.com/api/fordconnect/vehicles/v1/" + vehicleId + "/status").method("POST", body)
          .addHeader("Accept", "*/*").addHeader("Content-Type", "application/json")
          .addHeader("Application-Id", "afdc085b-377a-4351-b23e-5e1d35fb3700")
          .addHeader("Authorization", "Bearer " + accessToken).addHeader("api-version", "2020-06-01")
          .addHeader("callback-url", "{{callback-url}}").build();
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);

      if (!Jobject.optString("status").equals("SUCCESS")) {
        throw new CustomException(400, "updateVehicleInformation Error: error updating vehicle");
      }
    } catch (Exception e) {
      throw new CustomException(400, "updateVehicleInformation Error: invalid access token");
    }
  }

  private JSONObject getVehicleInformation(String accessToken) {
    try {
      OkHttpClient client = new OkHttpClient().newBuilder().build();
      Request request = new Request.Builder()
          .url("https://api.mps.ford.com/api/fordconnect/vehicles/v1/8a7f9fa878849d8a0179579d2f26043a")
          .method("GET", null).addHeader("Application-Id", "afdc085b-377a-4351-b23e-5e1d35fb3700")
          .addHeader("Authorization", "Bearer " + accessToken).addHeader("api-version", "2020-06-01").build();
      Response okHttpResponse = client.newCall(request).execute();
      String responseString = okHttpResponse.body().string();
      JSONObject Jobject = new JSONObject(responseString);

      if (!Jobject.optString("status").equals("SUCCESS")) {
        throw new CustomException(400, "getVehicleInformation Error: error getting vehicle information");
      }

      return Jobject;
    } catch (Exception e) {
      throw new CustomException(400, "getVehicleInformation Error: invalid access token");
    }
  }
}
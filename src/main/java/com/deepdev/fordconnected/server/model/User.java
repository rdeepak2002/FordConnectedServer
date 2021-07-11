package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "users")
public class User {
  @Id
  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String fordProfileId;
  private LocalDateTime updatedAt;
  private LocalDateTime createdAt;
  private LocalDateTime lastActive;
  private List<Vehicle> vehicles;
  private List<User> friends;

  public User() {
    this.vehicles = new ArrayList<Vehicle>();
    this.friends = new ArrayList<User>();
  }

  public void addVehicle(Vehicle vehicle) {
    if(vehicles == null) {
      this.vehicles = new ArrayList<Vehicle>();
    }

    for(int i = 0; i < vehicles.size(); i++) {
      Vehicle currentVehicle = vehicles.get(i);
      if(currentVehicle.getId().equals(vehicle.getId())) {
        vehicles.set(i, vehicle);
        return;
      }
    }
    
    vehicles.add(vehicle);
  }

  public void addFriend(User friend) {
    if(friends == null) {
      this.friends = new ArrayList<User>();
    }

    for(int i = 0; i < friends.size(); i++) {
      User currentFriend = friends.get(i);
      if(currentFriend.getId().equals(friend.getId())) {
        friends.set(i, friend);
        return;
      }
    }

    friends.add(friend);
  }
}
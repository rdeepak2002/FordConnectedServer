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

  public User() {
    this.vehicles = new ArrayList<Vehicle>();
  }

  public void addVehicle(Vehicle vehicle) {
    vehicles.add(vehicle);
  }
}
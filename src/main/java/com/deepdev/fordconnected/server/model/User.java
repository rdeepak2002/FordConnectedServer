package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
  @Indexed
  private String username;
  private String firstName;
  private String lastName;
  @Indexed
  private String fordProfileId;
  private LocalDateTime updatedAt;
  private LocalDateTime createdAt;
  private LocalDateTime lastActive;
  private ArrayList<Vehicle> vehicles;

  public User() {
    this.vehicles = new ArrayList<Vehicle>();
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
}
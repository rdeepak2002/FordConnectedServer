package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "vehicles")
public class Vehicle {
  @Id
  private String id;
  private String userId;
  private String fordProfileId;
  private String make;
  private String modelName;
  private String modelYear;
  private String color;
  private String nickname;
  private Boolean modemEnabled;
  private Integer vehicleAuthorizationIndicator;
  private Boolean serviceCompatible;
  private LocalDateTime lastUpdated;
  private String engineType;
  private Double fuelLevelValue;
  private Double fuelLevelDistanceToEmpty;
  private Double mileage;
  private Double odometer;
  private String remoteStartStatus;
  private String chargingStatusValue;
  private String ignitionStatusValue;
  private String doorStatus;
  private Double vehicleLocationLongitude;
  private Double vehicleLocationLatitude;
  private Double vehicleLocationSpeed;
  private String vehicleLocationDirection;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

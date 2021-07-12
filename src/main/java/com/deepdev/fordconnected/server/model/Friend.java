package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "friends")
public class Friend {
  @Id
  private String id;
  private String requesterUserId;
  private String status;
  @DBRef
  private ArrayList<User> pair;
  @Indexed
  private ArrayList<String> pairStr;
  private LocalDateTime updatedAt;
  private LocalDateTime createdAt;

  public Friend(User friend1, User friend2) {
    this.requesterUserId = friend1.getId();
    this.pair = new ArrayList<User>();
    this.pair.add(friend1);
    this.pair.add(friend2);
    this.pairStr = new ArrayList<String>();
    this.pairStr.add(friend1.getId());
    this.pairStr.add(friend2.getId());
    this.status = "REQUESTED";
  }
}

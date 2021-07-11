package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@CompoundIndexes({
  @CompoundIndex(name = "friend_pair_userId", def = "{ 'pair.id': 1 }")
})
@Document(collection = "friends")
public class Friend {
  @Id
  private String id;
  private String requesterUserId;
  private String status;
  private ArrayList<User> pair;
  private LocalDateTime updatedAt;
  private LocalDateTime createdAt;

  public Friend(User friend1, User friend2) {
    this.requesterUserId = friend1.getId();
    this.pair = new ArrayList<User>();
    this.pair.add(friend1);
    this.pair.add(friend2);
    this.status = "REQUESTED";
  }
}

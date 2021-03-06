package com.deepdev.fordconnected.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "posts")
public class Post {
  @Id
  private String id;
  private String userId;
  private String fordProfileId;
  private String visibility;
  private String title;
  private String body;
  private ArrayList<String> files;
  @DBRef
  private User user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String type;

  public Post() {
    this.files = new ArrayList<String>();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Post) && ((Post) o).getId().equals(this.getId());
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

package com.deepdev.fordconnected.server.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RedisHash(value = "AccessToken", timeToLive = 1200)  // 20 minute time to live
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AccessToken implements Serializable {
  @Id
  private String id;
  private String accessToken;
  private String fordProfileId;
}

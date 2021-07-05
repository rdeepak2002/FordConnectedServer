package com.deepdev.fordconnected.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserWithToken {
  private String userId;
  private String fordProfileId;
  private String accessToken;
  private Long accessExpiresAtSeconds;
  private String refreshToken;
  private Long refreshExpiresAtSeconds;
}
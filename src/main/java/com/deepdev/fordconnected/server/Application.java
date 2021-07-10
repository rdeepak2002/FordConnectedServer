package com.deepdev.fordconnected.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@EnableCaching
public class Application {
  @Value("${REDIS_ENDPOINT}")
  private String REDIS_ENDPOINT;

  @Value("${REDIS_PORT}")
  private String REDIS_PORT;

  @Value("${REDIS_PASSWORD}")
  private String REDIS_PASSWORD;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(REDIS_ENDPOINT,
        Integer.parseInt(REDIS_PORT));

    if (REDIS_PASSWORD != null) {
      redisStandaloneConfiguration.setPassword(RedisPassword.of(REDIS_PASSWORD));
    }

    return new JedisConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(jedisConnectionFactory());
    return template;
  }
}

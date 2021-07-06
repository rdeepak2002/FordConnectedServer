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

import io.lettuce.core.RedisURI;

@SpringBootApplication
@EnableCaching
public class Application {
  // get redis url from environment
  @Value("${REDIS_URL}")
  private String REDIS_URL;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    RedisURI redisURI = RedisURI.create(REDIS_URL);

    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisURI.getHost(),
        redisURI.getPort());
    if (redisURI.getPassword() != null) {
      redisStandaloneConfiguration.setPassword(RedisPassword.of(redisURI.getPassword()));
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

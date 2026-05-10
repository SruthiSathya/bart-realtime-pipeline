package com.bart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    // StringRedisTemplate is Spring's wrapper for Redis string operations
    // Auto-configured using our application.properties
    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store latest station status
    // Key format: "bart:station:Montgomery St"
    // Value: JSON string of the latest event
    public void saveStationStatus(String station, String status) {
        String key = "bart:station:" + station;
        redisTemplate.opsForValue().set(key, status);
        log.info("Cached in Redis: key={}", key);
    }

    // Retrieve latest station status
    public String getStationStatus(String station) {
        String key = "bart:station:" + station;
        return redisTemplate.opsForValue().get(key);
    }
}

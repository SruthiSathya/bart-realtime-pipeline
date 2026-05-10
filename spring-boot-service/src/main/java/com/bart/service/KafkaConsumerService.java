package com.bart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final RedisService redisService;

    public KafkaConsumerService(RedisService redisService) {
        this.redisService = redisService;
    }

    // @KafkaListener tells Spring to subscribe to this topic automatically
    // Every message that arrives calls this method
    @KafkaListener(topics = "bart-events", groupId = "bart-consumer-group")
    public void consume(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String station) {

        log.info("Consumed from Kafka: station={}, payload={}", station, message);

        // Store latest status in Redis
        redisService.saveStationStatus(station, message);
    }
}

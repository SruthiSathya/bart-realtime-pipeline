package com.bart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bart")
public class BartController {

    private static final Logger log = LoggerFactory.getLogger(BartController.class);

    private final KafkaProducerService kafkaProducerService;
    private final RedisService redisService;

    public BartController(KafkaProducerService kafkaProducerService, RedisService redisService) {
        this.kafkaProducerService = kafkaProducerService;
        this.redisService = redisService;
    }

    @PostMapping("/events")
    public ResponseEntity<String> receiveEvent(@RequestBody BartEvent event) {
        log.info("Received BART event: station={}, line={}, minutes={}, status={}",
                event.getStation(),
                event.getLine(),
                event.getMinutes(),
                event.getStatus());

        String payload = String.format(
                "{\"station\":\"%s\",\"line\":\"%s\",\"minutes\":%d,\"status\":\"%s\"}",
                event.getStation(),
                event.getLine(),
                event.getMinutes(),
                event.getStatus()
        );

        kafkaProducerService.sendEvent(event.getStation(), payload);
        return ResponseEntity.ok("Event received and published to Kafka: " + event.getStation());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BART service is running");
    }

    @GetMapping("/station/{name}")
    public ResponseEntity<String> getStationStatus(@PathVariable String name) {
        String status = redisService.getStationStatus(name);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}

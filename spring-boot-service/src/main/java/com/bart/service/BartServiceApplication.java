package com.bart.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This one annotation does a lot:
// - Marks this as a Spring Boot app
// - Auto-configures everything (web server, JSON parsing, etc.)
// - Scans this package for controllers, services, etc.
@SpringBootApplication
public class BartServiceApplication {

    public static void main(String[] args) {
        // Starts the embedded Tomcat server on port 8080
        SpringApplication.run(BartServiceApplication.class, args);
    }
}

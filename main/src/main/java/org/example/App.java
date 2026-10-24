package org.example;

import org.example.config.EnvFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        EnvFile.load();
        SpringApplication.run(App.class, args);
    }
}
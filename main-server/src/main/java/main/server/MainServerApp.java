package main.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {"main.server", "client"})
public class MainServerApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServerApp.class, args);
    }
}
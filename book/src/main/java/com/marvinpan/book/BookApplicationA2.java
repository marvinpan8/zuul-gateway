package com.marvinpan.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class BookApplicationA2 {

    public static void main(String[] args) {
    	System.setProperty("spring.profiles.active", "a2");
        SpringApplication.run(BookApplicationA2.class, args);
    }
}
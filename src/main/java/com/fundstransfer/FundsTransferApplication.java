package com.fundstransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FundsTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundsTransferApplication.class, args);
    }
} 
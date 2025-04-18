package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.repository.DataSource;

@Configuration
public class configuration {
    @Bean
    DataSource dataSource(){
        return new DataSource();
    }
  

}

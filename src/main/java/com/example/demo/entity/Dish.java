package com.example.demo.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Dish {
    private Long id;
    private String name;
    private Long quantity;
    private double amount;
}

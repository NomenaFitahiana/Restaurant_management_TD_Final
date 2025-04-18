package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Sale {
    private Long id;
    private String name;
    private LocalDateTime updatedAt;
    private List<Dish> dishes;
}

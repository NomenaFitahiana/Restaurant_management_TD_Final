package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PointOfSale {
    private Long id;
    private String name;
    private LocalDateTime synchronisationDate;
    private List<Dish> dishes;
}

package com.example.demo.entity;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Sale {
    private Long id;
    private String salesPoint;
    private String dish;
    private int quantitySold;
    private double totalAmount;
}

package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BestSale {
    private Long id;
    private LocalDateTime updatedAt;
    private List<Sale> sales;
}

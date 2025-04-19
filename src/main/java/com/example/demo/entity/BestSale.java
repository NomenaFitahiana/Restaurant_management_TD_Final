package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BestSale {
    @JsonIgnore
    private Long id;
    private LocalDateTime updatedAt;
    private List<Sale> sales;
}

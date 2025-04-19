package com.example.demo.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BestProcessingTime {
   private String salesPoint;
   private Long dishId;
   private String dish;
   private double preparationDuration;
   private DurationUnit durationUnit;
}

package com.example.demo.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BestProcessingTime {
   private String salesPoint;
   private String dishName;
   private Double duration;
   private DurationUnit durationUnit;
   private CalculationMode calculationMode;
}

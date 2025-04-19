package com.example.demo.controller;

import java.rmi.ServerException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.AllProcessingTime;
import com.example.demo.entity.BestSale;
import com.example.demo.entity.CalculationMode;
import com.example.demo.entity.DurationUnit;
import com.example.demo.entity.Sale;
import com.example.demo.service.CentralService;
import com.example.demo.service.Exceptions.*;

@RestController
public class CentralController{
    @Autowired  private CentralService centralService;

    /*@GetMapping("dishes/{id}/bestProcessingTime")
    public ResponseEntity<Object> getDishWithBestProcessingTime(@PathVariable Long id, @RequestParam (name = "top", required = false) int top, @RequestParam (name = "durationUnit", required = false) DurationUnit durationUnit, @RequestParam(name = "calculationMode", required = false) CalculationMode calculationMode ) {
        try {
            AllProcessingTime allProcessingTime = centralService.getAllBestProcessingTime(top, durationUnit, calculationMode);
            return new ResponseEntity<>(allProcessingTime, HttpStatus.OK);
        } catch (ClientException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }*/
    
  
    @GetMapping("/bestSales")
    public ResponseEntity<Object> getAllBestSales(@RequestParam (name = "top", required = false) Integer top ){
        try {
            BestSale bestSales = centralService.getAllBestSales(top);
            return new ResponseEntity<>(bestSales, HttpStatus.OK);
        } catch (ClientException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @PostMapping("/synchronization")
    public ResponseEntity<BestSale> synchronizeFromBothApis() {
    
    BestSale result = centralService.fetchAndSaveFromBothApis();
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}
}




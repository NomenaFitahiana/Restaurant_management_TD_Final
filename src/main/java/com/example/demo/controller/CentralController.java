package com.example.demo.controller;

import java.rmi.ServerException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.BestSale;
import com.example.demo.entity.Sale;
import com.example.demo.service.CentralService;
import com.example.demo.service.Exceptions.*;

@RestController
public class CentralController{
    @Autowired  private CentralService centralService;

  
    @GetMapping("/bestSales")
    public ResponseEntity<Object> getAllBestSales(@RequestParam (name = "limit", required = false) Integer limit , @RequestParam(name = "startDate", required = false) LocalDateTime startDate, @RequestParam (name = "endDate", required = false) LocalDateTime endDate){
        try {
            List<BestSale> bestSales = centralService.getAllBestSales(limit, startDate, endDate);
            return new ResponseEntity<>(bestSales, HttpStatus.OK);
        } catch (ClientException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @PostMapping("/synchronization")
    public ResponseEntity<BestSale> synchronizeOrders(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int limit) {
        
        String ordersData = centralService.fetchOrdersSales(startDate, endDate, limit);
        
        List<Sale> salesToSave = centralService.convertToSales(ordersData);
        
        BestSale savedSales = centralService.saveAll(salesToSave);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSales);
    }
}




package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.BestSale;
import com.example.demo.service.CentralService;

@RestController
@RequestMapping("/")
public class CentralController{
    @Autowired  private CentralService centralService;

  
    @GetMapping("/bestSales")
    public ResponseEntity<Object> getAllBestSales(@RequestParam (name = "limit", required = false) Integer limit , @RequestParam(name = "startDate", required = false) LocalDateTime startDate, @RequestParam (name = "endDate", required = false) LocalDateTime endDate){
        List<BestSale> bestSale = centralService.getAllBestSales(limit, startDate, endDate);
      
        return new ResponseEntity<>(bestSale, HttpStatus.OK);
    }


}

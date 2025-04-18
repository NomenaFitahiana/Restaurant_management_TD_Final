package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.BestSale;
import com.example.demo.repository.CentralRepository;

@Service
public class CentralService {
    @Autowired private CentralRepository centralRepository;

    public List<BestSale> getAllBestSales(Integer limit, LocalDateTime startDate, LocalDateTime endDate){
        return centralRepository.getAllBestSales(limit, startDate, endDate);
    }
}

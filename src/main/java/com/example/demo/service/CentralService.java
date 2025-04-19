package com.example.demo.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.BestSale;
import com.example.demo.entity.Sale;
import com.example.demo.repository.CentralRepository;
import com.example.demo.repository.SaleRepository;
import com.example.demo.service.Exceptions.ServerException;

@Service
public class CentralService {
    @Autowired private CentralRepository centralRepository;
    @Autowired private SaleRepository saleRepository;

    public List<BestSale> getAllBestSales(Integer limit, LocalDateTime startDate, LocalDateTime endDate){
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date must be before end date");
                }
        
        return centralRepository.getAllBestSales(limit, startDate, endDate);  
    }

    public BestSale saveAll(List<Sale> sale){
        BestSale sales = centralRepository.saveAll(sale);
        return sales;
    }
}

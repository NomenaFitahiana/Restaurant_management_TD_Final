package com.example.demo.repository;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BestSale;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CentralRepository {
    @Autowired private final DataSource dataSource;

    public String getAllBestSales(){
       try (  Connection connection = dataSource.getConnection();){
      
        if (connection == null) {
            return "failed";
        }

       } catch (Exception e) {
        // TODO: handle exception
       }
              return "Connected";
    }
}

package com.example.demo.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BestSale;
import com.example.demo.entity.Sale;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CentralRepository {
    @Autowired private final DataSource dataSource;
    @Autowired private final SaleRepository saleRepository;

    public List<BestSale> getAllBestSales(){
        List<BestSale> bestSales = new ArrayList<>();
       try (  Connection connection = dataSource.getConnection();
       PreparedStatement statement = connection.prepareStatement("select bs.id, bs.updatedat, bs.id_sale from best_sale bs")){
        
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            BestSale bestSale = new BestSale();
            Long id = resultSet.getLong("id");
            LocalDateTime updatedAt = resultSet.getTimestamp("updatedat").toLocalDateTime();
            List<Sale> sales = saleRepository.getByUpdatedAt(updatedAt);

            bestSale.setId(id);
            bestSale.setUpdatedAt(updatedAt);
            bestSale.setSales(sales);

            bestSales.add(bestSale);
        }
      return bestSales;

       } catch (SQLException e) {
        throw new RuntimeException(e);
       }
    }
}

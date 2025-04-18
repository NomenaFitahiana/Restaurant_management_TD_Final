package com.example.demo.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    public List<BestSale> getAllBestSales(Integer limit, LocalDateTime startDate, LocalDateTime endDate){
        List<BestSale> bestSales = new ArrayList<>();
       try (  Connection connection = dataSource.getConnection()){
        StringBuilder query = new StringBuilder("SELECT bs.id, bs.updatedat, bs.id_sale FROM best_sale bs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        if (startDate != null && endDate != null) {
            query.append(" AND updatedat BETWEEN ? AND ?");
            parameters.add(Timestamp.valueOf(startDate));
            parameters.add(Timestamp.valueOf(endDate));
        }

        query.append(" ORDER BY bs.updatedat DESC");

        if (limit != null) {
            query.append(" LIMIT ?");
            parameters.add(limit);
        }

        PreparedStatement statement = connection.prepareStatement(query.toString());

       for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof Timestamp) {
                    statement.setTimestamp(i + 1, (Timestamp) param);
                } else if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                }
            }

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
        throw new RuntimeException("Error fetching best sales", e);       }
    }

    public BestSale saveAll(List<Sale> sales){
        BestSale bestSale = new BestSale();
    }
}

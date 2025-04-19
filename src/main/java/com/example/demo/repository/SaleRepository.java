package com.example.demo.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Sale;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SaleRepository {
    @Autowired private final DataSource dataSource;

    public List<Sale> getByUpdatedAt(LocalDateTime updatedAt){
        List<Sale> sales = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("select s.id, s.sale_point, s.dish, s.quantity_sold, s.total_amount, b.updatedat from sale s join best_sale b on b.id_sale = s.id where updatedat = ? ")) {
            statement.setTimestamp(1, Timestamp.valueOf(updatedAt));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Sale sale = new Sale();
                sale.setId(resultSet.getLong("id"));
                sale.setSalesPoint(resultSet.getString("sale_point"));
                sale.setDish(resultSet.getString("dish"));
                sale.setQuantitySold(resultSet.getInt("quantity_sold"));
                sale.setTotalAmount(resultSet.getDouble("total_amount"));

                sales.add(sale);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sales;
    }

    public Sale saveAll(Sale saleToSave) throws SQLException{
        Sale sale = new Sale();
        PostgresNextReference postgresNextReference = new PostgresNextReference();

        try (Connection connection = dataSource.getConnection()) {
            
                try (PreparedStatement statement =
                             connection.prepareStatement("insert into sale (id, sale_point, dish, quantity_sold, total_amount) values (?, ?, ?, ?, ?)"
                                     + " on conflict (id) do nothing"
                                     + " returning id, sale_point, dish, quantity_sold, total_amount")) {
                    long id = saleToSave.getId() == null ? postgresNextReference.nextID("saleToSave", connection) : saleToSave.getId();
                    statement.setLong(1, id);
                    statement.setString(2, saleToSave.getSalesPoint());
                    statement.setString(3, saleToSave.getDish());
                    statement.setInt(4, saleToSave.getQuantitySold());
                    statement.setDouble(5, saleToSave.getTotalAmount());

                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        sale.setId(resultSet.getLong("id"));
                        sale.setSalesPoint(resultSet.getString("sale_point"));
                        sale.setDish(resultSet.getString("dish"));
                        sale.setQuantitySold(resultSet.getInt("quantity_sold"));
                        sale.setTotalAmount(resultSet.getDouble("total_amount"));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
        }
        return sale;
    }
}


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

    public List<Sale> saveAll(List<Sale> saleToSave) throws SQLException{
        List<Sale> sales = new ArrayList<>();
        PostgresNextReference postgresNextReference = new PostgresNextReference();

        try (Connection connection = dataSource.getConnection()) {
            saleToSave.forEach(entityToSave -> {
                try (PreparedStatement statement =
                             connection.prepareStatement("insert into sale (id, sale_point, dish, quantity_sold, total_amount) values (?, ?, ?, ?, ?)"
                                     + " on conflict (id) do nothing"
                                     + " returning id, sale_point, dish, quantity_sold, total_amount")) {
                    long id = entityToSave.getId() == null ? postgresNextReference.nextID("entityToSave", connection) : entityToSave.getId();
                    statement.setLong(1, id);
                    statement.setString(2, entityToSave.getSalesPoint());
                    statement.setString(3, entityToSave.getDish());
                    statement.setInt(4, entityToSave.getQuantitySold());
                    statement.setDouble(5, entityToSave.getTotalAmount());
                    statement.executeBatch();

                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                       
                        Sale savedsale = new Sale();
                        savedsale.setId(resultSet.getLong("id"));
                        savedsale.setSalesPoint(resultSet.getString("sale_point"));
                        savedsale.setDish(resultSet.getString("dish"));
                        savedsale.setQuantitySold(resultSet.getInt("quantity_sold"));
                        savedsale.setTotalAmount(resultSet.getDouble("total_amount"));
                        sales.add(savedsale);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return sales;
    }
}


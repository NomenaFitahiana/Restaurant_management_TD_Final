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

    public BestSale saveAll(List<Sale> saleToSave) {
        BestSale bestSale = new BestSale();
        List<Sale> savedSales = new ArrayList<>();
    
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
    
            try {
                for (Sale s : saleToSave) {
                    Sale savedSale = saleRepository.saveAll(s);
                    savedSales.add(savedSale);
                }
    
                String insertBestSaleSQL = "INSERT INTO best_sale (updatedat, id_sale) VALUES (?, ?) " +
                                         "RETURNING id, updatedat";
    
                savedSales.forEach(s -> {
                    try (PreparedStatement statement = connection.prepareStatement(insertBestSaleSQL)) {
                        statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                        statement.setLong(2, s.getId());
                        statement.addBatch();
    
                        try (ResultSet rs = statement.executeQuery()) {
                            if (rs.next()) {
                                bestSale.setId(rs.getLong("id"));
                                bestSale.setUpdatedAt(rs.getTimestamp("updatedat").toLocalDateTime());
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Erreur lors de l'insertion dans best_sale", e);
                    }
                });
    
                bestSale.setSales(savedSales);
                connection.commit();
    
            } catch (RuntimeException | SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw new RuntimeException("Erreur lors de la transaction", e);
            }
    
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    
        return bestSale;
    }

    }


package com.example.demo.repository.operations;

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

import com.example.demo.entity.BestProcessingTime;
import com.example.demo.entity.DurationUnit;
import com.example.demo.repository.DataSource;

@Repository
public class ProcessingTimeRepository {
    @Autowired 
    private final DataSource dataSource;

    public ProcessingTimeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<BestProcessingTime> getBestProcessingTimes(Long dishId, Integer top, DurationUnit durationUnit) {
        List<BestProcessingTime> processingTimes = new ArrayList<>();

        String query = "SELECT sales_point, dish_name, preparation_duration, duration_unit " +
                       "FROM processing_time " +
                       "WHERE id_dish = ? " +
                       "ORDER BY preparation_duration ASC " +
                       (top != null ? "LIMIT ?" : "");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setLong(1, dishId);
            if (top != null) {
                statement.setInt(2, top);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                BestProcessingTime processingTime = new BestProcessingTime();
                processingTime.setSalesPoint(resultSet.getString("sales_point"));
                processingTime.setDish(resultSet.getString("dish_name"));
                
                double duration = resultSet.getDouble("preparation_duration");
                DurationUnit unit = DurationUnit.valueOf(resultSet.getString("duration_unit"));
                
                if (durationUnit != null && unit != durationUnit) {
                    duration = convertDuration(duration, unit, durationUnit);
                    unit = durationUnit;
                }
                
                processingTime.setPreparationDuration(duration);
                processingTime.setDurationUnit(unit);
                
                processingTimes.add(processingTime);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return processingTimes;
    }

    public void saveProcessingTime(List<BestProcessingTime> processingTimes) {
        String query = "INSERT INTO processing_time (updatedat, sales_point, id_dish, dish_name, preparation_duration, duration_unit) " +
                       "VALUES (?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (id) DO UPDATE SET " +
                       "updatedat = EXCLUDED.updatedat, " +
                       "sales_point = EXCLUDED.sales_point, " +
                       "dish_name = EXCLUDED.dish_name, " +
                       "preparation_duration = EXCLUDED.preparation_duration, " +
                       "duration_unit = EXCLUDED.duration_unit";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            for (BestProcessingTime processingTime : processingTimes) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(2, processingTime.getSalesPoint());
                statement.setLong(3, processingTime.getDishId());
                statement.setString(4, processingTime.getDish());
                statement.setDouble(5, processingTime.getPreparationDuration());
                statement.setString(6, processingTime.getDurationUnit().name());
                
                statement.addBatch();
            }
            
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private double convertDuration(double duration, DurationUnit from, DurationUnit to) {
        if (from == to) return duration;
        
        double seconds;
        switch (from) {
            case MINUTES:
                seconds = duration * 60;
                break;
            case HOUR:
                seconds = duration * 3600;
                break;
            default:
                seconds = duration;
        }
        
        switch (to) {
            case MINUTES:
                return seconds / 60;
            case HOUR:
                return seconds / 3600;
            default:
                return seconds;
        }
    }
}

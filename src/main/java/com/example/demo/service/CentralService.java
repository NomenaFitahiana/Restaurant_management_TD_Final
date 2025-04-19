package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AllProcessingTime;
import com.example.demo.entity.BestProcessingTime;
import com.example.demo.entity.BestSale;
import com.example.demo.entity.CalculationMode;
import com.example.demo.entity.DurationUnit;
import com.example.demo.entity.Sale;
import com.example.demo.repository.operations.CentralRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CentralService {
    private CentralRepository centralRepository;
    private static final String BASE_URL_8081 = "http://localhost:8081";
    private static final String BASE_URL_8082 = "http://localhost:8082";

private static final String PROCESSING_TIME_URL_8081 = "/dishes/processing-times";
private static final String PROCESSING_TIME_URL_8082 = "/dishes/processing-times";

    private final HttpClient httpClient;

    public CentralService(CentralRepository centralRepository){
        this.centralRepository = centralRepository;
        this.httpClient = HttpClient.newBuilder()
                          .version(HttpClient.Version.HTTP_2)
                          .connectTimeout(Duration.ofSeconds(10))
                          .build();
    }

    private String fetchApi(String fullUrl) {  
        HttpRequest request = HttpRequest.newBuilder()
                              .GET()
                              .uri(URI.create(fullUrl))
                              .header("X-API-KEY", System.getenv("API_KEY") )
                              .build();
    
    
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Failed with HTTP error code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while calling the external API: " + e.getMessage());
        }
    }

    public String fetchProcessingTimesFrom8081() {
        return fetchApi(BASE_URL_8081 + PROCESSING_TIME_URL_8081);
    }
    
    public String fetchProcessingTimesFrom8082() {
        return fetchApi(BASE_URL_8082 + PROCESSING_TIME_URL_8082);
    }

    public List<BestProcessingTime> convertToProcessingTimes(String processingTimesJson) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(processingTimesJson);
        
        List<BestProcessingTime> processingTimes = new ArrayList<>();
        
        for (JsonNode node : rootNode) {
            BestProcessingTime processingTime = new BestProcessingTime();
            processingTime.setDishId(node.path("dishId").asLong());
            processingTime.setDish(node.path("dishName").asText());
            
            double processingTimeValue = node.path("processingTime").asDouble();
            DurationUnit unit = DurationUnit.valueOf(node.path("durationFormat").asText());
            
        
            if (unit != DurationUnit.SECONDS) {
                processingTimeValue = convertDuration(processingTimeValue, unit, DurationUnit.SECONDS);
                unit = DurationUnit.SECONDS;
            }
            
            processingTime.setPreparationDuration(processingTimeValue);
            processingTime.setDurationUnit(unit);
            
            processingTimes.add(processingTime);
        }
        
        return processingTimes;
    } catch (Exception e) {
        throw new RuntimeException("Failed to convert processing times data", e);
    }
}

private double convertDuration(double duration, DurationUnit from, DurationUnit to) {
    if (from == to) return duration;
    
    double seconds;
    switch (from) {
        case MINUTES: seconds = duration * 60; break;
        case HOUR: seconds = duration * 3600; break;
        default: seconds = duration;
    }
    
    switch (to) {
        case MINUTES: return seconds / 60;
        case HOUR: return seconds / 3600;
        default: return seconds;
    }
}

    public String fetchOrdersSalesFrom8081() {
        return fetchApi(BASE_URL_8081 + "/orders/sales?top="+Integer.MAX_VALUE);
    }

    public String fetchOrdersSalesFrom8082() {
        return fetchApi(BASE_URL_8082 + "/orders/sales?top="+Integer.MAX_VALUE);
    }

    public BestSale fetchAndSaveFromBothApis() {
        String jsonFrom8081 = fetchOrdersSalesFrom8081();
        List<Sale> salesFrom8081 = convertToSales(jsonFrom8081);
        
        String jsonFrom8082 = fetchOrdersSalesFrom8082();
        List<Sale> salesFrom8082 = convertToSales(jsonFrom8082);
        
        List<Sale> allSales = new ArrayList<>();
        salesFrom8081.forEach(sale -> sale.setSalesPoint("Antanimena"));
        salesFrom8082.forEach(sale -> sale.setSalesPoint("Analamahitsy"));

        allSales.addAll(salesFrom8081);
        allSales.addAll(salesFrom8082);
        
        return saveAll(allSales);
    }

   /* public AllProcessingTime getAllBestProcessingTime(Long dishId, Integer top, DurationUnit durationUnit, CalculationMode calculationMode) {
    String jsonFrom8081 = fetchProcessingTimesFrom8081();
    List<BestProcessingTime> timesFrom8081 = convertToProcessingTimes(jsonFrom8081);
    
    String jsonFrom8082 = fetchProcessingTimesFrom8082();
    List<BestProcessingTime> timesFrom8082 = convertToProcessingTimes(jsonFrom8082);
    
    timesFrom8081.forEach(time -> time.setSalesPoint("Antanimena"));
    timesFrom8082.forEach(time -> time.setSalesPoint("Analamahitsy"));
    
    List<BestProcessingTime> allTimes = new ArrayList<>();
    allTimes.addAll(timesFrom8081);
    allTimes.addAll(timesFrom8082);
    
    List<BestProcessingTime> filteredTimes = allTimes.stream()
            .filter(time -> dishId == null || time.getDishId().equals(dishId))
            .toList();
    
   
    List<Double> bestTimes = new ArrayList<>();


    AllProcessingTime response = new AllProcessingTime();
    response.setUpdatedAt(LocalDateTime.now());
    response.setBestProcessingTimes(bestTimes);
    
    return response;
}*/

    public List<Sale> convertToSales(String ordersJson) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(ordersJson);
        
        List<Sale> sales = new ArrayList<>();
        
        for (JsonNode node : rootNode) {
            Sale sale = new Sale();
            sale.setId(node.path("id").asLong());
            sale.setDish(node.path("dishName").asText());
            sale.setQuantitySold(node.path("quantity").asInt());
            sale.setTotalAmount(node.path("amountTotal").asDouble());
            
            sales.add(sale);
        }
        
        return sales;
    } catch (Exception e) {
        throw new RuntimeException("Failed to convert orders data to Sales", e);
    }
}

    public BestSale getAllBestSales(Integer top){        
        return centralRepository.getAllBestSales(top);  
    }

    public BestSale saveAll(List<Sale> sale){
        BestSale sales = centralRepository.saveAll(sale);
        return sales;
    }

    
}

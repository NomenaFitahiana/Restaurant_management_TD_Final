package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CentralService {
    private CentralRepository centralRepository;
    private static final String BASE_URL_8081 = "http://localhost:8081";
    private static final String BASE_URL_8082 = "http://localhost:8082";
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
                              .header("Accept", "application/json")
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

    public String fetchOrdersSalesFrom8081() {
        return fetchApi(BASE_URL_8081 + "/orders/sales");
    }

    public String fetchOrdersSalesFrom8082() {
        return fetchApi(BASE_URL_8082 + "/orders/sales");
    }

    public BestSale fetchAndSaveFromBothApis() {
        String jsonFrom8081 = fetchOrdersSalesFrom8081();
        List<Sale> salesFrom8081 = convertToSales(jsonFrom8081);
        
        String jsonFrom8082 = fetchOrdersSalesFrom8082();
        List<Sale> salesFrom8082 = convertToSales(jsonFrom8082);
        
        List<Sale> allSales = new ArrayList<>();
        allSales.addAll(salesFrom8081);
        allSales.addAll(salesFrom8082);
        
        return saveAll(allSales);
    }

    public List<Sale> convertToSales(String ordersJson) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(ordersJson);
        
        List<Sale> sales = new ArrayList<>();
        
        for (JsonNode node : rootNode) {
            Sale sale = new Sale();
            sale.setId(node.path("id").asLong());
            sale.setDish(node.path("dish").asText());
            sale.setQuantitySold(node.path("quantitySold").asInt());
            sale.setTotalAmount(node.path("totalAmount").asDouble());
            sale.setSalesPoint("DEFAULT_POINT"); 
            
            sales.add(sale);
        }
        
        return sales;
    } catch (Exception e) {
        throw new RuntimeException("Failed to convert orders data to Sales", e);
    }
}

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

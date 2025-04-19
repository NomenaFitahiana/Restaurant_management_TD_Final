package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CentralRepository centralRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL_8081 = "http://localhost:8081";
    private static final String BASE_URL_8082 = "http://localhost:8082";

    private static final String PROCESSING_TIME_URL_8081 = "/orders/dishes/";
    private static final String PROCESSING_TIME_URL_8082 = "/orders/dishes/";

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
        return centralRepository.saveAll(sale);
    }

    public String fetchProcessingTimesFrom8081(Long id) {
        return fetchApi(BASE_URL_8081 + PROCESSING_TIME_URL_8081 + id + "/processingTime");
    }

    public String fetchProcessingTimesFrom8082(Long id) {
        return fetchApi(BASE_URL_8082 + PROCESSING_TIME_URL_8082 + id + "/processingTime");
    }

    public AllProcessingTime getAllBestProcessingTime(Long id, Integer top, DurationUnit durationUnit, CalculationMode calculationMode) {
        String response8081 = fetchProcessingTimesFrom8081(id);
        List<BestProcessingTime> timesFrom8081 = convertToBestProcessingTimeList(response8081, "Antanimena", durationUnit, calculationMode);

        String response8082 = fetchProcessingTimesFrom8082(id);
        List<BestProcessingTime> timesFrom8082 = convertToBestProcessingTimeList(response8082, "Analamahitsy", durationUnit, calculationMode);

        List<BestProcessingTime> allTimes = new ArrayList<>();
        allTimes.addAll(timesFrom8081);
        allTimes.addAll(timesFrom8082);
        AllProcessingTime allProcessingTime = new AllProcessingTime();
        allProcessingTime.setUpdatedAt(LocalDateTime.now());
        if(calculationMode.equals(CalculationMode.MINIMUM)){
            allProcessingTime.setBestProcessingTimes(allTimes.stream().sorted(Comparator.comparingDouble(BestProcessingTime::getDuration)).limit(top).collect(Collectors.toList()));
        }else if(calculationMode.equals(CalculationMode.MAXIMUM)){
            allProcessingTime.setBestProcessingTimes(allTimes.stream().sorted(Comparator.comparingDouble(BestProcessingTime::getDuration).reversed()).limit(top).collect(Collectors.toList()));
        }else if (calculationMode.equals(CalculationMode.AVERAGE)) {
            BestProcessingTime avg8081 = averageOf(timesFrom8081);
            BestProcessingTime avg8082 = averageOf(timesFrom8082);

            List<BestProcessingTime> averages = new ArrayList<>();
            if(avg8081!=null) averages.add(avg8081);
            if(avg8082!=null) averages.add(avg8082);

            allProcessingTime.setBestProcessingTimes(averages);
        }

        return allProcessingTime;
    }

    private BestProcessingTime averageOf(List<BestProcessingTime> list) {
        if (list.isEmpty()) return null;
        BestProcessingTime reference = list.getFirst();
        double averageDuration = list.stream()
                .mapToDouble(BestProcessingTime::getDuration)
                .average()
                .orElse(0.0);
        BestProcessingTime result = new BestProcessingTime();
        result.setSalesPoint(reference.getSalesPoint());
        result.setDishName(reference.getDishName());
        result.setDuration(averageDuration);
        result.setDurationUnit(reference.getDurationUnit());
        result.setCalculationMode(reference.getCalculationMode());

        return result;
    }


    public List<BestProcessingTime> convertToBestProcessingTimeList(String json, String salesPoint, DurationUnit durationUnit, CalculationMode calculationMode) {
        {
            List<BestProcessingTime> result = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(json);
                String dishName = root.get("dishName").asText();
                JsonNode processingTimeArray = root.get("processingTime");
                for (JsonNode durationNode : processingTimeArray) {
                    BestProcessingTime bpt = new BestProcessingTime();
                    bpt.setSalesPoint(salesPoint);
                    bpt.setDishName(dishName);
                    bpt.setDuration(convertDuration(durationNode.asDouble(), durationUnit));
                    bpt.setDurationUnit(durationUnit);
                    bpt.setCalculationMode(calculationMode);
                    result.add(bpt);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert processing times data to BestProcessingTime", e);
            }

            return result;
        }
    }

    private double convertDuration ( double duration, DurationUnit durationUnit){
        return switch (durationUnit) {
            case MINUTES -> duration / 60;
            case HOUR -> duration / 3600;
            default -> duration;
        };
    }
}

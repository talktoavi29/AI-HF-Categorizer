package com.ai.hf_categorizer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class ClassificationService {

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    private final List<String> CATEGORIES = List.of(
            "Medical", "Finance", "Legal",
            "Bills and Utilities", "Education",
            "Employment", "Government and ID",
            "Insurance", "Travel", "Notices and Letters"
    );

    public Map<String, Object> classify(String text) {
        try {
            // 1. Build request body
            Map<String, Object> payload = Map.of(
                    "inputs", text,
                    "parameters", Map.of("candidate_labels", CATEGORIES)
            );

            // 2. Call HF API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HF API Response: " + response.body());

            // 3. Parse response
            JsonNode root = mapper.readTree(response.body());

// Array is already sorted best → worst
            String topCategory = root.get(0).get("label").asText();
            double topScore = root.get(0).get("score").asDouble();

            Map<String, Double> allScores = new LinkedHashMap<>();
            for (JsonNode node : root) {
                allScores.put(node.get("label").asText(), node.get("score").asDouble());
            }

            return Map.of(
                    "category", topCategory,
                    "confidence", topScore,
                    "allScores", allScores
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Classification failed");
        }
    }
}
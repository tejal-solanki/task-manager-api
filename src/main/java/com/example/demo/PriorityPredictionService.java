package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
public class PriorityPredictionService {

    @Value("${huggingface.api.token}")
    private String apiToken;

    private static final String MODEL_URL =
        "https://router.huggingface.co/hf-inference/models/facebook/bart-large-mnli";

    //     private static final String MODEL_URL =
    // "https://router.huggingface.co/BROKEN/models/facebook/bart-large-mnli";

    private final ObjectMapper mapper = new ObjectMapper();

    @CircuitBreaker(name = "huggingface", fallbackMethod = "fallbackPriority")
    @Retry(name = "huggingface")
    public String predictPriority(String title, String description) {
        try {
            URL url = new URL(MODEL_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setDoOutput(true);

            Map<String, Object> body = Map.of(
                "inputs", title + " " + description,
                "parameters", Map.of(
                    "candidate_labels", List.of("high priority", "medium priority", "low priority")
                )
            );

            String jsonBody = mapper.writeValueAsString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int status = conn.getResponseCode();
            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new String(is.readAllBytes());

            System.out.println("[AI] HuggingFace response (" + status + "): " + responseBody);

            if (status == 200) {
                List<Map<String, Object>> results = mapper.readValue(responseBody, List.class);
                if (results != null && !results.isEmpty()) {
                    String topLabel = (String) results.get(0).get("label");
                    System.out.println("[AI] Top label: " + topLabel);
                    if (topLabel.contains("high")) return "HIGH";
                    if (topLabel.contains("low")) return "LOW";
                    return "MEDIUM";
                }
            }
            throw new RuntimeException("HuggingFace returned status: " + status);

        } catch (Exception e) {
            System.out.println("[AI] Prediction attempt failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Fallback — called when circuit is open or all retries exhausted
    public String fallbackPriority(String title, String description, Exception e) {
        System.out.println("[AI] Fallback triggered — defaulting to MEDIUM. Reason: " + e.getMessage());
        return "MEDIUM";
    }
}
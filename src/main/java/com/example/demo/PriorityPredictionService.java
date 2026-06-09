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

    private static final String MODEL_URL = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-mnli";

    // private static final String MODEL_URL =
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
                            "candidate_labels", List.of("high priority", "medium priority", "low priority")));

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
                    if (topLabel.contains("high"))
                        return "HIGH";
                    if (topLabel.contains("low"))
                        return "LOW";
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

    private static final String CHAT_URL = "https://router.huggingface.co/v1/chat/completions";

    public String suggestDescription(String title) {
        try {
            URL url = new URL(CHAT_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setDoOutput(true);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content",
                    "Write a single short sentence describing this task for a project management tool. Task: "
                            + title.replace("\"", "'") + ". Reply with only the description sentence.");

            Map<String, Object> body = Map.of(
                    "model", "meta-llama/Llama-3.2-3B-Instruct:featherless-ai",
                    "messages", List.of(message),
                    "max_tokens", 60);

            String jsonBody = mapper.writeValueAsString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int status = conn.getResponseCode();
            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new String(is.readAllBytes());

            System.out.println("[AI] Suggest response (" + status + "): " + responseBody);

            if (status == 200) {
                Map<String, Object> result = mapper.readValue(responseBody, Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                    if (msg != null) {
                        String content = (String) msg.get("content");
                        if (content != null)
                            return content.trim();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[AI] Suggest failed: " + e.getMessage());
        }
        return "";
    }

    private static final String EMBEDDING_URL = "https://router.huggingface.co/hf-inference/models/intfloat/multilingual-e5-large";

    public double[] generateEmbedding(String text) {
        try {
            URL url = new URL(EMBEDDING_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setDoOutput(true);

            Map<String, Object> body = Map.of("inputs", List.of(text));
            String jsonBody = mapper.writeValueAsString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int status = conn.getResponseCode();
            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new String(is.readAllBytes());

            System.out.println("[AI] Embedding response (" + status + "): "
                    + responseBody.substring(0, Math.min(100, responseBody.length())));

            if (status == 200) {
                // Returns nested array [[embedding values]]
                List<List<Double>> outer = mapper.readValue(responseBody, List.class);
                if (outer != null && !outer.isEmpty()) {
                    List<Double> vector = (List<Double>) outer.get(0);
                    double[] result = new double[vector.size()];
                    for (int i = 0; i < vector.size(); i++) {
                        result[i] = ((Number) vector.get(i)).floatValue();
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            System.out.println("[AI] Embedding failed: " + e.getMessage());
        }
        return null;
    }
}
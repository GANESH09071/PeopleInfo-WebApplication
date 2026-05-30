package com.peopleinfo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NlpUrgencyService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Determines if the leave reason is urgent.
     * Uses Gemini LLM API if a key is provided, otherwise falls back to local keyword-based rules.
     */
    public boolean isUrgent(String reason) {
        if (reason == null || reason.isBlank()) {
            return false;
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                return callGeminiApi(reason);
            } catch (Exception e) {
                log.warn("Gemini API call failed, falling back to local keyword classification: {}", e.getMessage());
            }
        } else {
            log.info("No Gemini API key configured. Using local keyword fallback.");
        }

        return checkLocalKeywords(reason);
    }

    private boolean callGeminiApi(String reason) throws Exception {
        String prompt = "You are an HR Assistant. Analyze the following leave request reason and determine if it indicates an urgent situation (e.g. medical emergency, family emergency, accident, funeral, or critical personal crisis).\n" +
                "Respond with exactly one word: 'true' if it is urgent, or 'false' if it is a routine or non-urgent request. Do not include any other text or punctuation.\n\n" +
                "Reason: \"" + reason.replace("\"", "\\\"") + "\"";

        // Construct JSON payload
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> contents = Map.of("contents", List.of(parts));
        
        String jsonPayload = objectMapper.writeValueAsString(contents);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                String text = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText()
                        .trim();
                
                log.info("Gemini classified reason as urgent: '{}'. Reason: '{}'", text, reason);
                return Boolean.parseBoolean(text.toLowerCase());
            }
        } else {
            throw new RuntimeException("Gemini API returned status code " + response.statusCode() + ": " + response.body());
        }

        return false;
    }

    private boolean checkLocalKeywords(String reason) {
        String lower = reason.toLowerCase();
        
        // 1. If it explicitly contains routine, checkup, or appointment, it is likely not urgent
        if (lower.contains("routine") || lower.contains("checkup") || lower.contains("appointment") || lower.contains("appt")) {
            if (!lower.contains("emergency") || lower.contains("no emergency") || lower.contains("not an emergency")) {
                return false;
            }
        }
        
        // 2. Check for explicit negative context
        if (lower.contains("no emergency") || lower.contains("not an emergency") || lower.contains("not a emergency") || lower.contains("no urgency")) {
            return false;
        }

        List<String> urgentKeywords = List.of(
            "emergency", "hospital", "surgery", "accident", "funeral", "death", 
            "critical", "medical", "admitted", "casualty", "injury", "sick", "illness"
        );
        for (String keyword : urgentKeywords) {
            if (lower.contains(keyword)) {
                // Negation check
                if (lower.contains("not an " + keyword) || lower.contains("no " + keyword) || lower.contains("not a " + keyword)) {
                    continue;
                }
                log.info("Local keyword classification matched keyword '{}' as urgent. Reason: '{}'", keyword, reason);
                return true;
            }
        }
        return false;
    }
}

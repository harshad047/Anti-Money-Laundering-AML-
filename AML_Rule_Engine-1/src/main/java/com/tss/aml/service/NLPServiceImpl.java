// com.tss.aml.service.NLPServiceImpl.java

package com.tss.aml.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

@Service
public class NLPServiceImpl implements NLPService {

    private static final String NLP_API_URL = "https://episodically-unfaceable-selah.ngrok-free.dev/analyze";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NLPServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> analyzeText(String text) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("nlpScore", 0);
        fallback.put("nlpFlags", Collections.emptyList());
        fallback.put("tokens", Collections.emptyList());
        fallback.put("summary", "");

        if (text == null || text.trim().isEmpty()) {
            return fallback;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(NLP_API_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());

                int score = root.has("nlp_risk_score") ? root.get("nlp_risk_score").asInt() : 0;
                JsonNode flagsNode = root.get("nlp_flags");
                List<String> flags = new ArrayList<>();

                if (flagsNode != null && flagsNode.isArray()) {
                    for (JsonNode flag : flagsNode) {
                        flags.add(flag.asText());
                    }
                }

                String summary = root.has("summary") ? root.get("summary").asText() : "";

                Map<String, Object> result = new HashMap<>();
                result.put("nlpScore", score);
                result.put("nlpFlags", flags);
                result.put("summary", summary);
                return result;
            }

        } catch (HttpClientErrorException | ResourceAccessException e) {
            System.err.println("NLP Service Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected NLP Error: " + e.getMessage());
        }

        return fallback;
    }
}
package com.tss.aml.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Service
public class ReCaptchaService {

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${recaptcha.url}")
    private String recaptchaUrl;

    private final WebClient webClient;

    public ReCaptchaService() {
        this.webClient = WebClient.builder().build();
    }
    public boolean verifyRecaptcha(String recaptchaResponse) {
        try {
            ReCaptchaResponse response = webClient.post()
                    .uri(recaptchaUrl)
                    // CORRECTED: Send data as URL-encoded form data
                    .body(BodyInserters.fromFormData("secret", recaptchaSecret)
                                       .with("response", recaptchaResponse))
                    .retrieve()
                    .bodyToMono(ReCaptchaResponse.class)
                    .block();

            return response != null && response.isSuccess();
        } catch (Exception e) {
            // Logging the error is crucial here!
            // log.error("Error verifying reCAPTCHA", e);
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReCaptchaRequest {
        private String secret;
        private String response;
    }

    @Data
    public static class ReCaptchaResponse {
        private boolean success;
        private String challenge_ts;
        private String hostname;
        @JsonProperty("error-codes")
        private String[] errorCodes;
    }
}

package com.tss.captchaTest.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecaptchaController {

    // Replace with your secret key (server-side)
    private static final String SECRET_KEY = "6LcRxtkrAAAAADuYyOdkE7-agvG_olqN-mUP0BO7"; 

    @PostMapping("/verify")
    public String verifyCaptcha(@RequestParam("g-recaptcha-response") String gRecaptchaResponse,
                                Model model) {

        boolean captchaVerified = false;
        try {
            captchaVerified = verifyRecaptcha(gRecaptchaResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (captchaVerified) {
            model.addAttribute("message", "Captcha Verified Successfully!");
        } else {
            model.addAttribute("message", "Captcha Verification Failed!");
        }

        return "result";
    }

    private boolean verifyRecaptcha(String gRecaptchaResponse) throws Exception {
        if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty()) {
            return false;
        }

        String url = "https://www.google.com/recaptcha/api/siteverify";
        String params = "secret=" + SECRET_KEY + "&response=" + gRecaptchaResponse;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(params.getBytes());
            os.flush();
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        JSONObject json = new JSONObject(response.toString());
        return json.getBoolean("success");
    }
    
    @GetMapping("/login")
    public String showLogin() {
        return "login"; // Returns src/main/resources/templates/login.html
    }
}

package com.tss.aml.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank @Size(max=100) private String firstName;
    @Size(max=100) private String middleName;
    @NotBlank @Size(max=100) private String lastName;

    @NotBlank @Email private String email;
    @Size(max=20) private String phone;


    @NotBlank
    @Size(min=8, max=100)
    private String password;

    private String role = "CUSTOMER";

    @Size(max=200) private String street;
    @Size(max=100) private String city;
    @Size(max=100) private String state;
    @Size(max=100) private String country;
    @Size(max=20) private String postalCode;

    @NotBlank private String recaptchaToken; 
}

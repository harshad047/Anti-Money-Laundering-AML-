package com.tss.aml.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginInitRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    

}



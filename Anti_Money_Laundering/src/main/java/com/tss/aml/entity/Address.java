package com.tss.aml.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Embeddable
public class Address {

    @Size(max=200)
    @Column(name="street")
    private String street;

    @Size(max=100)
    @Column(name="city")
    private String city;

    @Size(max=100)
    @Column(name="state")
    private String state;

    @Size(max=100)
    @Column(name="country")
    private String country;

    @Size(max=20)
    @Column(name="postal_code")
    private String postalCode;

    // getters/setters
}

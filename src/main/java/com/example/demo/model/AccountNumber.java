package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountNumber {

    @JsonProperty("number")
    private String number;

    @JsonProperty("sort_code")
    private String sortCode;

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getSortCode() { return sortCode; }
    public void setSortCode(String sortCode) { this.sortCode = sortCode; }
}

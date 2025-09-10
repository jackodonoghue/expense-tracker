package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Root response wrapper for accounts API
public class AccountsResponse {

    @JsonProperty("results")
    private List<AccountDto> results;

    public List<AccountDto> getResults() {
        return results;
    }

    public void setResults(List<AccountDto> results) {
        this.results = results;
    }
}


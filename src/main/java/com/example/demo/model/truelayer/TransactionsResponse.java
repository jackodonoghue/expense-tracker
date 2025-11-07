package com.example.demo.model.truelayer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TransactionsResponse {
    @JsonProperty("results")
    private List<Transaction> results;

    public List<Transaction> getResults() {
        return results;
    }

    public void setResults(List<Transaction> results) {
        this.results = results;
    }
}
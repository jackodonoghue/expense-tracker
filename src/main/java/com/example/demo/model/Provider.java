package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Provider {
    @JsonProperty("display_name")
    private String displayName;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}

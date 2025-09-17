package com.example.demo.dto;

public class UserDTO {
    private String fullName;

    public UserDTO() {}

    public UserDTO(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// Account data transfer object matching JSON structure
public class AccountDto {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("account_number")
    private AccountNumber accountNumber;

    @JsonProperty("provider")
    private Provider provider;

    // Getters and setters omitted for brevity, include standard ones

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public AccountNumber getAccountNumber() { return accountNumber; }
    public void setAccountNumber(AccountNumber accountNumber) { this.accountNumber = accountNumber; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
}
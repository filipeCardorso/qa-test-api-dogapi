package com.qatest.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BreedListResponse {

    private Map<String, List<String>> message;
    private String status;

    public Map<String, List<String>> getMessage() { return message; }
    public void setMessage(Map<String, List<String>> message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.qatest.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BreedImagesResponse {

    private List<String> message;
    private String status;

    public List<String> getMessage() { return message; }
    public void setMessage(List<String> message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

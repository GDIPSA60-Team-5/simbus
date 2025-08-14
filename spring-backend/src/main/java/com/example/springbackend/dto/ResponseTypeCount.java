package com.example.springbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTypeCount {
    private String id; // This will be the responseType from MongoDB aggregation
    private long count;
    
    public String getResponseType() {
        return id;
    }
    
    public void setResponseType(String responseType) {
        this.id = responseType;
    }
}
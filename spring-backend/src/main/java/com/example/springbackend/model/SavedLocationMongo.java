package com.example.springbackend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;


@Document(collection = "saved_locations")
@Data
public class SavedLocationMongo {
    @Id
    private String id;

    private String userId;
    private String name;
    private String postalCode;

    public SavedLocationMongo(String userId, String name, String postalCode) {
        this.userId = userId;
        this.name = name;
        this.postalCode = postalCode;
    }

    public SavedLocationMongo() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

}
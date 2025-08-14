package com.example.springbackend.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.Instant;
import java.util.Date;
@Document("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	private String id;

	private String userName;
	private String userType = "user";
	private String passwordHash;

	@CreatedDate
	private Date createdAt;
	// keep them separate and reference by userId
}

package com.example.springbackend.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
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
	private String email;
	private String userType;
	private String passwordHash;
	private String fcmToken;

	@CreatedDate
	private Date createdAt;
}

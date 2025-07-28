package com.example.springbackend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springbackend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

}

package com.example.springbackend.repository;

import com.example.springbackend.model.CommutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommutePlanRepository extends JpaRepository<CommutePlan, Long> {
}

package com.example.springbackend.dto.llm;

import com.example.springbackend.model.CommutePlan;

public record CommutePlanResponseDTO(
    boolean creationSuccess,
    CommutePlan commutePlan
) implements BotResponseDTO {

    @Override
    public String getType() {
        return "commute-plan";
    }
}

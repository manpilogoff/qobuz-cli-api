package com.manpilogoff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractResponse {
    private final String status;

    protected AbstractResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

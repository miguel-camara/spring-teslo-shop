package com.teslo.shop.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class PaginationRequest {

    @Positive
    private Integer limit;

    @Min(0)
    private Integer offset;

    private String gender;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

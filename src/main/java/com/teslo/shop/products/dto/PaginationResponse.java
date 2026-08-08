package com.teslo.shop.products.dto;

import java.util.List;

public class PaginationResponse {

    private long count;
    private long pages;
    private List<ProductResponse> products;

    public PaginationResponse() {
    }

    public PaginationResponse(long count, long pages, List<ProductResponse> products) {
        this.count = count;
        this.pages = pages;
        this.products = products;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponse> products) {
        this.products = products;
    }
}

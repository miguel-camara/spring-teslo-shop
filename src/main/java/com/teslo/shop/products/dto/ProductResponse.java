package com.teslo.shop.products.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.teslo.shop.auth.dto.UserResponse;
import com.teslo.shop.products.entity.Product;
import java.util.List;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private String title;

    @JsonSerialize(using = WholeNumberDoubleSerializer.class)
    private Double price;

    private String description;
    private String slug;
    private Integer stock;
    private String[] sizes;
    private String gender;
    private String[] tags;
    private List<String> images;
    private UserResponse user;

    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setSlug(product.getSlug());
        response.setStock(product.getStock());
        response.setSizes(product.getSizes() != null ? product.getSizes().clone() : null);
        response.setGender(product.getGender());
        response.setTags(product.getTags() != null ? product.getTags().clone() : null);
        response.setImages(product.getImages().stream().map(img -> img.getUrl()).toList());
        if (product.getUser() != null) {
            response.setUser(UserResponse.from(product.getUser()));
        }
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String[] getSizes() {
        return sizes;
    }

    public void setSizes(String[] sizes) {
        this.sizes = sizes;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}

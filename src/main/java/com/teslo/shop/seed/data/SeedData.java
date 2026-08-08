package com.teslo.shop.seed.data;

import java.util.List;

public record SeedData(List<SeedUser> users, List<SeedProduct> products) {

    public record SeedUser(String email, String fullName, String password, String[] roles) {
    }

    public record SeedProduct(String title, double price, String description, String slug, int stock,
                              String[] sizes, String gender, String[] tags, String[] images) {
    }
}

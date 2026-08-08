package com.teslo.shop.products.controller;

import com.teslo.shop.auth.RoleGuard;
import com.teslo.shop.auth.entity.User;
import com.teslo.shop.common.dto.PaginationRequest;
import com.teslo.shop.products.dto.CreateProductRequest;
import com.teslo.shop.products.dto.PaginationResponse;
import com.teslo.shop.products.dto.ProductResponse;
import com.teslo.shop.products.dto.UpdateProductRequest;
import com.teslo.shop.products.service.ProductsService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal User user) {
        return productsService.create(request, user);
    }

    @GetMapping
    public PaginationResponse findAll(@Valid PaginationRequest pagination) {
        return productsService.findAll(pagination);
    }

    @GetMapping("/{term}")
    public ProductResponse findOne(@PathVariable String term) {
        return productsService.findOnePlain(term);
    }

    @PatchMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal User user) {
        RoleGuard.requireAny(user, "admin");
        return productsService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        RoleGuard.requireAny(user, "admin");
        productsService.remove(id);
        return ResponseEntity.ok().build();
    }
}

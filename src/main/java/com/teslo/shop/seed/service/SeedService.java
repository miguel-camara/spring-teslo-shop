package com.teslo.shop.seed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teslo.shop.auth.entity.User;
import com.teslo.shop.auth.repository.UserRepository;
import com.teslo.shop.products.dto.CreateProductRequest;
import com.teslo.shop.products.service.ProductsService;
import com.teslo.shop.seed.data.SeedData;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedService {

    private final ProductsService productsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public SeedService(ProductsService productsService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.productsService = productsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String runSeed() {
        deleteTables();
        User adminUser = insertUsers();
        insertNewProducts(adminUser);
        return "SEED EXECUTED";
    }

    @Transactional
    public void deleteTables() {
        productsService.deleteAllProducts();
        userRepository.deleteAll();
    }

    @Transactional
    public User insertUsers() {
        SeedData data = loadSeedData();
        User first = null;
        for (SeedData.SeedUser seedUser : data.users()) {
            User user = new User();
            user.setEmail(seedUser.email());
            user.setFullName(seedUser.fullName());
            user.setPassword(passwordEncoder.encode(seedUser.password()));
            user.setActive(true);
            user.setRoles(seedUser.roles());
            User saved = userRepository.save(user);
            if (first == null) {
                first = saved;
            }
        }
        return first;
    }

    @Transactional
    public void insertNewProducts(User user) {
        productsService.deleteAllProducts();

        SeedData data = loadSeedData();
        for (SeedData.SeedProduct seedProduct : data.products()) {
            productsService.create(toCreateRequest(seedProduct), user);
        }
    }

    private CreateProductRequest toCreateRequest(SeedData.SeedProduct seedProduct) {
        CreateProductRequest request = new CreateProductRequest();
        request.setTitle(seedProduct.title());
        request.setPrice(seedProduct.price());
        request.setDescription(seedProduct.description());
        request.setSlug(seedProduct.slug());
        request.setStock(seedProduct.stock());
        request.setSizes(seedProduct.sizes());
        request.setGender(seedProduct.gender());
        request.setTags(seedProduct.tags());
        request.setImages(seedProduct.images());
        return request;
    }

    private SeedData loadSeedData() {
        try (InputStream in = new ClassPathResource("seed-data.json").getInputStream()) {
            return objectMapper.readValue(in, SeedData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load seed-data.json", e);
        }
    }
}

package com.teslo.shop.products.service;

import com.teslo.shop.auth.entity.User;
import com.teslo.shop.auth.repository.UserRepository;
import com.teslo.shop.common.dto.PaginationRequest;
import com.teslo.shop.common.exception.ApiBadRequestException;
import com.teslo.shop.common.exception.ApiNotFoundException;
import com.teslo.shop.products.dto.CreateProductRequest;
import com.teslo.shop.products.dto.PaginationResponse;
import com.teslo.shop.products.dto.ProductResponse;
import com.teslo.shop.products.dto.UpdateProductRequest;
import com.teslo.shop.products.entity.Product;
import com.teslo.shop.products.entity.ProductImage;
import com.teslo.shop.products.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductsService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductsService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request, User user) {
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice() != null ? request.getPrice() : 0);
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setSizes(request.getSizes());
        product.setGender(request.getGender());
        product.setTags(request.getTags() != null ? request.getTags() : new String[0]);
        product.setUser(userRepository.getReferenceById(user.getId()));

        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setUrl(url);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }

        try {
            productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            throw new ApiBadRequestException(e.getMostSpecificCause().getMessage());
        }

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PaginationResponse findAll(PaginationRequest pagination) {
        int limit = pagination.getLimit() != null ? pagination.getLimit() : 10;
        int offset = pagination.getOffset() != null ? pagination.getOffset() : 0;
        String gender = pagination.getGender() != null ? pagination.getGender() : "";

        long total = productRepository.countFiltered(gender);
        List<Product> products = productRepository.findAllFiltered(gender, limit, offset);
        long pages = total == 0 ? 0 : (long) Math.ceil((double) total / limit);

        return new PaginationResponse(total, pages,
                products.stream().map(ProductResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public Product findOne(String term) {
        Optional<Product> product;
        if (isUuid(term)) {
            product = productRepository.findById(UUID.fromString(term));
        } else {
            product = productRepository.findOneByTerm(term);
        }
        return product.orElseThrow(() -> new ApiNotFoundException("Product with " + term + " not found"));
    }

    @Transactional(readOnly = true)
    public ProductResponse findOnePlain(String term) {
        return ProductResponse.from(findOne(term));
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request, User user) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiNotFoundException("Product with id: " + id + " not found"));

        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getSlug() != null) {
            product.setSlug(request.getSlug());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getSizes() != null) {
            product.setSizes(request.getSizes());
        }
        if (request.getGender() != null) {
            product.setGender(request.getGender());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }
        product.setUser(userRepository.getReferenceById(user.getId()));

        if (request.getImages() != null) {
            product.getImages().clear();
            for (String url : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setUrl(url);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }

        try {
            productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            throw new ApiBadRequestException(e.getMostSpecificCause().getMessage());
        }

        return findOnePlain(id.toString());
    }

    @Transactional
    public void remove(UUID id) {
        Product product = findOne(id.toString());
        productRepository.delete(product);
    }

    @Transactional
    public void deleteAllProducts() {
        productRepository.deleteAllProducts();
    }

    private boolean isUuid(String term) {
        try {
            UUID.fromString(term);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

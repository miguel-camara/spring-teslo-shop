package com.teslo.shop.products.repository;

import com.teslo.shop.products.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {

    @Modifying
    @Query("delete from Product p")
    void deleteAllProducts();
}

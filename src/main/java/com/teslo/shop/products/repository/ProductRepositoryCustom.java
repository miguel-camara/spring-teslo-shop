package com.teslo.shop.products.repository;

import com.teslo.shop.products.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryCustom {

    List<Product> findAllFiltered(String gender, int limit, int offset);

    long countFiltered(String gender);

    Optional<Product> findOneByTerm(String term);
}

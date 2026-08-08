package com.teslo.shop.products.repository;

import com.teslo.shop.products.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findAllFiltered(String gender, int limit, int offset) {
        TypedQuery<Product> query = entityManager.createQuery(
            "select p from Product p " +
                "where (:gender = '' or p.gender = :gender or p.gender = 'unisex') " +
                "order by p.id asc",
            Product.class
        );
        query.setParameter("gender", gender);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public long countFiltered(String gender) {
        TypedQuery<Long> query = entityManager.createQuery(
            "select count(p) from Product p " +
                "where (:gender = '' or p.gender = :gender or p.gender = 'unisex')",
            Long.class
        );
        query.setParameter("gender", gender);
        return query.getSingleResult();
    }

    @Override
    public Optional<Product> findOneByTerm(String term) {
        TypedQuery<Product> query = entityManager.createQuery(
            "select p from Product p " +
                "where upper(p.title) = upper(:term) or p.slug = lower(:term)",
            Product.class
        );
        query.setParameter("term", term);
        return query.getResultStream().findFirst();
    }
}

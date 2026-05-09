package com.eduardomango.pricetracker.product;

import com.eduardomango.pricetracker.product.domain.ProductEntity;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.math.BigDecimal;

public class ProductSpecification {

    //JPA Specification has recently moved away from null values
    // Instead we need to return a conjunction if the value is null

    public static PredicateSpecification<ProductEntity> priceGreaterThan(BigDecimal price) {
        return (root, cb) -> price == null
                ? cb.conjunction()
                : cb.greaterThan(root.get("currentPrice").get("amount"), price);
    }

    public static PredicateSpecification<ProductEntity> priceLesserThan(BigDecimal price) {
        return (root, cb) -> price == null
                ? cb.conjunction()
                : cb.lessThan(root.get("currentPrice").get("amount"), price);
    }

    public static PredicateSpecification<ProductEntity> nameContains(String name) {
        return (root, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static PredicateSpecification<ProductEntity> nameEquals(String name) {
        return (root, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static PredicateSpecification<ProductEntity> descriptionContains(String description) {
        return (root, cb) -> description == null || description.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

}


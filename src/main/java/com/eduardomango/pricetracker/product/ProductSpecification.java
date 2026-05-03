package com.eduardomango.pricetracker.product;

import com.eduardomango.pricetracker.product.domain.ProductEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    //JPA Specification has recently moved away from null values
    // Instead we need to return a conjunction if the value is null

    public static Specification<ProductEntity> priceGreaterThan(BigDecimal price) {
        return (root, query, cb) -> price == null
                ? cb.conjunction()
                : cb.greaterThan(root.get("currentPrice").get("amount"), price);
    }

    public static Specification<ProductEntity> priceLessThan(BigDecimal price) {
        return (root, query, cb) -> price == null
                ? cb.conjunction()
                : cb.lessThan(root.get("currentPrice").get("amount"), price);
    }

    public static Specification<ProductEntity> nameContains(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ProductEntity> nameEquals(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static Specification<ProductEntity> descriptionContains(String description) {
        return (root, query, cb) -> description == null || description.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

}

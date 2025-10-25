package com.carrental.repository.rental;

import com.carrental.model.Rental;
import org.springframework.data.jpa.domain.Specification;

public class RentalSpecification {

    public static Specification<Rental> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Rental> isActive(Boolean active) {
        if (active == null) {
            return null;
        }

        return (root, query, cb) -> {
            if (active) {
                return cb.isNull(root.get("actualReturnDate"));
            } else {
                return cb.isNotNull(root.get("actualReturnDate"));
            }
        };
    }
}


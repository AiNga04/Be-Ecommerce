package com.zyna.dev.ecommerce.users.repository;

import com.zyna.dev.ecommerce.users.criteria.UserCriteria;
import com.zyna.dev.ecommerce.users.models.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> fromCriteria(UserCriteria criteria, boolean isDeleted) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by deleted status
            if (isDeleted) {
                predicates.add(cb.isTrue(root.get("isDeleted")));
            } else {
                predicates.add(cb.isFalse(root.get("isDeleted")));
            }

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (StringUtils.hasText(criteria.getFirstName())) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getLastName())) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getEmail())) { // Added email search capability
                 predicates.add(cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getRole())) {
                // Join with roles
                predicates.add(cb.equal(root.join("roles").get("code"), criteria.getRole()));
            }

            if (StringUtils.hasText(criteria.getPhone())) {
                predicates.add(cb.like(root.get("phone"), "%" + criteria.getPhone() + "%"));
            }

            if (criteria.getDateOfBirth() != null) {
                predicates.add(cb.equal(root.get("dateOfBirth"), criteria.getDateOfBirth()));
            }

            if (criteria.getGender() != null) {
                predicates.add(cb.equal(root.get("gender"), criteria.getGender()));
            }

            if (criteria.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), criteria.getCity()));
            }
            
             if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

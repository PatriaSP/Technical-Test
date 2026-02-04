package com.patria.test.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.entity.Order;

@Service
public class OrderFilter {

    public Specification<Order> specification(OrderListRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(request.getFilter()) && !request.getFilter().isBlank()) {
                String filter = "%" + request.getFilter().toLowerCase() + "%";

                Predicate namePredicate = builder.like(
                        builder.lower(root.get("item").get("name")),
                        filter);

                Predicate idPredicate = builder.like(
                        builder.lower(root.get("orderNo")),
                        filter);

                predicates.add(builder.or(namePredicate, idPredicate));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

}

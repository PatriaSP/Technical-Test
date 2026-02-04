package com.patria.test.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.entity.Inventory;

@Service
public class InventoryFilter {

    public Specification<Inventory> specification(InventoryListRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(request.getFilter()) && !request.getFilter().isBlank()) {
                String filter = "%" + request.getFilter().toLowerCase() + "%";

                Predicate namePredicate = builder.like(
                        builder.lower(root.get("item").get("name")),
                        filter);

                Predicate typePredicate = builder.like(
                        builder.lower(root.get("type")),
                        filter);

                Predicate qtyPredicate = builder.like(
                        builder.lower(builder.function("str", String.class, root.get("qty"))),
                        filter);

                predicates.add(builder.or(namePredicate, qtyPredicate, typePredicate));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

}

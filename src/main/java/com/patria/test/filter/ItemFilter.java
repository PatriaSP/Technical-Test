package com.patria.test.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.entity.Item;

@Service
public class ItemFilter {

    public Specification<Item> specification(ItemListRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.nonNull(request.getFilter()) && !request.getFilter().isBlank()) {
                String filter = "%" + request.getFilter().toLowerCase() + "%";

                Predicate namePredicate = builder.like(
                        builder.lower(root.get("name")),
                        filter);

                Predicate pricePredicate = builder.like(
                        builder.lower(builder.function("str", String.class, root.get("price"))),
                        filter);

                predicates.add(builder.or(namePredicate, pricePredicate));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

}

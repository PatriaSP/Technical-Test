package com.patria.test.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.dto.response.PaginationResponse;
import com.patria.test.entity.Order;
import com.patria.test.exception.AppException;
import com.patria.test.serializer.OrderSerializer;
import com.patria.test.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/order")
@Tag(name = "Order Controller")
public class OrderController {

        private final OrderService orderService;
        private final OrderSerializer orderSerializer;

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<List<OrderResponse>> orders(
                        @RequestParam(required = false, defaultValue = "1") Integer page,
                        @RequestParam(required = false, defaultValue = "5") Integer perPage,
                        @RequestParam(required = false) String filter) {

                Page<Order> orders = orderService.list(OrderListRequest.builder()
                                .page(page)
                                .perPage(perPage)
                                .filter(filter)
                                .build());

                return AppResponse.<List<OrderResponse>>builder()
                                .success(true)
                                .data(orders.stream().map(order -> {
                                        try {
                                                return orderSerializer.serialize(order);
                                        } catch (Exception e) {
                                                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                                "Internal server error!", e);
                                        }
                                }).toList())
                                .pagination(PaginationResponse.builder()
                                                .currentPage(orders.getNumber() + 1)
                                                .totalPage(orders.getTotalPages())
                                                .perPage(orders.getSize())
                                                .total(orders.getTotalElements())
                                                .count(orders.getNumberOfElements())
                                                .hasNext(orders.hasNext())
                                                .hasPrevious(orders.hasPrevious())
                                                .hasContent(orders.hasContent())
                                                .build())
                                .message("Success get data")
                                .build();
        }

        @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<OrderResponse> order(@PathVariable String id) {
                return AppResponse.<OrderResponse>builder()
                .success(true)
                .data(orderService.get(id))
                .message("Success get data")
                .build();
        }

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<OrderResponse> save(@Valid @RequestBody OrderSaveRequest request) {
                return AppResponse.<OrderResponse>builder()
                    .success(true)
                    .data(orderService.save(request))
                    .message("Order created successfully")
                    .build();
        }

        @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<OrderResponse> edit(@Valid @RequestBody OrderEditRequest request) {
                return AppResponse.<OrderResponse>builder()
                    .success(true)
                    .data(orderService.edit(request))
                    .message("Order updated successfully")
                    .build();
        }

        @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<OrderResponse> delete(@PathVariable String id) {
                orderService.delete(id);
                return AppResponse.<OrderResponse>builder()
                    .success(true)
                    .message("Order deleted successfully")
                    .build();
        }
}

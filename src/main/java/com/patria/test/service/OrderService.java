package com.patria.test.service;

import org.springframework.data.domain.Page;

import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.entity.Order;

public interface OrderService {

    Page<Order> list(OrderListRequest request);

    OrderResponse get(String id);

    OrderResponse save(OrderSaveRequest request);

    OrderResponse edit(OrderEditRequest request);

    void delete(String id);

}

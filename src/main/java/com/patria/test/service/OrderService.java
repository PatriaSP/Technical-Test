package com.patria.test.service;

import java.util.List;

import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.OrderResponse;

public interface OrderService {

    AppResponse<List<OrderResponse>> list(OrderListRequest request);

    AppResponse<OrderResponse> get(String id);

    AppResponse<OrderResponse> save(OrderSaveRequest request);

    AppResponse<OrderResponse> edit(OrderEditRequest request);

    AppResponse<OrderResponse> delete(String id);

}

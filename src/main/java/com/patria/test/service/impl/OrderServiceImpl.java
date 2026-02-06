package com.patria.test.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.dto.response.PaginationResponse;
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.entity.Order;
import com.patria.test.exception.AppException;
import com.patria.test.filter.OrderFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.OrderRepository;
import com.patria.test.serializer.OrderSerializer;
import com.patria.test.service.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderSerializer orderSerializer;
    private final OrderFilter orderFilter;
    private final ItemServiceImpl itemServiceImpl;

    @Override
    public Page<Order> list(OrderListRequest request) {
        try {
            Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
            return orderRepository.findAll(orderFilter.specification(request),
                    pageable);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    public OrderResponse get(String id) {
        return orderRepository.findById(id)
                .map(order -> {
                    try {
                        return orderSerializer.serialize(order);
                    } catch (Exception e) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                    }
                })
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Order not found!"));
    }

    @Override
    @Transactional
    public OrderResponse save(OrderSaveRequest request) {
        try {

            Item item = itemServiceImpl.getItemById(request.getItemId());

            if (Boolean.FALSE.equals(isStockReady(item, request.getQty()))) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Stock is not available!");
            }

            Inventory inventory = Inventory.builder()
                    .item(item)
                    .qty(request.getQty())
                    .type(InventoryTypeEnum.W)
                    .build();

            inventoryRepository.save(inventory);

            Order order = Order.builder()
                    .orderNo(generateOrderNo())
                    .item(item)
                    .qty(request.getQty())
                    .price(item.getPrice())
                    .inventory(inventory)
                    .build();

            return orderSerializer.serialize(orderRepository.save(order));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public OrderResponse edit(OrderEditRequest request) {
        try {

            Item item = itemServiceImpl.getItemById(request.getItemId());

            if (Boolean.FALSE.equals(isStockReady(item, request.getQty()))) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Stock is not available!");
            }

            Order order = getOrderById(request.getOrderNo());

            order.setPrice(item.getPrice());
            order.setQty(request.getQty());

            if (!order.getItem().equals(item)) {
                order.setItem(item);

                inventoryRepository.delete(order.getInventory());

                Inventory newInventory = Inventory.builder()
                        .item(item)
                        .qty(request.getQty())
                        .type(InventoryTypeEnum.W)
                        .build();

                inventoryRepository.save(newInventory);
                order.setInventory(newInventory);
            }

            return orderSerializer.serialize(orderRepository.save(order));

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        try {
            Order order = getOrderById(id);

            orderRepository.delete(order);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    private Boolean isStockReady(Item item, Integer qty) {
        int stock = itemServiceImpl.getStockByItem(item);

        return stock >= qty;
    }

    private String generateOrderNo() {
        Long lastId = orderRepository.findTopByOrderByOrderNoDesc()
                .map(order -> {
                    String lastOrderNo = order.getOrderNo();
                    return Long.parseLong(lastOrderNo.replace("O", ""));
                })
                .orElse(0L);

        return "O" + (lastId + 1);
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Order not found!"));
    }
}

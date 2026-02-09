package com.patria.test.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.request.OrderListRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.entity.Order;
import com.patria.test.exception.AppException;
import com.patria.test.filter.OrderFilter;
import com.patria.test.repository.OrderRepository;
import com.patria.test.serializer.OrderSerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.service.ItemService;
import com.patria.test.service.OrderService;
import com.patria.test.util.StockUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSerializer orderSerializer;
    private final OrderFilter orderFilter;
    private final ItemService itemService;
    private final InventoryService inventoryService;

    @Override
    public Page<OrderResponse> list(OrderListRequest request) {
        Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
        return orderRepository.findAll(orderFilter.specification(request),
                pageable).map(order -> orderSerializer.serialize(order, StockUtil.getStockByItem(order.getItem())));
    }

    @Override
    public OrderResponse get(String id) {
        return orderRepository.findById(id)
                .map(order -> orderSerializer.serialize(order, StockUtil.getStockByItem(order.getItem())))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Order not found!"));
    }

    @Override
    @Transactional
    public OrderResponse save(OrderSaveRequest request) {
        Item item = itemService.getItemById(request.getItemId());

        if (Boolean.FALSE.equals(isStockReady(item, request.getQty()))) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Stock is not available!");
        }

        Inventory inventory = inventoryService.save(Inventory.builder()
                .item(item)
                .qty(request.getQty())
                .type(InventoryTypeEnum.W)
                .build());

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .item(item)
                .qty(request.getQty())
                .price(item.getPrice())
                .inventory(inventory)
                .build();

        return orderSerializer.serialize(orderRepository.save(order), StockUtil.getStockByItem(order.getItem()));
    }

    @Override
    @Transactional
    public OrderResponse edit(OrderEditRequest request) {
        Item item = itemService.getItemById(request.getItemId());

        if (Boolean.FALSE.equals(isStockReady(item, request.getQty()))) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Stock is not available!");
        }

        Order order = getOrderById(request.getOrderNo());

        order.setPrice(item.getPrice());
        order.setQty(request.getQty());

        if (!order.getItem().equals(item)) {
            order.setItem(item);

            inventoryService.delete(order.getInventory().getId());

            Inventory newInventory = Inventory.builder()
                    .item(item)
                    .qty(request.getQty())
                    .type(InventoryTypeEnum.W)
                    .build();

            inventoryService.save(newInventory);
            order.setInventory(newInventory);
        }

        return orderSerializer.serialize(orderRepository.save(order), StockUtil.getStockByItem(order.getItem()));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Order order = getOrderById(id);

        orderRepository.delete(order);
    }

    private Boolean isStockReady(Item item, Integer qty) {
        int stock = StockUtil.getStockByItem(item);

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

    @Override
    public void deleteAll(Iterable<Order> orders) {
        orderRepository.deleteAll(orders);
    }
}

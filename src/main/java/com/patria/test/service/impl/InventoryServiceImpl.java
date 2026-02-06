package com.patria.test.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.entity.Order;
import com.patria.test.exception.AppException;
import com.patria.test.filter.InventoryFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.OrderRepository;
import com.patria.test.serializer.InventorySerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.util.AESUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final InventorySerializer inventorySerializer;
    private final InventoryFilter inventoryFilter;
    private final ItemServiceImpl itemServiceImpl;

    @Override
    public Page<Inventory> list(InventoryListRequest request) {
        Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
        return inventoryRepository.findAll(inventoryFilter.specification(request),
                pageable);
    }

    @Override
    public InventoryResponse get(String id) {
        return Optional.ofNullable(getInventoryById(id))
                .map(item -> {
                    try {
                        return inventorySerializer.serialize(item);
                    } catch (Exception e) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                    }
                })
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!"));
    }

    @Override
    @Transactional
    public InventoryResponse save(InventorySaveRequest request) {
        try {
            Item item = itemServiceImpl.getItemById(request.getItemId());

            Inventory inventory = Inventory.builder()
                    .item(item)
                    .qty(request.getQty())
                    .type(request.getType())
                    .build();

            return inventorySerializer.serialize(inventoryRepository.save(inventory));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public InventoryResponse edit(InventoryEditRequest request) {
        try {
            Inventory inventory = getInventoryById(request.getId());

            Item item = itemServiceImpl.getItemById(request.getItemId());

            inventory.setItem(item);
            inventory.setQty(request.getQty());
            inventory.setType(request.getType());
            return inventorySerializer.serialize(inventoryRepository.save(inventory));
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
            Inventory inventory = getInventoryById(id);

            if(inventory.getOrder() != null) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Cannot delete inventory associated with an order!");
            }
            inventoryRepository.delete(inventory);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    public Inventory getInventoryById(String id) {
        if (id == null || id.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid inventory id!");
        }
        Long decodedId = AESUtil.getDecryptedString(id);
        if (decodedId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid inventory id!");
        }
        return inventoryRepository.findById(decodedId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!"));
    }

}

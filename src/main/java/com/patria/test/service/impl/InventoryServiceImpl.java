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
import com.patria.test.exception.AppException;
import com.patria.test.filter.InventoryFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.serializer.InventorySerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.service.ItemService;
import com.patria.test.util.AESUtil;
import com.patria.test.util.StockUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventorySerializer inventorySerializer;
    private final InventoryFilter inventoryFilter;
    private final ItemService itemService;

    @Override
    public Page<InventoryResponse> list(InventoryListRequest request) {
        Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
        return inventoryRepository.findAll(inventoryFilter.specification(request),
                pageable).map(inventory -> inventorySerializer.serialize(inventory, StockUtil.getStockByItem(inventory.getItem())));
    }

    @Override
    public InventoryResponse get(String id) {
        return Optional.ofNullable(getInventoryById(id))
                .map(inventory -> inventorySerializer.serialize(inventory, StockUtil.getStockByItem(inventory.getItem())))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!"));
    }

    @Override
    @Transactional
    public InventoryResponse save(InventorySaveRequest request) {
        Item item = itemService.getItemById(request.getItemId());

        Inventory inventory = Inventory.builder()
                .item(item)
                .qty(request.getQty())
                .type(request.getType())
                .build();

        return inventorySerializer.serialize(inventoryRepository.save(inventory), StockUtil.getStockByItem(inventory.getItem()));
    }

    @Override
    @Transactional
    public InventoryResponse edit(InventoryEditRequest request) {
        Inventory inventory = getInventoryById(request.getId());

        if (inventory.getOrder() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Cannot edit inventory associated with an order!");
        }

        Item item = itemService.getItemById(request.getItemId());

        inventory.setItem(item);
        inventory.setQty(request.getQty());
        inventory.setType(request.getType());
        return inventorySerializer.serialize(inventoryRepository.save(inventory), StockUtil.getStockByItem(inventory.getItem()));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Inventory inventory = getInventoryById(id);

        if (inventory.getOrder() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Cannot delete inventory associated with an order!");
        }
        inventoryRepository.delete(inventory);
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

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public void delete(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<Inventory> inventories) {
        inventoryRepository.deleteAll(inventories);
    }

}

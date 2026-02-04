package com.patria.test.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.dto.response.PaginationResponse;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.exception.AppException;
import com.patria.test.filter.InventoryFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.ItemRepository;
import com.patria.test.serializer.InventorySerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.util.AESUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventorySerializer inventorySerializer;
    private final InventoryFilter inventoryFilter;
    private final ItemServiceImpl itemServiceImpl;

    @Override
    public AppResponse<List<InventoryResponse>> list(InventoryListRequest request) {
        try {
            Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
            Page<Inventory> inventories = inventoryRepository.findAll(inventoryFilter.specification(request),
                    pageable);
            return AppResponse.<List<InventoryResponse>>builder()
                    .success(true)
                    .data(inventories.stream().map(item -> {
                        try {
                            return inventorySerializer.serialize(item);
                        } catch (Exception e) {
                            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                        }
                    }).toList())
                    .pagination(PaginationResponse.builder()
                            .currentPage(inventories.getNumber() + 1)
                            .totalPage(inventories.getTotalPages())
                            .perPage(inventories.getSize())
                            .total(inventories.getTotalElements())
                            .count(inventories.getNumberOfElements())
                            .hasNext(inventories.hasNext())
                            .hasPrevious(inventories.hasPrevious())
                            .hasContent(inventories.hasContent())
                            .build())
                    .message("Success get data")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    public AppResponse<InventoryResponse> get(String id) {
        if (id == null || id.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        Long decodedId = AESUtil.getDecryptedString(id);
        if (decodedId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        return AppResponse.<InventoryResponse>builder()
                .success(true)
                .data(inventoryRepository.findById(decodedId)
                        .map(item -> {
                            try {
                                return inventorySerializer.serialize(item);
                            } catch (Exception e) {
                                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                            }
                        })
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!")))
                .message("Success get data")
                .build();
    }

    @Override
    @Transactional
    public AppResponse<InventoryResponse> save(InventorySaveRequest request) {
        try {
            if (request.getItemId() == null || request.getItemId().isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
            }
            Long decodedId = AESUtil.getDecryptedString(request.getItemId());
            if (decodedId == null) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
            }

            Item item = itemRepository.findById(decodedId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"));

            Inventory inventory = Inventory.builder()
                    .item(item)
                    .qty(request.getQty())
                    .type(request.getType())
                    .build();

            Inventory savedInventory = inventoryRepository.save(inventory);
            InventoryResponse inventoryResponse = inventorySerializer.serialize(savedInventory);

            return AppResponse.<InventoryResponse>builder()
                    .success(true)
                    .data(inventoryResponse)
                    .message("Inventory saved successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public AppResponse<InventoryResponse> edit(InventoryEditRequest request) {
        try {
            Inventory inventory = getInventoryById(request.getId());

            Item item = itemServiceImpl.getItemById(request.getItemId());

            inventory.setItem(item);
            inventory.setQty(request.getQty());
            inventory.setType(request.getType());
            Inventory updatedInventory = inventoryRepository.save(inventory);
            InventoryResponse inventoryResponse = inventorySerializer.serialize(updatedInventory);
            return AppResponse.<InventoryResponse>builder()
                    .success(true)
                    .data(inventoryResponse)
                    .message("Inventory updated successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public AppResponse<InventoryResponse> delete(String id) {
        try {
            Inventory inventory = getInventoryById(id);

            inventoryRepository.delete(inventory);

            return AppResponse.<InventoryResponse>builder()
                    .success(true)
                    .message("Inventory deleted successfully")
                    .build();
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

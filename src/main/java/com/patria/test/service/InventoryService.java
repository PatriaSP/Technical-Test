package com.patria.test.service;

import org.springframework.data.domain.Page;

import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.entity.Inventory;

public interface InventoryService {

    Page<InventoryResponse> list(InventoryListRequest request);

    InventoryResponse get(String id);

    InventoryResponse save(InventorySaveRequest request);

    InventoryResponse edit(InventoryEditRequest request);

    void delete(String id);

    void delete(Long id);

    void deleteAll(Iterable<Inventory> inventories);

    Inventory save(Inventory inventory);
}

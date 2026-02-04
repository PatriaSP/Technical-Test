package com.patria.test.service;

import java.util.List;

import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.InventoryResponse;

public interface InventoryService {

    AppResponse<List<InventoryResponse>> list(InventoryListRequest request);

    AppResponse<InventoryResponse> get(String id);

    AppResponse<InventoryResponse> save(InventorySaveRequest request);

    AppResponse<InventoryResponse> edit(InventoryEditRequest request);

    AppResponse<InventoryResponse> delete(String id);

}

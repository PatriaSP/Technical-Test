package com.patria.test.service;

import java.util.List;

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.ItemResponse;

public interface ItemService {

    AppResponse<List<ItemResponse>> list(ItemListRequest request);

    AppResponse<ItemResponse> get(String id);

    AppResponse<ItemResponse> save(ItemSaveRequest request);

    AppResponse<ItemResponse> edit(ItemEditRequest request);

    AppResponse<ItemResponse> delete(String id);

}

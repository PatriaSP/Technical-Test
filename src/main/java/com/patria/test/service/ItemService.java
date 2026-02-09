package com.patria.test.service;

import org.springframework.data.domain.Page;

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.entity.Item;

public interface ItemService {

    Page<ItemResponse> list(ItemListRequest request);

    ItemResponse get(String id);

    ItemResponse save(ItemSaveRequest request);

    ItemResponse edit(ItemEditRequest request);

    void delete(String id);

    Item getItemById(String id);
}

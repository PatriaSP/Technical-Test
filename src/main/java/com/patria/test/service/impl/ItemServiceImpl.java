package com.patria.test.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.dto.response.PaginationResponse;
import com.patria.test.entity.Item;
import com.patria.test.exception.AppException;
import com.patria.test.filter.ItemFilter;
import com.patria.test.repository.ItemRepository;
import com.patria.test.serializer.ItemSerializer;
import com.patria.test.service.ItemService;
import com.patria.test.util.AESUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemSerializer itemSerializer;
    private final ItemFilter itemFilter;

    @Override
    public AppResponse<List<ItemResponse>> list(ItemListRequest request) {
        try {
            Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
            Page<Item> items = itemRepository.findAll(itemFilter.specification(request), pageable);
            return AppResponse.<List<ItemResponse>>builder()
                    .success(true)
                    .data(items.stream().map(item -> {
                        try {
                            return itemSerializer.serialize(item);
                        } catch (Exception e) {
                            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                        }
                    }).toList())
                    .pagination(PaginationResponse.builder()
                            .currentPage(items.getNumber() + 1)
                            .totalPage(items.getTotalPages())
                            .perPage(items.getSize())
                            .total(items.getTotalElements())
                            .count(items.getNumberOfElements())
                            .hasNext(items.hasNext())
                            .hasPrevious(items.hasPrevious())
                            .hasContent(items.hasContent())
                            .build())
                    .message("Success get data")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    public AppResponse<ItemResponse> get(String id) {
        if (id == null || id.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        Long decodedId = AESUtil.getDecryptedString(id);
        if (decodedId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        return AppResponse.<ItemResponse>builder()
                .success(true)
                .data(itemRepository.findById(decodedId)
                        .map(item -> {
                            try {
                                return itemSerializer.serialize(item);
                            } catch (Exception e) {
                                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                            }
                        })
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!")))
                .message("Success get data")
                .build();
    }

    @Override
    @Transactional
    public AppResponse<ItemResponse> save(ItemSaveRequest request) {
        try {
            Item item = Item.builder()
                    .name(request.getName())
                    .price(request.getPrice())
                    .build();

            Item savedItem = itemRepository.save(item);

            ItemResponse itemResponse = itemSerializer.serialize(savedItem);

            return AppResponse.<ItemResponse>builder()
                    .success(true)
                    .data(itemResponse)
                    .message("Item saved successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public AppResponse<ItemResponse> edit(ItemEditRequest request) {
        try {
            Item item = getItemById(request.getId());

            item.setName(request.getName());
            item.setPrice(request.getPrice());
            Item savedItem = itemRepository.save(item);

            ItemResponse itemResponse = itemSerializer.serialize(savedItem);

            return AppResponse.<ItemResponse>builder()
                    .success(true)
                    .data(itemResponse)
                    .message("Item updated successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public AppResponse<ItemResponse> delete(String id) {
        try {
            Item item = getItemById(id);

            itemRepository.delete(item);

            return AppResponse.<ItemResponse>builder()
                    .success(true)
                    .message("Item deleted successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    public Item getItemById(String id) {
        if (id == null || id.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        Long decodedId = AESUtil.getDecryptedString(id);
        if (decodedId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid item id!");
        }
        return itemRepository.findById(decodedId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"));
    }
}

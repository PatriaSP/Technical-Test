package com.patria.test.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.entity.Item;
import com.patria.test.exception.AppException;
import com.patria.test.filter.ItemFilter;
import com.patria.test.repository.ItemRepository;
import com.patria.test.serializer.ItemSerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.service.ItemService;
import com.patria.test.service.OrderService;
import com.patria.test.util.AESUtil;
import com.patria.test.util.StockUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemSerializer itemSerializer;
    private final ItemFilter itemFilter;
    // private final OrderService orderService;
    // private final InventoryService inventoryService;
    
    @Override
    public Page<ItemResponse> list(ItemListRequest request) {
        Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
        return itemRepository.findAll(itemFilter.specification(request), pageable)
                .map(item -> itemSerializer.serialize(item, StockUtil.getStockByItem(item)));
    }

    @Override
    public ItemResponse get(String id) {
        return Optional.ofNullable(getItemById(id))
                .map(item -> itemSerializer.serialize(item, StockUtil.getStockByItem(item)))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"));
    }

    @Override
    @Transactional
    public ItemResponse save(ItemSaveRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .build();

        Item savedItem = itemRepository.save(item);

        return itemSerializer.serialize(savedItem, StockUtil.getStockByItem(savedItem));
    }

    @Override
    @Transactional
    public ItemResponse edit(ItemEditRequest request) {
        Item item = getItemById(request.getId());

        item.setName(request.getName());
        item.setPrice(request.getPrice());
        Item savedItem = itemRepository.save(item);

        return itemSerializer.serialize(savedItem, StockUtil.getStockByItem(savedItem));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Item item = getItemById(id);

        // orderService.deleteAll(item.getOrders());
        // inventoryService.deleteAll(item.getInventories());

        itemRepository.delete(item);
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

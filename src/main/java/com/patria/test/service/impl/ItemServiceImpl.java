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
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Item;
import com.patria.test.exception.AppException;
import com.patria.test.filter.ItemFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.ItemRepository;
import com.patria.test.repository.OrderRepository;
import com.patria.test.serializer.ItemSerializer;
import com.patria.test.service.ItemService;
import com.patria.test.util.AESUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemSerializer itemSerializer;
    private final ItemFilter itemFilter;

    @Override
    public Page<Item> list(ItemListRequest request) {
        try {
            Pageable pageable = PageRequest.of((request.getPage() - 1), request.getPerPage());
            return itemRepository.findAll(itemFilter.specification(request), pageable);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    public ItemResponse get(String id) {
        return Optional.ofNullable(getItemById(id))
                .map(item -> {
                    try {
                        return itemSerializer.serialize(item, getStockByItem(item));
                    } catch (Exception e) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
                    }
                })
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"));
    }

    @Override
    @Transactional
    public ItemResponse save(ItemSaveRequest request) {
        try {
            Item item = Item.builder()
                    .name(request.getName())
                    .price(request.getPrice())
                    .build();

            Item savedItem = itemRepository.save(item);

            return itemSerializer.serialize(savedItem, getStockByItem(savedItem));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error!", e);
        }
    }

    @Override
    @Transactional
    public ItemResponse edit(ItemEditRequest request) {
        try {
            Item item = getItemById(request.getId());

            item.setName(request.getName());
            item.setPrice(request.getPrice());
            Item savedItem = itemRepository.save(item);

            return itemSerializer.serialize(savedItem, getStockByItem(savedItem));
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
            Item item = getItemById(id);

            orderRepository.deleteAll(item.getOrders()); 
            inventoryRepository.deleteAll(item.getInventories()); 

            itemRepository.delete(item);
        } catch (AppException e) {
            throw e;
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

    public Integer getStockByItem(Item item) {
        if (item.getInventories() == null) {
            return 0;
        }

        return item.getInventories().stream().distinct()
                .mapToInt(inv -> inv.getType() == InventoryTypeEnum.T
                        ? inv.getQty()
                        : inv.getType() == InventoryTypeEnum.W
                                ? -inv.getQty()
                                : 0)
                .sum();
    }
}

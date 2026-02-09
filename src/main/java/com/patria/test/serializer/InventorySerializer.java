package com.patria.test.serializer;

import org.springframework.stereotype.Service;

import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.entity.Inventory;
import com.patria.test.util.AESUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventorySerializer {

    private final ItemSerializer itemSerializer;

    public InventoryResponse serialize(Inventory data, Integer stock) {
        return InventoryResponse.builder()
                .id(AESUtil.encrypt(data.getId().toString()))
                .item(itemSerializer.serialize(data.getItem(), stock))
                .qty(data.getQty())
                .type(data.getType())
                .build();
    }
}

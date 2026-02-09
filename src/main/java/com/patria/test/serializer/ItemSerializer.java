package com.patria.test.serializer;

import org.springframework.stereotype.Service;

import com.patria.test.dto.response.ItemResponse;
import com.patria.test.entity.Item;
import com.patria.test.util.AESUtil;

@Service
public class ItemSerializer {

    public ItemResponse serialize(Item data, Integer stock) {
        return ItemResponse.builder()
                .id(AESUtil.encrypt(data.getId().toString()))
                .name(data.getName())
                .price(data.getPrice())
                .stock(stock)
                .build();
    }
}

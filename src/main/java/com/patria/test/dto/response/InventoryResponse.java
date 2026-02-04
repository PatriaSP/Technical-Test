package com.patria.test.dto.response;

import com.patria.test.dto.type.InventoryTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private String id;
    private ItemResponse item;
    private Integer qty;
    private InventoryTypeEnum type;

}

package com.patria.test.dto.request;

import com.patria.test.dto.type.InventoryTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEditRequest {

    @NotBlank(message = "Id is mandatory")
    private String id;

    @NotBlank(message = "ItemId is mandatory")
    private String itemId;

    @NotNull(message = "Qty is mandatory")
    private Integer qty;

    @NotNull(message = "Type is mandatory")
    private InventoryTypeEnum type; 
}

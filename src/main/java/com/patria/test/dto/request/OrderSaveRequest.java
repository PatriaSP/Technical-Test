package com.patria.test.dto.request;

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
public class OrderSaveRequest {

    @NotBlank(message = "ItemId is mandatory")
    private String itemId;

    @NotNull(message = "Qty is mandatory") 
    private Integer qty;
}

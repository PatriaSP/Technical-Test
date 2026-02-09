package com.patria.test.util;

import org.springframework.stereotype.Service;

import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Item;

@Service
public class StockUtil {

    public static Integer getStockByItem(Item item) {
        if (item == null || item.getInventories() == null) {
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

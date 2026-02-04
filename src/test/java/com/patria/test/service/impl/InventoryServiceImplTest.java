package com.patria.test.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.filter.InventoryFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.ItemRepository;
import com.patria.test.serializer.InventorySerializer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventorySerializer inventorySerializer;

    @Mock
    private InventoryFilter inventoryFilter;

    @Mock
    private ItemServiceImpl itemServiceImpl;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Item testItem;
    private Inventory testInventory;
    private InventoryResponse testInventoryResponse;
    private static final Long INVENTORY_ID = 1L;
    private static final String ENCRYPTED_INVENTORY_ID = "encryptedInventoryId123";
    private static final Long ITEM_ID = 1L;
    private static final String ENCRYPTED_ITEM_ID = "encryptedItemId123";

    @BeforeEach
    void setUp() {
        testItem = Item.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .price(new BigDecimal("100.00"))
                .build();

        testInventory = Inventory.builder()
                .id(INVENTORY_ID)
                .item(testItem)
                .qty(10)
                .type(InventoryTypeEnum.T)
                .build();

        testInventoryResponse = InventoryResponse.builder()
                .id(ENCRYPTED_INVENTORY_ID)
                .item(ItemResponse.builder()
                        .id(ENCRYPTED_ITEM_ID)
                        .name("Test Item")
                        .build())
                .qty(10)
                .type(InventoryTypeEnum.T)
                .build();
    }

    @Nested
    @DisplayName("InventoryService.get() Tests")
    class GetTests {

        @Test
        @DisplayName("Should throw exception when null id")
        void get_ShouldThrowExceptionWhenNullId() {
            // When & Then
            Exception exception = assertThrows(Exception.class, 
                () -> inventoryService.get(null));
            
            assertNotNull(exception);
        }
    }
}

package com.patria.test.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Inventory;
import com.patria.test.entity.Item;
import com.patria.test.entity.Order;
import com.patria.test.filter.OrderFilter;
import com.patria.test.repository.InventoryRepository;
import com.patria.test.repository.OrderRepository;
import com.patria.test.serializer.OrderSerializer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderSerializer orderSerializer;

    @Mock
    private OrderFilter orderFilter;

    @Mock
    private ItemServiceImpl itemServiceImpl;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item testItem;
    private Inventory testInventory;
    private Order testOrder;
    private OrderResponse testOrderResponse;
    private static final String ORDER_NO = "O1";
    private static final Long ITEM_ID = 1L;
    private static final String ENCRYPTED_ITEM_ID = "encryptedItemId123";

    @BeforeEach
    void setUp() {
        // Create inventory with T type to provide stock
        Inventory stockInventory = Inventory.builder()
                .id(1L)
                .qty(20)
                .type(InventoryTypeEnum.T)
                .build();

        testItem = Item.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .price(new BigDecimal("100.00"))
                .inventories(new ArrayList<>(List.of(stockInventory)))
                .build();

        testInventory = Inventory.builder()
                .id(2L)
                .item(testItem)
                .qty(10)
                .type(InventoryTypeEnum.W)
                .build();

        testOrder = Order.builder()
                .orderNo(ORDER_NO)
                .item(testItem)
                .qty(5)
                .price(new BigDecimal("100.00"))
                .inventory(testInventory)
                .build();

        testOrderResponse = OrderResponse.builder()
                .orderNo(ORDER_NO)
                .item(ItemResponse.builder()
                        .id(ENCRYPTED_ITEM_ID)
                        .name("Test Item")
                        .build())
                .qty(5)
                .price(new BigDecimal("100.00"))
                .build();
    }

    @Nested
    @DisplayName("OrderService.save() Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save order successfully")
        void save_ShouldSaveOrderSuccessfully() throws Exception {
            // Given
            OrderSaveRequest request = OrderSaveRequest.builder()
                    .itemId(ENCRYPTED_ITEM_ID)
                    .qty(5)
                    .build();

            Order savedOrder = Order.builder()
                    .orderNo("O1")
                    .item(testItem)
                    .qty(5)
                    .price(new BigDecimal("100.00"))
                    .inventory(testInventory)
                    .build();

            when(itemServiceImpl.getItemById(ENCRYPTED_ITEM_ID)).thenReturn(testItem);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderRepository.findTopByOrderByOrderNoDesc()).thenReturn(java.util.Optional.empty());
            when(orderSerializer.serialize(any(Order.class))).thenReturn(testOrderResponse);

            // When
            AppResponse<OrderResponse> response = orderService.save(request);

            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNotNull(response.getData());
            assertEquals("Order created successfully", response.getMessage());
        }
    }
}

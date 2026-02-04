package com.patria.test.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.entity.Item;
import com.patria.test.filter.ItemFilter;
import com.patria.test.repository.ItemRepository;
import com.patria.test.serializer.ItemSerializer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemSerializer itemSerializer;

    @Mock
    private ItemFilter itemFilter;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item testItem;
    private ItemResponse testItemResponse;
    private static final Long DECRYPTED_ID = 1L;

    @BeforeEach
    void setUp() throws Exception {
        testItem = Item.builder()
                .id(DECRYPTED_ID)
                .name("Test Item")
                .price(new BigDecimal("100.00"))
                .build();

        testItemResponse = ItemResponse.builder()
                .id("encryptedId123")
                .name("Test Item")
                .price(new BigDecimal("100.00"))
                .build();
    }

    @Nested
    @DisplayName("ItemService.save() Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save item successfully")
        void save_ShouldSaveItemSuccessfully() throws Exception {
            // Given
            ItemSaveRequest request = ItemSaveRequest.builder()
                    .name("New Item")
                    .price(new BigDecimal("50.00"))
                    .build();

            Item savedItem = Item.builder()
                    .id(DECRYPTED_ID)
                    .name("New Item")
                    .price(new BigDecimal("50.00"))
                    .build();

            ItemResponse savedResponse = ItemResponse.builder()
                    .id("encryptedId123")
                    .name("New Item")
                    .price(new BigDecimal("50.00"))
                    .build();

            when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
            when(itemSerializer.serialize(savedItem)).thenReturn(savedResponse);

            // When
            AppResponse<ItemResponse> response = itemService.save(request);

            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNotNull(response.getData());
            assertEquals("New Item", response.getData().getName());
            assertEquals("Item saved successfully", response.getMessage());
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("Should save item with correct properties")
        void save_ShouldSaveItemWithCorrectProperties() throws Exception {
            // Given
            BigDecimal price = new BigDecimal("75.50");
            ItemSaveRequest request = ItemSaveRequest.builder()
                    .name("Test Product")
                    .price(price)
                    .build();

            Item savedItem = Item.builder()
                    .id(DECRYPTED_ID)
                    .name("Test Product")
                    .price(price)
                    .build();

            when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
            when(itemSerializer.serialize(any(Item.class))).thenReturn(testItemResponse);

            // When
            AppResponse<ItemResponse> response = itemService.save(request);

            // Then
            assertNotNull(response);
            verify(itemRepository).save(argThat(item -> 
                item.getName().equals("Test Product") && 
                item.getPrice().equals(price)));
        }
    }
}

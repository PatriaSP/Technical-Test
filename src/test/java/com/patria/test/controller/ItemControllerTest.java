package com.patria.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.entity.Item;
import com.patria.test.exception.AppException;
import com.patria.test.serializer.ItemSerializer;
import com.patria.test.service.ItemService;
import com.patria.test.util.AESUtil;
import com.patria.test.util.StockUtil;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ItemController.class)
@Import(AESUtil.class)
@TestPropertySource(properties = {
                "AES_KEY=M5Qh!0p@$@%^)!)^[]\\KGe01A"
})
class ItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ItemService itemService;

        @MockitoBean
        private ItemSerializer itemSerializer;

        @Autowired
        private ObjectMapper objectMapper;

        // Test list
        @Test
        void items_success() throws Exception {

                Page<ItemResponse> pageData = new PageImpl<>(
                                List.of(new ItemResponse()),
                                PageRequest.of(0, 5),
                                1);

                ItemResponse response = ItemResponse.builder()
                                .id("1")
                                .name("Item A")
                                .build();

                Mockito.when(itemService.list(any()))
                                .thenReturn(pageData);

                try (MockedStatic<StockUtil> mockedStock = Mockito.mockStatic(StockUtil.class)) {
                        mockedStock.when(() -> StockUtil.getStockByItem(Mockito.any(Item.class)))
                                        .thenReturn(100);
                }

                mockMvc.perform(get("/v1/items")
                                .param("page", "1")
                                .param("perPage", "5")
                                .param("filter", "")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Success get data"))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.pagination.currentPage").value(1))
                                .andExpect(jsonPath("$.pagination.totalPage").value(1))
                                .andDo(print());
        }

        // Test get by id
        @Test
        void item_success() throws Exception {

                String encryptedId = AESUtil.encrypt("1");

                ItemResponse response = ItemResponse.builder()
                                .id(encryptedId)
                                .name("Item A")
                                .build();

                Mockito.when(itemService.get(eq("1")))
                                .thenReturn(response);

                mockMvc.perform(get("/v1/items/{id}", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Success get data"))
                                .andExpect(jsonPath("$.data.id").value(encryptedId));
        }

        // Test get by id not found
        @Test
        void item_notFound() throws Exception {

                String encryptedId = AESUtil.encrypt("1");

                Mockito.when(itemService.get(eq(encryptedId)))
                                .thenThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"));

                mockMvc.perform(get("/v1/items/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Item not found!"));
        }

        // Test save
        @Test
        void save_success() throws Exception {
                String encryptedId = AESUtil.encrypt("1");
                ItemSaveRequest request = ItemSaveRequest.builder()
                                .name("Item A")
                                .price(new BigDecimal("1000"))
                                .build();

                ItemResponse response = ItemResponse.builder()
                                .id(encryptedId)
                                .name("Item A")
                                .build();

                Mockito.when(itemService.save(any()))
                                .thenReturn(response);

                mockMvc.perform(post("/v1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Item saved successfully"))
                                .andExpect(jsonPath("$.data").exists());
        }

        // Test save validation error
        @Test
        void save_validationError() throws Exception {

                ItemSaveRequest request = ItemSaveRequest.builder()
                                .name("") // invalid if @NotBlank
                                .price(null) // invalid if @NotNull
                                .build();

                mockMvc.perform(post("/v1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.message").exists());
        }

        // Test edit success
        @Test
        void edit_success() throws Exception {

                String encryptedId = AESUtil.encrypt("1");

                ItemEditRequest request = ItemEditRequest.builder()
                                .id(encryptedId)
                                .name("Item Updated")
                                .price(new BigDecimal("2000"))
                                .build();

                ItemResponse response = ItemResponse.builder()
                                .id(encryptedId)
                                .name("Item Updated")
                                .build();

                Mockito.when(itemService.edit(any()))
                                .thenReturn(response);

                mockMvc.perform(put("/v1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Item updated successfully"));
        }

        // Test edit fail not found
        @Test
        void edit_notFound() throws Exception {

                String encryptedId = AESUtil.encrypt("999");
                ItemEditRequest request = ItemEditRequest.builder()
                                .id(encryptedId)
                                .name("Item Updated")
                                .price(new BigDecimal("3000"))
                                .build();

                Mockito.when(itemService.edit(any()))
                                .thenThrow(new AppException(404, "Item not found!"));

                mockMvc.perform(put("/v1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Item not found!"));
        }

        // Test delete success
        @Test
        void delete_success() throws Exception {

                String encryptedId = AESUtil.encrypt("1");
                Mockito.doNothing().when(itemService).delete(eq(encryptedId));

                mockMvc.perform(delete("/v1/items/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Item deleted successfully"));
        }

        // Test delete not found
        @Test
        void delete_notFound() throws Exception {

                String encryptedId = AESUtil.encrypt("999");
                Mockito.doThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Item not found!"))
                                .when(itemService).delete(eq(encryptedId));

                mockMvc.perform(delete("/v1/items/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Item not found!"));
        }
}

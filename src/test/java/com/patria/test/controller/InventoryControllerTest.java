package com.patria.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.dto.type.InventoryTypeEnum;
import com.patria.test.entity.Inventory;
import com.patria.test.exception.AppException;
import com.patria.test.serializer.InventorySerializer;
import com.patria.test.service.InventoryService;
import com.patria.test.util.AESUtil;

import org.junit.jupiter.api.Test;
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

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = InventoryController.class)
@Import(AESUtil.class)
@TestPropertySource(properties = {
                "AES_KEY=M5Qh!0p@$@%^)!)^[]\\KGe01A"
})
class InventoryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private InventoryService inventoryService;

        @MockitoBean
        private InventorySerializer inventorySerializer;

        @Autowired
        private ObjectMapper objectMapper;

        private InventoryResponse inventoryResponse;

        // Test list
        @Test
        void inventories_success() throws Exception {

                Page<?> pageData = new PageImpl<>(
                                List.of(new Inventory()),
                                PageRequest.of(0, 5),
                                1);

                Mockito.when(inventoryService.list(any()))
                                .thenReturn((Page) pageData);

                Mockito.when(inventorySerializer.serialize(any()))
                                .thenReturn(inventoryResponse);

                mockMvc.perform(get("/v1/inventory")
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

        // Test by id
        @Test
        void inventory_success() throws Exception {

                String encryptedId = AESUtil.encrypt("1");

                InventoryResponse response = InventoryResponse.builder()
                                .id(encryptedId)
                                .build();

                Mockito.when(inventoryService.get(eq(encryptedId)))
                                .thenReturn(response);

                mockMvc.perform(get("/v1/inventory/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Success get data"))
                                .andExpect(jsonPath("$.data.id").value(encryptedId));
        }

        // Test By id Not found
        @Test
        void inventory_notFound() throws Exception {

                String encryptedId = AESUtil.encrypt("99");

                Mockito.when(inventoryService.get(eq(encryptedId)))
                                .thenThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!"));

                mockMvc.perform(get("/v1/inventory/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Inventory not found!"));
        }

        // Tets save
        @Test
        void save_success() throws Exception {

                InventorySaveRequest request = InventorySaveRequest.builder()
                                .itemId(AESUtil.encrypt("1"))
                                .qty(10)
                                .type(InventoryTypeEnum.T)
                                .build();

                InventoryResponse response = InventoryResponse.builder()
                                .id(AESUtil.encrypt("1"))
                                .build();

                Mockito.when(inventoryService.save(any()))
                                .thenReturn(response);

                mockMvc.perform(post("/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Inventory saved successfully"))
                                .andExpect(jsonPath("$.data").exists());
        }

        // Save fail
        @Test
        void save_validationError() throws Exception {

                InventorySaveRequest request = InventorySaveRequest.builder()
                                .itemId("") // invalid
                                .qty(null) // invalid
                                .type(InventoryTypeEnum.T)
                                .build();

                mockMvc.perform(post("/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.message").exists());
        }

        // Test Edit
        @Test
        void edit_success() throws Exception {

                String encryptedId = AESUtil.encrypt("1");
                String encryptedItemId = AESUtil.encrypt("2");
                InventoryEditRequest request = InventoryEditRequest.builder()
                                .id(encryptedId)
                                .itemId(encryptedItemId)
                                .qty(50)
                                .type(InventoryTypeEnum.T)
                                .build();

                InventoryResponse response = InventoryResponse.builder()
                                .id(encryptedId)
                                .build();

                Mockito.when(inventoryService.save(any()))
                                .thenReturn(response);

                mockMvc.perform(put("/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Inventory updated successfully"));
        }

        // Edit fail
        @Test
        void edit_fail() throws Exception {

                // Inventory not found
                String encryptedId = AESUtil.encrypt("999");
                String encryptedItemId = AESUtil.encrypt("2");
                InventoryEditRequest request = InventoryEditRequest.builder()
                                .id(encryptedId)
                                .itemId(encryptedItemId)
                                .qty(50)
                                .type(InventoryTypeEnum.T)
                                .build();

                Mockito.when(inventoryService.edit(any()))
                                .thenThrow(new AppException(404, "Inventory not found!"));

                mockMvc.perform(put("/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Inventory not found!"));
        }

        // Test edit item not found
        @Test
        void edit_fail_item() throws Exception {
                // Item not found
                String encryptedId = AESUtil.encrypt("1");
                String encryptedItemId = AESUtil.encrypt("999");
                InventoryEditRequest request = InventoryEditRequest.builder()
                                .id(encryptedId)
                                .itemId(encryptedItemId)
                                .qty(50)
                                .type(InventoryTypeEnum.T)
                                .build();

                Mockito.when(inventoryService.edit(any()))
                                .thenThrow(new AppException(404, "Item not found!"));

                mockMvc.perform(put("/v1/inventory")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Item not found!"));
        }

        // Test delete
        @Test
        void delete_success() throws Exception {
                String encryptedId = AESUtil.encrypt("1");
                Mockito.doNothing().when(inventoryService).delete(eq(encryptedId));

                mockMvc.perform(delete("/v1/inventory/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Inventory deleted successfully"));
        }

        // Test delete not found
        @Test
        void delete_notFound() throws Exception {
                String encryptedId = AESUtil.encrypt("999");
                Mockito.doThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Inventory not found!"))
                                .when(inventoryService).delete(eq(encryptedId));

                mockMvc.perform(delete("/v1/inventory/{id}", encryptedId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404))
                                .andExpect(jsonPath("$.message").value("Inventory not found!"));
        }
}

package com.patria.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patria.test.dto.request.OrderEditRequest;
import com.patria.test.dto.request.OrderSaveRequest;
import com.patria.test.dto.response.OrderResponse;
import com.patria.test.entity.Order;
import com.patria.test.exception.AppException;
import com.patria.test.serializer.OrderSerializer;
import com.patria.test.service.OrderService;
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
@WebMvcTest(controllers = OrderController.class)
@Import(AESUtil.class)
@TestPropertySource(properties = {
                "AES_KEY=M5Qh!0p@$@%^)!)^[]\\KGe01A"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderSerializer orderSerializer;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse orderResponse;

    // Test list
    @Test
    void orders_success() throws Exception {

        Page<?> pageData = new PageImpl<>(
                List.of(new Order()),
                PageRequest.of(0, 5),
                1);

        Mockito.when(orderService.list(any()))
                .thenReturn((Page) pageData);

        Mockito.when(orderSerializer.serialize(any()))
                .thenReturn(orderResponse);

        mockMvc.perform(get("/v1/order")
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
    void order_success() throws Exception {

        OrderResponse response = OrderResponse.builder()
                .orderNo("O1")
                .build();

        Mockito.when(orderService.get(eq("O1")))
                .thenReturn(response);

        mockMvc.perform(get("/v1/order/{id}", "O1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success get data"))
                .andExpect(jsonPath("$.data.orderNo").value("O1"));
    }

    // Test by id not found
    @Test
    void order_notFound() throws Exception {

        Mockito.when(orderService.get(eq("O999")))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Order not found!"));

        mockMvc.perform(get("/v1/order/{id}", "O999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Order not found!"));
    }

    // Test save success
    @Test
    void save_success() throws Exception {

        OrderSaveRequest request = OrderSaveRequest.builder()
                .itemId(AESUtil.encrypt("1"))
                .qty(10)
                .build();

        OrderResponse response = OrderResponse.builder()
                .build();

        Mockito.when(orderService.save(any()))
                .thenReturn(response);

        mockMvc.perform(post("/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data").exists());
    }

    // Save validation error
    @Test
    void save_validationError() throws Exception {

        OrderSaveRequest request = OrderSaveRequest.builder()
                .itemId("") // invalid
                .qty(null) // invalid
                .build();

        mockMvc.perform(post("/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // Test edit success
    @Test
    void edit_success() throws Exception {

        String encryptedItemId = AESUtil.encrypt("2");

        OrderEditRequest request = OrderEditRequest.builder()
                .orderNo("O1")
                .itemId(encryptedItemId)
                .qty(50)
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderNo("O1")
                .build();

        Mockito.when(orderService.edit(any()))
                .thenReturn(response);

        mockMvc.perform(put("/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order updated successfully"));
    }

    // Edit fail order not found
    @Test
    void edit_fail() throws Exception {

        String encryptedItemId = AESUtil.encrypt("2");

        OrderEditRequest request = OrderEditRequest.builder()
                .orderNo("O999")
                .itemId(encryptedItemId)
                .qty(50)
                .build();

        Mockito.when(orderService.edit(any()))
                .thenThrow(new AppException(404, "Order not found!"));

        mockMvc.perform(put("/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Order not found!"));
    }

    // Edit fail item not found
    @Test
    void edit_fail_item() throws Exception {

        String encryptedItemId = AESUtil.encrypt("999");

        OrderEditRequest request = OrderEditRequest.builder()
                .orderNo("O1")
                .itemId(encryptedItemId)
                .qty(50)
                .build();

        Mockito.when(orderService.edit(any()))
                .thenThrow(new AppException(404, "Item not found!"));

        mockMvc.perform(put("/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found!"));
    }

    // Test delete success
    @Test
    void delete_success() throws Exception {

        Mockito.doNothing().when(orderService).delete(eq("O1"));

        mockMvc.perform(delete("/v1/order/{id}", "O1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));
    }

    // Test delete not found
    @Test
    void delete_notFound() throws Exception {

        Mockito.doThrow(new AppException(HttpStatus.NOT_FOUND.value(), "Order not found!"))
                .when(orderService).delete(eq("O999"));

        mockMvc.perform(delete("/v1/order/{id}", "O999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Order not found!"));
    }
}

package com.patria.test.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patria.test.dto.request.InventoryListRequest;
import com.patria.test.dto.request.InventorySaveRequest;
import com.patria.test.dto.request.InventoryEditRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.InventoryResponse;
import com.patria.test.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/inventories")
@Tag(name = "Inventory Controller")
public class InventoryController {

        private final InventoryService inventoryService;

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<List<InventoryResponse>> inventories(
                        @RequestParam(required = false, defaultValue = "1") Integer page,
                        @RequestParam(required = false, defaultValue = "5") Integer perPage,
                        @RequestParam(required = false) String filter) {
                InventoryListRequest request = InventoryListRequest.builder()
                                .page(page)
                                .perPage(perPage)
                                .filter(filter)
                                .build();

                return inventoryService.list(request);
        }

        @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<InventoryResponse> inventory(@PathVariable String id) {
                return inventoryService.get(id);
        }

        @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<InventoryResponse> save(@Valid @RequestBody InventorySaveRequest request) {
                return inventoryService.save(request);
        }

        @PutMapping(path = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<InventoryResponse> edit(@Valid @RequestBody InventoryEditRequest request) {
                return inventoryService.edit(request);
        }

        @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<InventoryResponse> delete(@PathVariable String id) {
                return inventoryService.delete(id);
        }
}

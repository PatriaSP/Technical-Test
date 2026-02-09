package com.patria.test.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patria.test.dto.request.ItemSaveRequest;
import com.patria.test.dto.request.ItemEditRequest;
import com.patria.test.dto.request.ItemListRequest;
import com.patria.test.dto.response.AppResponse;
import com.patria.test.dto.response.ItemResponse;
import com.patria.test.dto.response.PaginationResponse;
import com.patria.test.service.ItemService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/items")
@Tag(name = "Item Controller")
public class ItemController {

        private final ItemService itemService;

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<List<ItemResponse>> items(
                        @RequestParam(required = false, defaultValue = "1") Integer page,
                        @RequestParam(required = false, defaultValue = "5") Integer perPage,
                        @RequestParam(required = false) String filter) {

                Page<ItemResponse> items = itemService.list(ItemListRequest.builder()
                                .page(page)
                                .perPage(perPage)
                                .filter(filter)
                                .build());

                return AppResponse.<List<ItemResponse>>builder()
                                .success(true)
                                .data(items.toList())
                                .pagination(PaginationResponse.builder()
                                                .currentPage(items.getNumber() + 1)
                                                .totalPage(items.getTotalPages())
                                                .perPage(items.getSize())
                                                .total(items.getTotalElements())
                                                .count(items.getNumberOfElements())
                                                .hasNext(items.hasNext())
                                                .hasPrevious(items.hasPrevious())
                                                .hasContent(items.hasContent())
                                                .build())
                                .message("Success get data")
                                .build();
        }

        @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<ItemResponse> item(@PathVariable String id) {
                return AppResponse.<ItemResponse>builder()
                                .success(true)
                                .data(itemService.get(id))
                                .message("Success get data")
                                .build();
        }

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<ItemResponse> save(@Valid @RequestBody ItemSaveRequest request) {
                return AppResponse.<ItemResponse>builder()
                                .success(true)
                                .data(itemService.save(request))
                                .message("Item saved successfully")
                                .build();
        }

        @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<ItemResponse> edit(@Valid @RequestBody ItemEditRequest request) {
                return AppResponse.<ItemResponse>builder()
                                .success(true)
                                .data(itemService.edit(request))
                                .message("Item updated successfully")
                                .build();
        }

        @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public AppResponse<ItemResponse> delete(@PathVariable String id) {
                itemService.delete(id);
                return AppResponse.<ItemResponse>builder()
                    .success(true)
                    .message("Item deleted successfully")
                    .build();
        }
}

package com.patria.test.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "Pagination Response")
public class PaginationResponse {

    private Long total;

    private Integer count;

    private Integer currentPage;

    private Integer perPage;

    private Integer totalPage;

    private Boolean hasNext;

    private Boolean hasPrevious;

    private Boolean hasContent;

}

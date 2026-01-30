package com.project_x.core.paginationhelper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationMeta {

    @JsonProperty("currentPage")
    private long currentPage;          // 1-based

    @JsonProperty("pageSize")
    private long pageSize;

    @JsonProperty("totalRecordCount")
    private long totalRecordCount;

    @JsonProperty("totalPages")
    private long totalPages;

    @JsonProperty("currentCount")
    private long currentCount;

    @JsonProperty("fromRecord")
    private long fromRecord;

    @JsonProperty("toRecord")
    private long toRecord;

    @JsonProperty("hasNext")
    private boolean hasNext;

    @JsonProperty("hasPrevious")
    private boolean hasPrevious;

    @JsonProperty("isFirst")
    private boolean isFirst;

    @JsonProperty("isLast")
    private boolean isLast;

    @JsonProperty("empty")
    private boolean empty;

    @JsonProperty("nextPage")
    private Long nextPage;             // null if none

    @JsonProperty("previousPage")
    private Long previousPage;         // null if none

    @JsonProperty("sort")
    private String sort;

    // Optional: UI pagination window
    @JsonProperty("lastShowing")
    private long lastShowingPage;
}

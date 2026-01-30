package com.project_x.core.paginationhelper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class BasePaginationRequest {

    @JsonProperty("pageSize")
    @Builder.Default
    private long pageSize = 35;

    @JsonProperty("page")
    private long page = 1;

    @JsonProperty("lastRecord")
    private Long lastRecord;

    @JsonProperty("totalPage")
    private long totalPage;

    @JsonProperty("nextPage")
    private boolean nextPage;

    @JsonProperty("previousPage")
    private boolean previousPage;

    @JsonProperty("firstPage")
    private boolean firstPage;

    @JsonIgnore
    private long queryPage;

    @JsonIgnore
    private long buttonSize;
}

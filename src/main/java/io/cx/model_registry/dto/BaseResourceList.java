package io.cx.model_registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true, fluent = true)
@Data
public class BaseResourceList <V> {
    @JsonProperty("items")
    private List<V> items;

    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("size")
    private Integer size;
}
package io.cx.model_registry.proxy.dto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.proxy.dto.BaseResource;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredModel extends BaseResource {

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("state")
    private RegisteredModelState state;

    @JsonProperty("readme")
    private String readme;

    @JsonProperty("maturity")
    private String maturity;

    @JsonProperty("language")
    private List<String> language;

    @JsonProperty("tasks")
    private List<String> tasks;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("license")
    private String license;

    @JsonProperty("licenseLink")
    private String licenseLink;

    @JsonProperty("libraryName")
    private String libraryName;
}
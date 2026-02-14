package io.cx.model_registry.dto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredModelUpdate extends BaseResourceUpdate {

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
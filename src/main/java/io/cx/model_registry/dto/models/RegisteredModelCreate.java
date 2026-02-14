package io.cx.model_registry.dto.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceCreate;
import jakarta.validation.constraints.AssertTrue;
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
public class RegisteredModelCreate extends BaseResourceCreate {

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

    @AssertTrue(message = "'name' must be provided")
    @JsonIgnore
    public boolean isNameValid() {
        return name() != null && !name().isBlank();
    }
}

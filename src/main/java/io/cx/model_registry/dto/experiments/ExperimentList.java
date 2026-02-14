package io.cx.model_registry.dto.experiments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResource;
import io.cx.model_registry.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Список экспериментов с поддержкой пагинации.
 * <p>
 * Соответствует схеме {@code ExperimentList} из OpenAPI спецификации.
 * Наследует поля {@code BaseResourceList} (nextPageToken, pageSize, size) и добавляет массив элементов.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentList extends BaseResourceList<Experiment> {
}
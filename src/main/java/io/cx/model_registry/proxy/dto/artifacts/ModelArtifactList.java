package io.cx.model_registry.proxy.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cx.model_registry.proxy.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Список артефактов модели машинного обучения с поддержкой пагинации.
 * <p>
 * Соответствует схеме ModelArtifactList из спецификации OpenAPI, наследует BaseResourceList.
 * Содержит массив элементов артефактов, токен следующей страницы, размер страницы и общее количество элементов.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelArtifactList extends BaseResourceList<ModelArtifact> {
}
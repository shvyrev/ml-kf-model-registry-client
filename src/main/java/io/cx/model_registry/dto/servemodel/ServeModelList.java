package io.cx.model_registry.dto.servemodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.cx.model_registry.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Список ServeModel с поддержкой пагинации.
 * <p>
 * Соответствует схеме {@code ServeModelList} из OpenAPI спецификации.
 * Наследует поля {@code BaseResourceList} (nextPageToken, pageSize, size) и добавляет массив элементов.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServeModelList extends BaseResourceList<ServeModel> {
}
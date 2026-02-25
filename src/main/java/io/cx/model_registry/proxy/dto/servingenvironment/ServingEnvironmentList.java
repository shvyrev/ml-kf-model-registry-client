package io.cx.model_registry.proxy.dto.servingenvironment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.cx.model_registry.proxy.dto.BaseResourceList;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Список ServingEnvironment с поддержкой пагинации.
 * <p>
 * Соответствует схеме {@code ServingEnvironmentList} из OpenAPI спецификации.
 * Наследует поля {@code BaseResourceList} (nextPageToken, pageSize, size) и добавляет массив элементов.
 * </p>
 */
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServingEnvironmentList extends BaseResourceList<ServingEnvironment> {

}
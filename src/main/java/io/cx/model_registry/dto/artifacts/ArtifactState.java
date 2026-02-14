package io.cx.model_registry.dto.artifacts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Состояния артефакта.
 * <p>
 * - UNKNOWN: Неизвестное состояние.
 * - PENDING: Состояние, указывающее, что артефакт может существовать.
 * - LIVE: Состояние, указывающее, что артефакт должен существовать, если что-то внешнее по отношению к системе не удалит его.
 * - MARKED_FOR_DELETION: Состояние, указывающее, что артефакт должен быть удален.
 * - DELETED: Состояние, указывающее, что артефакт был удален.
 * - ABANDONED: Состояние, указывающее, что артефакт был заброшен, возможно, из-за неудавшегося или отмененного выполнения.
 * - REFERENCE: Состояние, указывающее, что артефакт является ссылочным артефактом. Время начала выполнения оркестратор создает выходной артефакт для каждого выходного ключа с состоянием PENDING. Однако для промежуточного артефакта состояние первого артефакта будет REFERENCE. Промежуточные артефакты, созданные во время выполнения компонента, копируют атрибуты артефакта REFERENCE. В конце выполнения состояние артефакта должно остаться REFERENCE, а не измениться на LIVE.
 * </p>
 */
public enum ArtifactState {
    @JsonProperty("UNKNOWN")
    UNKNOWN,
    @JsonProperty("PENDING")
    PENDING,
    @JsonProperty("LIVE")
    LIVE,
    @JsonProperty("MARKED_FOR_DELETION")
    MARKED_FOR_DELETION,
    @JsonProperty("DELETED")
    DELETED,
    @JsonProperty("ABANDONED")
    ABANDONED,
    @JsonProperty("REFERENCE")
    REFERENCE
}
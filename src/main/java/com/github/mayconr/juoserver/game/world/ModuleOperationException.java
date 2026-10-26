package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.Getter;

/** Failure of a module operation, with a technical message and player-facing text or localization key. */
@Getter
public class ModuleOperationException extends RuntimeException {
    private final String operation;
    private final String code;
    private final String displayMessage;

    public ModuleOperationException(String operation, String code, String message, String displayMessage, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.code = code;
        this.displayMessage = displayMessage;
    }

    public static ModuleOperationException fromFailure(String operation, StepResult result, String defaultMessage) {
        if (result.cause() instanceof ModuleOperationException nested) {
            return new ModuleOperationException(operation, nested.getCode(), result.reason(), nested.getDisplayMessage(), nested);
        }
        return new ModuleOperationException(operation, result.code(), result.reason(),
                result.displayMessage() != null ? result.displayMessage() : defaultMessage, result.cause());
    }

    public static ModuleOperationException unexpected(String operation, RuntimeException cause, String defaultMessage) {
        return new ModuleOperationException(operation, "MODULE_OPERATION_FAILED", cause.getMessage(), defaultMessage, cause);
    }
}

package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.concurrent.CompletableFuture;

public record StepResult(
        ResultStatus status,
        CompletableFuture<StepResult> next,
        String code,
        String reason,
        String displayMessage,
        Throwable cause
) {

    /** Creates a result without a player-facing message. */
    public StepResult(ResultStatus status, CompletableFuture<StepResult> next, String code, String reason) {
        this(status, next, code, reason, null, null);
    }

    public StepResult(ResultStatus status, CompletableFuture<StepResult> next, String code, String reason, String displayMessage) {
        this(status, next, code, reason, displayMessage, null);
    }

    public static StepResult failure(String code, String reason, String displayMessage, Throwable cause) {
        return new StepResult(ResultStatus.FAILURE, null, code, reason, displayMessage, cause);
    }

    /** Keeps the technical reason separate from the text or localization key shown to the player. */
    public static StepResult failure(String code, String reason, String displayMessage) {
        return new StepResult(ResultStatus.FAILURE, null, code, reason, displayMessage);
    }

    public static StepResult success() {
        return new StepResult(
                ResultStatus.SUCCESS,
                null,
                null,
                null
        );
    }

    public static StepResult failure(String reason) {
        return new StepResult(
                ResultStatus.FAILURE,
                null,
                "NO_CODE",
                reason
        );
    }

    public static StepResult failure(String code, String reason) {
        return new StepResult(
                ResultStatus.FAILURE,
                null,
                code,
                reason
        );
    }

    public static StepResult async(CompletableFuture<StepResult> future) {
        return new StepResult(
                ResultStatus.ASYNC,
                future,
                null,
                null
        );
    }

    public static StepResult stop() {
        return new StepResult(
                ResultStatus.STOP,
                null,
                null,
                null
        );
    }

    public static StepResult stop(String code, String reason) {
        return new StepResult(
                ResultStatus.STOP,
                null,
                code,
                reason
        );
    }

    public static StepResult skip() {
        return new StepResult(
                ResultStatus.SKIP,
                null,
                null,
                null
        );
    }

    public static StepResult skip(String code, String reason) {
        return new StepResult(
                ResultStatus.SKIP,
                null,
                code,
                reason
        );
    }

    public boolean flowSucceeded() {
        return status != ResultStatus.FAILURE;
    }

    public boolean flowFailed() {
        return status == ResultStatus.FAILURE;
    }

    public boolean shouldContinue() {
        return status == ResultStatus.SUCCESS
                || status == ResultStatus.SKIP;
    }

    public boolean shouldStop() {
        return status == ResultStatus.STOP
                || status == ResultStatus.FAILURE;
    }
}

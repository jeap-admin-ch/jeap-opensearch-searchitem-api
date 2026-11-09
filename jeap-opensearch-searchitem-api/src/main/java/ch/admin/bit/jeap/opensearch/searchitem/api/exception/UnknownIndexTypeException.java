package ch.admin.bit.jeap.opensearch.searchitem.api.exception;

import lombok.Getter;

@Getter
public class UnknownIndexTypeException extends RuntimeException {

    private final String indexType;

    public UnknownIndexTypeException(String indexType) {
        super("Index type '%s' is not registered".formatted(indexType));
        this.indexType = indexType;
    }
}

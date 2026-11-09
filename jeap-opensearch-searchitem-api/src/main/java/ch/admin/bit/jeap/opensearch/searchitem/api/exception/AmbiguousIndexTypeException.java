package ch.admin.bit.jeap.opensearch.searchitem.api.exception;

import lombok.Getter;

@Getter
public class AmbiguousIndexTypeException extends IllegalStateException {

    private final String indexType;
    private final int indexMajorVersion;

    public AmbiguousIndexTypeException(String indexType, int indexMajorVersion, int registeredCount) {
        super("Ambiguous index type '%s' major version %d: %d IndexTypes are registered"
                .formatted(indexType, indexMajorVersion, registeredCount));
        this.indexType = indexType;
        this.indexMajorVersion = indexMajorVersion;
    }
}

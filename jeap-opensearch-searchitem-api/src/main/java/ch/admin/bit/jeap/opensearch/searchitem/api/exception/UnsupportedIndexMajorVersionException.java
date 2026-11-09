package ch.admin.bit.jeap.opensearch.searchitem.api.exception;

import lombok.Getter;

@Getter
public class UnsupportedIndexMajorVersionException extends Exception {

    private final String indexType;
    private final int indexMajorVersion;

    public UnsupportedIndexMajorVersionException(String indexType, int indexMajorVersion) {
        super("Index type '%s' is not available in schema major version %d".formatted(indexType, indexMajorVersion));
        this.indexType = indexType;
        this.indexMajorVersion = indexMajorVersion;
    }
}

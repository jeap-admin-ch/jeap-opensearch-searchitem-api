package ch.admin.bit.jeap.opensearch.searchitem.api.exception;

import lombok.Getter;

@Getter
public class SearchItemMismatchException extends IllegalStateException {

    private final String indexType;
    private final int indexMajorVersion;

    public SearchItemMismatchException(String message, String indexType, int indexMajorVersion) {
        super(message);
        this.indexType = indexType;
        this.indexMajorVersion = indexMajorVersion;
    }
}

package ch.admin.bit.jeap.opensearch.searchitem.api.exception;

public class InvalidIndexMajorVersionRequestException extends RuntimeException {

    public InvalidIndexMajorVersionRequestException(String message) {
        super(message);
    }
}

package ch.admin.bit.jeap.opensearch.searchitem.api.model;

import ch.admin.bit.jeap.opensearch.indextype.IndexType;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

abstract class TestDocumentIndexType<T> implements IndexType<T> {

    public static final String ORIGIN_TYPE = "TestDocument";

    public String system() {
        return "system";
    }

    public String originType() {
        return ORIGIN_TYPE;
    }

    public String description() {
        return null;
    }

    public String documentationUrl() {
        return null;
    }

    public List<String> roles() {
        return List.of();
    }

    public Supplier<InputStream> mappingDefinition() {
        return null;
    }
}

package ch.admin.bit.jeap.opensearch.searchitem.api.model;

public final class TestDocumentIndexTypeV1 extends TestDocumentIndexType<TestDocumentDataV1> {

    public int majorVersion() {
        return 1;
    }

    public int minorVersion() {
        return 3;
    }

    public Class<TestDocumentDataV1> dataClass() {
        return TestDocumentDataV1.class;
    }
}

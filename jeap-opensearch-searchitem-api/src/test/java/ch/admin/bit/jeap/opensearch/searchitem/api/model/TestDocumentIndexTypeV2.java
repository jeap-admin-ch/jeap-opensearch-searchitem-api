package ch.admin.bit.jeap.opensearch.searchitem.api.model;

public final class TestDocumentIndexTypeV2 extends TestDocumentIndexType<TestDocumentDataV2> {

    public int majorVersion() {
        return 2;
    }

    public int minorVersion() {
        return 0;
    }

    public Class<TestDocumentDataV2> dataClass() {
        return TestDocumentDataV2.class;
    }
}

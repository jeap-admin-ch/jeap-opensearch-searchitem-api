package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.indextype.Origin;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.AmbiguousIndexTypeException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemMismatchException;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDataV1;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentDataV1;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentDataV2;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentIndexTypeV1;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentIndexTypeV2;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class IndexTypesUtilsTest {

    private final IndexTypesUtils indexTypesUtils = new IndexTypesUtils();

    @BeforeEach
    void setup(){
        indexTypesUtils.init();
    }

    @Test
    void testCheckDataObject() {
        SearchItem<TestDataV1> searchItem = new SearchItem<>(mock(Origin.class), new TestDataV1("test"));
        assertDoesNotThrow(() -> indexTypesUtils.checkDataObject(searchItem.data()));
    }

    @Test
    void testCheckDataObject_indexType_notFound_throwsException() {
        Class<?> aClass = "not_found".getClass();
        assertThrows(IllegalStateException.class, () -> indexTypesUtils.checkDataObject(aClass));
    }

    @Test
    void findIndexType_matchesOriginTypeAndMajor() {
        assertThat(indexTypesUtils.findIndexType("TestDocument", 1)).get().isInstanceOf(TestDocumentIndexTypeV1.class);
        assertThat(indexTypesUtils.findIndexType("TestDocument", 2)).get().isInstanceOf(TestDocumentIndexTypeV2.class);
        assertThat(indexTypesUtils.findIndexType("TestDocument", 3)).isEmpty();
        assertThat(indexTypesUtils.findIndexType("Unknown", 1)).isEmpty();
    }

    @Test
    void findIndexType_throwsWhenOriginTypeAndMajorAreAmbiguous() {
        IndexTypesUtils ambiguous = new IndexTypesUtils();
        ambiguous.init(List.of(new TestDocumentIndexTypeV1(), new TestDocumentIndexTypeV1(), new TestDocumentIndexTypeV2()));

        assertThatThrownBy(() -> ambiguous.findIndexType("TestDocument", 1))
                .isInstanceOf(AmbiguousIndexTypeException.class).hasMessageContaining("Ambiguous");
        assertThat(ambiguous.findIndexType("TestDocument", 2)).get().isInstanceOf(TestDocumentIndexTypeV2.class);
    }

    @Test
    void checkContainerMatches_acceptsRequestedMajorWithKnownMinor() {
        assertDoesNotThrow(() -> indexTypesUtils.checkContainerMatches(new TestDocumentIndexTypeV1(), container(1, 3, new TestDocumentDataV1("t"))));
        assertDoesNotThrow(() -> indexTypesUtils.checkContainerMatches(new TestDocumentIndexTypeV1(), container(1, 0, new TestDocumentDataV1("t"))));
    }

    @Test
    void checkContainerMatches_rejectsOtherMajorUnknownMinorWrongPayloadAndMissingData() {
        TestDocumentIndexTypeV1 v1 = new TestDocumentIndexTypeV1();
        assertThatThrownBy(() -> indexTypesUtils.checkContainerMatches(v1, container(2, 0, new TestDocumentDataV1("t"))))
                .isInstanceOf(SearchItemMismatchException.class).hasMessageContaining("major version 2");
        assertThatThrownBy(() -> indexTypesUtils.checkContainerMatches(v1, container(1, 4, new TestDocumentDataV1("t"))))
                .isInstanceOf(SearchItemMismatchException.class).hasMessageContaining("minor version 4");
        assertThatThrownBy(() -> indexTypesUtils.checkContainerMatches(v1, container(1, 3, new TestDocumentDataV2("t", "de"))))
                .isInstanceOf(SearchItemMismatchException.class).hasMessageContaining(TestDocumentDataV2.class.getName());
        assertThatThrownBy(() -> indexTypesUtils.checkContainerMatches(v1, container(1, 3, null)))
                .isInstanceOf(SearchItemMismatchException.class).hasMessageContaining("no data");
    }

    private static SearchItemContainer container(int major, int minor, Object data) {
        return new SearchItemContainer(major, minor, new SearchItem<>(mock(Origin.class), data));
    }
}

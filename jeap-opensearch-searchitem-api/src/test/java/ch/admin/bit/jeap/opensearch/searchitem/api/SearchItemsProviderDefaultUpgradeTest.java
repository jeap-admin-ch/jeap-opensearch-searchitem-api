package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.indextype.Origin;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.UnsupportedIndexMajorVersionException;
import ch.admin.bit.jeap.opensearch.searchitem.api.model.TestDocumentDataV1;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * An existing provider that only implements {@link SearchItemsProvider#findSearchItem(String, String, String)}
 * keeps working and gets schema-major requests answered by the default implementation.
 */
class SearchItemsProviderDefaultUpgradeTest {

    private static final SearchItemContainer CURRENT_MAJOR_1 =
            new SearchItemContainer(1, 3, new SearchItem<>(new Origin("12345", "rev-7", null, null, null, null, null), new TestDocumentDataV1("Decree")));

    private final List<String> requestedOriginVersions = new ArrayList<>();

    private final SearchItemsProvider legacyProvider = (indexType, originId, originVersion) -> {
        requestedOriginVersions.add(originVersion);
        return "12345".equals(originId) ? Optional.of(CURRENT_MAJOR_1) : Optional.empty();
    };

    @Test
    void returnsCurrentItem_whenProviderAlreadyProducesRequestedMajor() throws Exception {
        assertThat(legacyProvider.findCurrentSearchItem("TestDocument", "12345", 1)).contains(CURRENT_MAJOR_1);
        assertThat(requestedOriginVersions).containsExactly((String) null);
    }

    @Test
    void reportsUnsupported_insteadOfReturningAnotherMajor() {
        assertThatThrownBy(() -> legacyProvider.findCurrentSearchItem("TestDocument", "12345", 2))
                .isInstanceOf(UnsupportedIndexMajorVersionException.class)
                .hasMessageContaining("TestDocument")
                .hasMessageContaining("2");
    }

    @Test
    void returnsEmpty_whenObjectNotFound() throws Exception {
        assertThat(legacyProvider.findCurrentSearchItem("TestDocument", "unknown", 1)).isEmpty();
    }

    @Test
    void legacyCallKeepsPassingOriginVersionThrough() throws Exception {
        legacyProvider.findSearchItem("TestDocument", "12345", "rev-7");
        assertThat(requestedOriginVersions).containsExactly("rev-7");
    }
}

package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemsBadInputException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.UnsupportedIndexMajorVersionException;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;

import java.util.Optional;

public interface SearchItemsProvider {

    /**
     * Returns the search item for the given origin. {@code originVersion} is an optional business revision of the
     * object; the provider chooses the schema major of the returned representation.
     */
    Optional<SearchItemContainer> findSearchItem(String indexType, String originId, String originVersion) throws SearchItemsBadInputException;

    /**
     * Returns the current data of the given origin, built for the requested schema major of the index type.
     * Implementations must return a container whose {@code indexMajorVersion} equals {@code indexMajorVersion}, or
     * throw {@link UnsupportedIndexMajorVersionException} if they cannot build that major. They must never return a
     * different major instead.
     * <p>
     * The default implementation lets existing providers serve the schema major they already produce today: it
     * returns the current item (no {@code originVersion}) if it was built for the requested major and reports any
     * other major as unsupported. Providers serving more than one major must override this method.
     */
    default Optional<SearchItemContainer> findCurrentSearchItem(String indexType, String originId, int indexMajorVersion)
            throws SearchItemsBadInputException, UnsupportedIndexMajorVersionException {
        Optional<SearchItemContainer> current = findSearchItem(indexType, originId, null);
        if (current.isPresent() && current.get().indexMajorVersion() != indexMajorVersion) {
            throw new UnsupportedIndexMajorVersionException(indexType, indexMajorVersion);
        }
        return current;
    }
}

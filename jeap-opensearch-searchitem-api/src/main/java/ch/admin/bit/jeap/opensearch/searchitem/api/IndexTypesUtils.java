package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.indextype.IndexType;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.AmbiguousIndexTypeException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemMismatchException;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IndexTypesUtils {

    private List<IndexType<?>> indexTypes;
    private Set<Class<?>> indexTypeClasses;

    @PostConstruct
    public void init() {
        init(IndexType.loadAll());
    }

    void init(List<IndexType<?>> loadedIndexTypes) {
        indexTypes = loadedIndexTypes;
        indexTypeClasses = indexTypes.stream()
                .map(IndexType::dataClass)
                .collect(Collectors.toSet());
        indexTypes.stream()
                .collect(Collectors.groupingBy(indexType -> indexType.originType() + " major version " + indexType.majorVersion(),
                        Collectors.counting()))
                .forEach((key, count) -> {
                    if (count > 1) {
                        log.warn("{} IndexTypes registered for index type '{}'; index_major_version requests for it will fail",
                                count, key);
                    }
                });
    }

    public void checkDataObject(Object data) {
        if (indexTypeClasses.contains(data.getClass())) {
            return;
        }
        throw new IllegalStateException("Received search item with data of type '%s' which does not match any known IndexType data class".formatted(data.getClass().getName()));
    }

    public boolean isKnownOriginType(String originType) {
        return indexTypes.stream().anyMatch(indexType -> indexType.originType().equals(originType));
    }

    /**
     * @throws AmbiguousIndexTypeException if several registered index types share the origin type and major version,
     *                                     as the requested index type would then depend on the classpath order
     */
    public Optional<IndexType<?>> findIndexType(String originType, int majorVersion) {
        List<IndexType<?>> matches = indexTypes.stream()
                .filter(indexType -> indexType.originType().equals(originType) && indexType.majorVersion() == majorVersion)
                .toList();
        if (matches.size() > 1) {
            throw new AmbiguousIndexTypeException(originType, majorVersion, matches.size());
        }
        return matches.stream().findFirst();
    }

    /**
     * Ensures a provider answer for a schema-major request is exactly what was requested. The data class alone is
     * not sufficient, since a class may be reused across minors; major and minor are checked explicitly.
     */
    public void checkContainerMatches(IndexType<?> requested, SearchItemContainer container) {
        if (container.indexMajorVersion() != requested.majorVersion()) {
            throw mismatch(requested, "major version %d".formatted(container.indexMajorVersion()));
        }
        if (container.indexMinorVersion() < 0 || container.indexMinorVersion() > requested.minorVersion()) {
            throw mismatch(requested, "minor version %d (known minor version is %d)"
                    .formatted(container.indexMinorVersion(), requested.minorVersion()));
        }
        if (container.searchItem() == null || container.searchItem().data() == null) {
            throw mismatch(requested, "no data");
        }
        Class<?> dataClass = container.searchItem().data().getClass();
        if (!requested.dataClass().equals(dataClass)) {
            throw mismatch(requested, "data of type '%s' (expected '%s')"
                    .formatted(dataClass.getName(), requested.dataClass().getName()));
        }
    }

    private static SearchItemMismatchException mismatch(IndexType<?> requested, String actual) {
        return new SearchItemMismatchException("SearchItemsProvider returned %s for index type '%s' major version %d"
                .formatted(actual, requested.originType(), requested.majorVersion()),
                requested.originType(), requested.majorVersion());
    }
}

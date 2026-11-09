package ch.admin.bit.jeap.opensearch.searchitem.api;

import ch.admin.bit.jeap.opensearch.indextype.IndexType;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.AmbiguousIndexTypeException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.InvalidIndexMajorVersionRequestException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemMismatchException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.SearchItemsBadInputException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.UnknownIndexTypeException;
import ch.admin.bit.jeap.opensearch.searchitem.api.exception.UnsupportedIndexMajorVersionException;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/index-api")
@Tag(name = "SearchItems", description = "Search and retrieve indexed items.")
@RequiredArgsConstructor
@Slf4j
public class SearchItemsController {

    public static final String PARAM_INDEX_TYPE = "index_type";
    public static final String PARAM_INDEX_MAJOR_VERSION = "index_major_version";
    public static final String HEADER_INDEX_TYPE = "index-type";
    public static final String HEADER_INDEX_MAJOR_VERSION = "index-major-version";
    public static final String HEADER_INDEX_MINOR_VERSION = "index-minor-version";
    public static final String UNSUPPORTED_INDEX_MAJOR_VERSION_CODE = "UNSUPPORTED_INDEX_MAJOR_VERSION";
    public static final URI UNSUPPORTED_INDEX_MAJOR_VERSION_TYPE = URI.create("urn:jeap:opensearch:searchitem:unsupported-index-major-version");
    public static final String SEARCH_ITEM_MISMATCH_CODE = "SEARCH_ITEM_MISMATCH";
    public static final URI SEARCH_ITEM_MISMATCH_TYPE = URI.create("urn:jeap:opensearch:searchitem:search-item-mismatch");
    public static final String INVALID_INDEX_MAJOR_VERSION_CODE = "INVALID_INDEX_MAJOR_VERSION";
    public static final URI INVALID_INDEX_MAJOR_VERSION_TYPE = URI.create("urn:jeap:opensearch:searchitem:invalid-index-major-version");
    public static final String UNKNOWN_INDEX_TYPE_CODE = "UNKNOWN_INDEX_TYPE";
    public static final URI UNKNOWN_INDEX_TYPE_TYPE = URI.create("urn:jeap:opensearch:searchitem:unknown-index-type");
    public static final String AMBIGUOUS_INDEX_TYPE_CODE = "AMBIGUOUS_INDEX_TYPE";
    public static final URI AMBIGUOUS_INDEX_TYPE_TYPE = URI.create("urn:jeap:opensearch:searchitem:ambiguous-index-type");

    private static final String LOG_REJECTED_REQUEST = "Rejected search item request: {}";

    private final SearchItemsProvider searchItemsProvider;

    private final IndexTypesUtils indexTypesUtils;

    @Operation(
            summary = "Get the search item of an origin.",
            description = """
                    Without `index_major_version`, the provider returns the item optionally for the business \
                    revision given in `origin_version`, in a schema major chosen by the provider.

                    With `index_major_version`, the provider returns the **current** data of the object, built for \
                    the requested schema major of the index type. The schema major is the major version of the \
                    index type's schema; it is neither the version of the business object nor a physical index name. \
                    The provider never returns another major instead: if it cannot build the requested major, it \
                    answers 422. `index_major_version` cannot be combined with `origin_version`.

                    The `index-major-version` and `index-minor-version` response headers describe the representation \
                    the provider produced for this response, not an index the data was read from.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search item found",
                            headers = {
                                    @Header(name = HEADER_INDEX_MAJOR_VERSION, required = true, schema = @Schema(type = "integer"),
                                            description = "Schema major version of the index type the provider built the payload for"),
                                    @Header(name = HEADER_INDEX_MINOR_VERSION, required = true, schema = @Schema(type = "integer"),
                                            description = "Schema minor version of the index type the provider built the payload for"),
                                    @Header(name = HEADER_INDEX_TYPE, schema = @Schema(type = "string"),
                                            description = "Index type of the payload. Only sent when `index_major_version` was requested.")
                            }),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters. With `index_major_version` the body is a problem detail: code `INVALID_INDEX_MAJOR_VERSION` (empty, not an integer or combined with `origin_version`) or `UNKNOWN_INDEX_TYPE` (`index_type` is not registered)",
                            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Caller does not hold the role `searchitem:read`", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No object found for `origin_id`", content = @Content),
                    @ApiResponse(responseCode = "422", description = "The provider does not support the requested `index_major_version` for this index type (problem code `UNSUPPORTED_INDEX_MAJOR_VERSION`)",
                            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "500", description = "The provider built a search item that does not match the requested index type, major or minor (problem code `SEARCH_ITEM_MISMATCH`), or several index types are registered for the requested index type and major (problem code `AMBIGUOUS_INDEX_TYPE`)",
                            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
            },
            security = {@SecurityRequirement(name = "OIDC")}
    )
    @PreAuthorize("hasRole('searchitem', 'read')")
    @GetMapping(value = "/searchitems", produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("java:S1452")
    public ResponseEntity<SearchItem<?>> searchItem(
            @Parameter(description = "Origin type of the index type, e.g. `JmeDecreeDocument`")
            @RequestParam(PARAM_INDEX_TYPE) String indexType,
            @Parameter(description = "Identifier of the business object")
            @RequestParam("origin_id") String originId,
            @Parameter(description = "Optional business revision of the object. Never interpreted as a schema major.")
            @RequestParam(value = "origin_version", required = false) String originVersion,
            @Parameter(description = "Optional schema major of the index type. When set, the current data of the object is returned, built for this major.")
            @RequestParam(value = PARAM_INDEX_MAJOR_VERSION, required = false) Integer indexMajorVersion,
            HttpServletRequest request)
            throws UnsupportedIndexMajorVersionException {

        log.info("Received request to search item with indexType '{}', originId '{}', originVersion '{}', indexMajorVersion '{}'",
                indexType, originId, originVersion, indexMajorVersion);

        // Spring binds a blank value to null, which would silently turn the request into a live lookup
        if (indexMajorVersion == null && request.getParameter(PARAM_INDEX_MAJOR_VERSION) != null) {
            throw new InvalidIndexMajorVersionRequestException("index_major_version must not be empty");
        }

        try {
            if (indexMajorVersion != null) {
                return currentSearchItem(indexType, originId, originVersion, indexMajorVersion);
            }

            Optional<SearchItemContainer> itemContainerOptional = searchItemsProvider.findSearchItem(indexType, originId, originVersion);

            if (itemContainerOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            SearchItemContainer itemContainer = itemContainerOptional.get();
            indexTypesUtils.checkDataObject(itemContainer.searchItem().data());

            return ResponseEntity.ok()
                    .header(HEADER_INDEX_MAJOR_VERSION, String.valueOf(itemContainer.indexMajorVersion()))
                    .header(HEADER_INDEX_MINOR_VERSION, String.valueOf(itemContainer.indexMinorVersion()))
                    .body(itemContainer.searchItem());

        } catch (SearchItemsBadInputException _) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<SearchItem<?>> currentSearchItem(String indexType, String originId, String originVersion, int indexMajorVersion)
            throws SearchItemsBadInputException, UnsupportedIndexMajorVersionException {
        if (originVersion != null) {
            throw new InvalidIndexMajorVersionRequestException("index_major_version cannot be combined with origin_version");
        }
        if (!indexTypesUtils.isKnownOriginType(indexType)) {
            throw new UnknownIndexTypeException(indexType);
        }
        IndexType<?> requested = indexTypesUtils.findIndexType(indexType, indexMajorVersion)
                .orElseThrow(() -> new UnsupportedIndexMajorVersionException(indexType, indexMajorVersion));

        Optional<SearchItemContainer> itemContainerOptional = searchItemsProvider.findCurrentSearchItem(indexType, originId, indexMajorVersion);
        if (itemContainerOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SearchItemContainer itemContainer = itemContainerOptional.get();
        indexTypesUtils.checkContainerMatches(requested, itemContainer);

        return ResponseEntity.ok()
                .header(HEADER_INDEX_TYPE, indexType)
                .header(HEADER_INDEX_MAJOR_VERSION, String.valueOf(itemContainer.indexMajorVersion()))
                .header(HEADER_INDEX_MINOR_VERSION, String.valueOf(itemContainer.indexMinorVersion()))
                .body(itemContainer.searchItem());
    }

    @ExceptionHandler(UnsupportedIndexMajorVersionException.class)
    ResponseEntity<ProblemDetail> handleUnsupportedIndexMajorVersion(UnsupportedIndexMajorVersionException e) {
        log.info(LOG_REJECTED_REQUEST, e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problem.setType(UNSUPPORTED_INDEX_MAJOR_VERSION_TYPE);
        problem.setTitle("Unsupported index major version");
        problem.setProperty("code", UNSUPPORTED_INDEX_MAJOR_VERSION_CODE);
        problem.setProperty(PARAM_INDEX_TYPE, e.getIndexType());
        problem.setProperty(PARAM_INDEX_MAJOR_VERSION, e.getIndexMajorVersion());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidIndexMajorVersionRequestException.class)
    ResponseEntity<ProblemDetail> handleInvalidIndexMajorVersionRequest(InvalidIndexMajorVersionRequestException e) {
        return invalidIndexMajorVersionRequest(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        if (PARAM_INDEX_MAJOR_VERSION.equals(e.getName())) {
            return invalidIndexMajorVersionRequest("index_major_version must be an integer");
        }
        // Rethrowing falls back to Spring's default resolvers (DefaultHandlerExceptionResolver), not to @ControllerAdvice
        throw e;
    }

    @ExceptionHandler(UnknownIndexTypeException.class)
    ResponseEntity<ProblemDetail> handleUnknownIndexType(UnknownIndexTypeException e) {
        log.info(LOG_REJECTED_REQUEST, e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setType(UNKNOWN_INDEX_TYPE_TYPE);
        problem.setTitle("Unknown index type");
        problem.setProperty("code", UNKNOWN_INDEX_TYPE_CODE);
        problem.setProperty(PARAM_INDEX_TYPE, e.getIndexType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private static ResponseEntity<ProblemDetail> invalidIndexMajorVersionRequest(String detail) {
        log.info(LOG_REJECTED_REQUEST, detail);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(INVALID_INDEX_MAJOR_VERSION_TYPE);
        problem.setTitle("Invalid index major version request");
        problem.setProperty("code", INVALID_INDEX_MAJOR_VERSION_CODE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(AmbiguousIndexTypeException.class)
    ResponseEntity<ProblemDetail> handleAmbiguousIndexType(AmbiguousIndexTypeException e) {
        log.error("Cannot serve search item request: {}", e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Several index types are registered for the requested index type and major version");
        problem.setType(AMBIGUOUS_INDEX_TYPE_TYPE);
        problem.setTitle("Ambiguous index type");
        problem.setProperty("code", AMBIGUOUS_INDEX_TYPE_CODE);
        problem.setProperty(PARAM_INDEX_TYPE, e.getIndexType());
        problem.setProperty(PARAM_INDEX_MAJOR_VERSION, e.getIndexMajorVersion());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(SearchItemMismatchException.class)
    ResponseEntity<ProblemDetail> handleSearchItemMismatch(SearchItemMismatchException e) {
        log.error("SearchItemsProvider returned a search item that does not match the request: {}", e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "The search item provider returned an item that does not match the requested index type and major version");
        problem.setType(SEARCH_ITEM_MISMATCH_TYPE);
        problem.setTitle("Search item does not match the request");
        problem.setProperty("code", SEARCH_ITEM_MISMATCH_CODE);
        problem.setProperty(PARAM_INDEX_TYPE, e.getIndexType());
        problem.setProperty(PARAM_INDEX_MAJOR_VERSION, e.getIndexMajorVersion());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}

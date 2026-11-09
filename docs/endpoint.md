# SearchItem endpoint

`SearchItemsController` exposes a single REST endpoint that the jEAP OpenSearch Index Writer calls
to retrieve the search representation of a business object before writing it to OpenSearch.

## Current data vs. business revision

The endpoint answers two kinds of requests. They are told apart by the query parameters.

| Request                      | Parameters                                       | What the provider returns                                                                                       | Used by                   |
|------------------------------|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|---------------------------|
| Live lookup (unchanged)      | `index_type`, `origin_id`, `origin_version`?     | The item, optionally for the business revision `origin_version`, in a schema major **chosen by the provider**. | Index writer, live events |
| Current data in schema major | `index_type`, `origin_id`, `index_major_version` | The **current** data of the object, built for the **requested** schema major. No business revision is given.   | Maintenance jobs          |

Three different versions are involved. Do not mix them up:

| Term                 | Example                         | Meaning                                                                                                    |
|----------------------|---------------------------------|------------------------------------------------------------------------------------------------------------|
| Schema major / minor | `JmeDecreeDocument` 1.3         | Version of the index type's schema. Selects the shape of `data`.                                           |
| Business revision    | `origin_version=7`              | Version of the business object itself. Reported in `origin.version`. Never interpreted as a schema major. |
| Physical index       | `jme_decree_document_v1-000003` | An OpenSearch index. Not part of this API; the provider does not read from it to answer a request.        |

## Request

```
GET /index-api/searchitems
```

| Parameter             | Required | Description                                                                                                                                                                |
|-----------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `index_type`          | Yes      | The `originType` of the target `IndexType` (e.g. `JmeDecreeDocument`).                                                                                                     |
| `origin_id`           | Yes      | The business object identifier.                                                                                                                                            |
| `origin_version`      | No       | Business revision of the object. When provided, the provider may return the representation at that revision. Cannot be combined with `index_major_version`.                |
| `index_major_version` | No       | Schema major of the index type (integer). When provided, the provider returns the current data of the object built for exactly this major, or answers `422`. Since 3.30.0. |

Example: a maintenance job fixed to major 1 refreshes object `12345`:

```
GET /index-api/searchitems?index_type=JmeDecreeDocument&origin_id=12345&index_major_version=1
```

## Security

The caller must be authenticated (OAuth2 bearer token) and hold the role `searchitem:read`, for
both kinds of requests. The index writer service uses the OAuth2 client configured in the
`oauthClientId` field of the operation configuration.

## Response

**HTTP 200 OK** — object found:

```
index-type: JmeDecreeDocument
index-major-version: 1
index-minor-version: 3
```

```json
{
  "origin": {
    "id":       "12345",
    "version":  "7",
    "bp_id":    "bp-001",
    "created":  "2024-01-15T10:30:00Z",
    "modified": "2024-03-22T14:00:00Z"
  },
  "data": {
    "document_id": "12345",
    "document_title": "Decree on Public Procurement",
    "created_at": "2024-01-15T10:30:00Z"
  }
}
```

Here `origin.version` (`7`) is the business revision of the object. The schema major and minor are
reported only in the headers.

The response headers describe the representation **the provider produced for this response**. They
do not describe an index the data was read from:

| Header                | Sent                            | Description                                               |
|-----------------------|---------------------------------|-----------------------------------------------------------|
| `index-major-version` | Always                          | Schema major of the index type the payload was built for. |
| `index-minor-version` | Always                          | Schema minor of the index type the payload was built for. |
| `index-type`          | Only with `index_major_version` | Index type the payload was built for.                     |

With `index_major_version`, the controller checks before answering that:

- the requested major is registered for `index_type` (as an `IndexType` on the provider's classpath),
- the returned major equals the requested major,
- the returned minor is not higher than the minor of the registered `IndexType`, and
- the payload is an instance of exactly the registered `IndexType`'s data class.

The data class check alone is not enough, because a data class can be reused across minors. That
is why the index writer also checks major, minor, index type and payload on its side.

**HTTP 400 Bad Request** — invalid request parameters: thrown as `SearchItemsBadInputException` by
the `SearchItemsProvider` implementation (empty body), or `index_major_version` is empty, not an integer or
combined with `origin_version`. In the latter cases the body is an RFC 9457 problem detail:

```json
{
  "type": "urn:jeap:opensearch:searchitem:invalid-index-major-version",
  "title": "Invalid index major version request",
  "status": 400,
  "detail": "index_major_version cannot be combined with origin_version",
  "code": "INVALID_INDEX_MAJOR_VERSION"
}
```

With `index_major_version`, an `index_type` for which no `IndexType` is registered on the provider's classpath
is also answered with 400. The problem detail has type `urn:jeap:opensearch:searchitem:unknown-index-type`,
code `UNKNOWN_INDEX_TYPE` and `index_type`.

**HTTP 401 Unauthorized** — no valid bearer token.

**HTTP 403 Forbidden** — caller does not hold the `searchitem:read` role.

**HTTP 404 Not Found** — no object found for the given `origin_id`.

**HTTP 422 Unprocessable Content** — only with `index_major_version`: the provider does not support
the requested schema major for this (registered) index type. The provider never returns another major instead.
The body is an RFC 9457 problem detail:

```json
{
  "type": "urn:jeap:opensearch:searchitem:unsupported-index-major-version",
  "title": "Unsupported index major version",
  "status": 422,
  "detail": "Index type 'JmeDecreeDocument' is not available in schema major version 3",
  "code": "UNSUPPORTED_INDEX_MAJOR_VERSION",
  "index_type": "JmeDecreeDocument",
  "index_major_version": 3
}
```

**HTTP 500 Internal Server Error** with problem code `SEARCH_ITEM_MISMATCH` — only with
`index_major_version`: the `SearchItemsProvider` implementation returned an item that does not match
the request (other major, unknown minor or other data class). This is a bug in the provider. The body is
an RFC 9457 problem detail; the details of the mismatch are only logged by the provider service, not returned:

```json
{
  "type": "urn:jeap:opensearch:searchitem:search-item-mismatch",
  "title": "Search item does not match the request",
  "status": 500,
  "detail": "The search item provider returned an item that does not match the requested index type and major version",
  "code": "SEARCH_ITEM_MISMATCH",
  "index_type": "JmeDecreeDocument",
  "index_major_version": 2
}
```

Each combination of index type (`originType`) and major version must be registered by exactly one `IndexType`
on the provider's classpath. If several are registered, `index_major_version` requests for that combination
fail with HTTP 500 and a problem detail with type `urn:jeap:opensearch:searchitem:ambiguous-index-type`, code
`AMBIGUOUS_INDEX_TYPE`, `index_type` and `index_major_version`.

## SearchItemsProvider

```java
public interface SearchItemsProvider {

    Optional<SearchItemContainer> findSearchItem(String indexType, String originId, String originVersion)
            throws SearchItemsBadInputException;

    default Optional<SearchItemContainer> findCurrentSearchItem(String indexType, String originId, int indexMajorVersion)
            throws SearchItemsBadInputException, UnsupportedIndexMajorVersionException { … }
}
```

- `findSearchItem` answers live lookups. Its contract is unchanged.
- `findCurrentSearchItem` answers requests with `index_major_version`. It returns the current data of
  the object built for `indexMajorVersion`, returns `Optional.empty()` if the object does not exist,
  or throws `UnsupportedIndexMajorVersionException` if it cannot build that major. It must never
  return a different major.

## Upgrading

### Providers

Upgrading to 3.30.0 requires no code change. Existing providers keep working:

- Live lookups (with or without `origin_version`) behave exactly as before. `origin_version` is passed
  to `findSearchItem` unchanged and is never interpreted as a schema major.
- Requests with `index_major_version` are answered by the default `findCurrentSearchItem`: it calls
  `findSearchItem(indexType, originId, null)` (the current data) and returns the result only if it was
  built for the requested major. Any other major is answered with `422`.

A provider that only ever builds one major per index type is therefore ready for maintenance jobs
as soon as it is upgraded. A provider that can build more than one major (for example while a new
major is being introduced) overrides `findCurrentSearchItem`:

```java
@Override
public Optional<SearchItemContainer> findCurrentSearchItem(String indexType, String originId, int indexMajorVersion)
        throws SearchItemsBadInputException, UnsupportedIndexMajorVersionException {
    Function<DecreeDocument, SearchItemContainer> projection = switch (indexMajorVersion) {
        case 1 -> this::toMajor1;
        case 2 -> this::toMajor2;
        default -> throw new UnsupportedIndexMajorVersionException(indexType, indexMajorVersion);
    };
    return repository.findCurrent(originId).map(projection);
}
```

Upgrading does not create representations the provider never supported: if the provider cannot
build an old major, requests for it keep failing with `422`.

### Callers

Callers that do not send `index_major_version` get exactly the previous behaviour. The response of a
live lookup is unchanged (no `index-type` header). Callers that want the current data in a fixed
schema major send `index_major_version` and must not send `origin_version`.

A provider still on a version before 3.30.0 ignores `index_major_version` and answers with its own
choice of major and without the `index-type` header. Callers must therefore reject a response that
lacks `index-type` or reports another major; the index writer does this.

## Modules

This library is split into two modules:

| Module                             | Description                                                                                                                                                  |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `jeap-opensearch-searchitem-model` | The `SearchItemContainer` record that pairs a `SearchItem` with `indexMajorVersion` and `indexMinorVersion`. Add this dependency if you only need the model. |
| `jeap-opensearch-searchitem-api`   | The `SearchItemsController` REST endpoint and the `SearchItemsProvider` SPI. Pulls in the model transitively.                                                |

## SearchItemContainer

`SearchItemsProvider` returns a `SearchItemContainer` that wraps the `SearchItem` together with
the schema version the provider built it for:

```java
public record SearchItemContainer(
    int indexMajorVersion,
    int indexMinorVersion,
    SearchItem<?> searchItem
) {}
```

`indexMajorVersion` and `indexMinorVersion` are forwarded as response headers and used by the
index writer to validate the response against the expected `IndexType` version.

## Related

- [Getting started](getting-started.md)
- [jeap-opensearch-searchitem-api](../README.md)

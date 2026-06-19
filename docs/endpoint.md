# SearchItem endpoint

`SearchItemsController` exposes a single REST endpoint that the jEAP OpenSearch Index Writer calls
to retrieve the current search representation of a business object before writing it to OpenSearch.

## Request

```
GET /index-api/searchitems
```

| Parameter        | Required | Description                                                                                                  |
|------------------|----------|--------------------------------------------------------------------------------------------------------------|
| `index_type`     | Yes      | The `originType` of the target `IndexType` (e.g. `JmeDecreeDocument`).                                       |
| `origin_id`      | Yes      | The business object identifier.                                                                              |
| `origin_version` | No       | Optional version string. When provided, the provider may return the representation at that specific version. |

**Security:** The caller must hold the role `searchitem:read`. The index writer service uses the
OAuth2 client configured in the `oauthClientId` field of the operation configuration.

## Response

**HTTP 200 OK** — business object found:

```json
{
  "origin": {
    "id":       "decree-doc-42",
    "version":  "3",
    "bp_id":    "bp-001",
    "created":  "2024-01-15T10:30:00Z",
    "modified": "2024-03-22T14:00:00Z"
  },
  "data": {
    "document_id": "decree-doc-42",
    "document_title": "Decree on Public Procurement",
    "created_at": "2024-01-15T10:30:00Z"
  }
}
```

Response headers carry the version of the physical OpenSearch index the item was read from:

| Header                 | Description                                                 |
|------------------------|-------------------------------------------------------------|
| `index-major-version`  | Major version of the index used to serialize the response.  |
| `index-minor-version`  | Minor version of the index used to serialize the response.  |

**HTTP 404 Not Found** — no item found for the given `origin_id`.

**HTTP 400 Bad Request** — invalid request parameters (thrown as `SearchItemsBadInputException`
by the `SearchItemsProvider` implementation).

**HTTP 403 Forbidden** — caller does not hold the `searchitem:read` role.

## Modules

This library is split into two modules:

| Module                             | Description                                                                                                                                                  |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `jeap-opensearch-searchitem-model` | The `SearchItemContainer` record that pairs a `SearchItem` with `indexMajorVersion` and `indexMinorVersion`. Add this dependency if you only need the model. |
| `jeap-opensearch-searchitem-api`   | The `SearchItemsController` REST endpoint and the `SearchItemsProvider` SPI. Pulls in the model transitively.                                                |

## SearchItemContainer

`SearchItemsProvider` returns a `SearchItemContainer` that wraps the `SearchItem` together with
the index version metadata:

```java
public record SearchItemContainer(
    int indexMajorVersion,
    int indexMinorVersion,
    SearchItem<?> searchItem
) {}
```

The `indexMajorVersion` and `indexMinorVersion` are forwarded as response headers and used by the
index writer to validate the response against the expected `IndexType` version.

## Related

- [Getting started](getting-started.md)
- [jeap-opensearch-searchitem-api](../README.md)

# jeap-opensearch-searchitem-api

Spring MVC REST API and model for exposing indexed search items from OpenSearch in jEAP applications.

## Overview

This multi-module project provides two libraries:

- **`jeap-opensearch-searchitem-model`** — the `SearchItemContainer` record that pairs a `SearchItem` with its index version metadata
- **`jeap-opensearch-searchitem-api`** — a ready-to-use Spring MVC REST controller and the `SearchItemsProvider` interface that applications implement to supply items from their OpenSearch indices

## Modules

| Module                             | Description                                                                                          |
|------------------------------------|------------------------------------------------------------------------------------------------------|
| `jeap-opensearch-searchitem-model` | `SearchItemContainer` record: wraps `SearchItem<?>` with `indexMajorVersion` and `indexMinorVersion` |
| `jeap-opensearch-searchitem-api`   | `SearchItemsController` REST endpoint, `SearchItemsProvider` SPI, `IndexTypesUtils`                  |

## Endpoint

`GET /index-api/searchitems` — requires role `searchitem:read`

| Parameter        | Required | Description                                |
|------------------|----------|--------------------------------------------|
| `index_type`     | yes      | The `originType` of the target `IndexType` |
| `origin_id`      | yes      | Business object identifier                 |
| `origin_version` | no       | Optional version filter                    |

On success the response body is the `SearchItem<?>` JSON and the response headers `index-major-version` / `index-minor-version` carry the version of the physical index the item was read from.

## Usage

Implement `SearchItemsProvider` and register it as a Spring bean:

```java
@Component
public class MySearchItemsProvider implements SearchItemsProvider {

    @Override
    public Optional<SearchItemContainer> findSearchItem(
            String indexType, String originId, String originVersion)
            throws SearchItemsBadInputException {

        // look up the item in OpenSearch and return it wrapped in SearchItemContainer
        return Optional.of(new SearchItemContainer(majorVersion, minorVersion, searchItem));
    }
}
```

## Build

```bash
mvn verify
```

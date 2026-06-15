# AGENTS.md — jeap-opensearch-searchitem-api

## Project purpose
Multi-module project providing the REST API and model for exposing indexed search items from OpenSearch in jEAP applications.

## Module layout
- `jeap-opensearch-searchitem-model` — `SearchItemContainer` record: pairs a `SearchItem<?>` with `indexMajorVersion` and `indexMinorVersion`
- `jeap-opensearch-searchitem-api` — `SearchItemsController` (Spring MVC REST), `SearchItemsProvider` (SPI interface), `IndexTypesUtils`, exception types

## Key types
- `SearchItemContainer(int indexMajorVersion, int indexMinorVersion, SearchItem<?> searchItem)` — the model record
- `SearchItemsProvider` — interface applications implement; returns `Optional<SearchItemContainer>`, throws `SearchItemsBadInputException` for invalid input
- `SearchItemsController` — `GET /index-api/searchitems` endpoint; requires role `searchitem:read`; returns `SearchItem<?>` body + `index-major-version`/`index-minor-version` headers

## External dependency
Depends on `jeap-opensearch-index-type`. Version controlled via the `jeap-opensearch-index-type.version` property in the parent pom.

## Build & test
```bash
mvn verify
```

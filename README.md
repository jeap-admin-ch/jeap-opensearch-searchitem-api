# jeap-opensearch-searchitem-api

Spring MVC REST API and model for exposing indexed search items from OpenSearch in jEAP
applications. Domain services implement `SearchItemsProvider` to supply the current search
representation of business objects to the jEAP OpenSearch Index Writer.

## Modules

| Module                             | Description                                                                                             |
|------------------------------------|---------------------------------------------------------------------------------------------------------|
| `jeap-opensearch-searchitem-model` | `SearchItemContainer` record — pairs a `SearchItem` with `indexMajorVersion` and `indexMinorVersion`.   |
| `jeap-opensearch-searchitem-api`   | `SearchItemsController` REST endpoint (`GET /index-api/searchitems`) and the `SearchItemsProvider` SPI. |

## Documentation

- [Getting started](docs/getting-started.md)
- [SearchItem endpoint](docs/endpoint.md)

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap) for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).

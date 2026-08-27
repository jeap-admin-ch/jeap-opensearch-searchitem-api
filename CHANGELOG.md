# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.20.0] - 2026-08-27

### Changed
- Update parent from 9.0.1 to 9.0.3
- update jeap-opensearch-index-type from 1.25.0 to 1.27.0
- Update parent from 9.0.2 to 9.0.3
- update jeap-starter from 24.20.1 to 24.22.0

## [2.19.1] - 2026-08-26
### Changed
- update jeap-starter from 24.20.0 to 24.20.1
- Exclude the AWS SDK Apache 5 HTTP client when using the URL connection client.

## [2.19.0] - 2026-08-22

### Changed
- Update parent from 9.0.0 to 9.0.1
- update jeap-opensearch-index-type from 1.24.0 to 1.25.0
- update jeap-starter from 24.19.0 to 24.20.0

## [2.18.0] - 2026-08-20

### Changed
- Update parent from 8.13.0 to 9.0.0
- update jeap-opensearch-index-type from 1.23.0 to 1.24.0
- update jeap-starter from 24.18.0 to 24.19.0
- Fix failing token introspection when a client id contains colons by URL-encoding the client id and secret before
  using them as basic auth credentials (see RFC 6749).

## [2.17.0] - 2026-08-19

### Changed
- Update parent from 8.12.1 to 8.13.0
- update jeap-opensearch-index-type from 1.22.0 to 1.23.0
- update jeap-starter from 24.17.0 to 24.18.0

## [2.16.0] - 2026-08-19

### Changed
- Update parent from 8.12.0 to 8.12.1
- update jeap-opensearch-index-type from 1.21.0 to 1.22.0
- update jeap-starter from 24.16.0 to 24.17.0

## [2.15.0] - 2026-08-18

### Changed
- Update parent from 8.11.0 to 8.12.0
- update jeap-opensearch-index-type from 1.20.0 to 1.21.0
- update jeap-starter from 24.15.0 to 24.16.0

## [2.14.0] - 2026-08-17

### Changed
- Update parent from 8.10.0 to 8.11.0
- update jeap-opensearch-index-type from 1.19.0 to 1.20.0
- update jeap-starter from 24.14.0 to 24.15.0

## [2.13.0] - 2026-08-13

### Changed
- Update parent from 8.9.1 to 8.10.0
- update jeap-opensearch-index-type from 1.18.0 to 1.19.0
- update jeap-starter from 24.12.0 to 24.14.0

## [2.12.0] - 2026-08-12

### Changed
- Update parent from 8.8.0 to 8.9.1
- update jeap-starter from 24.11.0 to 24.12.0
- update jeap-opensearch-index-type from 1.17.0 to 1.18.0

## [2.11.0] - 2026-08-11

### Changed
- Update parent from 8.7.1 to 8.8.0
- update jeap-opensearch-index-type from 1.16.0 to 1.17.0
- update jeap-starter from 24.10.0 to 24.11.0

## [2.10.0] - 2026-08-10

### Changed
- Update parent from 8.7.0 to 8.7.1
- update jeap-starter from 24.9.0 to 24.10.0
- update aws-advanced-jdbc-wrapper from 4.0.1 to 4.3.0
- update springdoc-openapi from 3.0.3 to 3.1.0
- update jeap-opensearch-index-type from 1.15.0 to 1.16.0

## [2.9.0] - 2026-08-08

### Changed
- Update parent from 8.6.1 to 8.7.0
- update jeap-opensearch-index-type from 1.14.0 to 1.15.0
- update jeap-starter from 24.8.0 to 24.9.0

## [2.8.0] - 2026-08-04

### Changed
- Update parent from 8.6.0 to 8.6.1
- update jeap-opensearch-index-type from 1.13.0 to 1.14.0

## [2.7.0] - 2026-08-01

### Changed
- Update parent from 8.5.6 to 8.6.0
- update jeap-opensearch-index-type from 1.12.0 to 1.13.0
- update jeap-starter from 24.6.1 to 24.7.0

## [2.6.1] - 2026-07-30
### Changed
- update jeap-starter from 24.6.0 to 24.6.1
- `ReadReplicaAwareTransactionManager`: Fixed a race condition in the lazy creation of the transaction counters.
  Transactions started concurrently while the counters were being created could observe a partially initialized
  state and fail with a `NullPointerException`, e.g. when kafka messages are consumed right after startup. Both
  counters are now published together. In addition, a failure to resolve the `MeterRegistry` no longer fails the
  transaction: it is logged once, and the counters are created on a subsequent transaction.

## [2.6.0] - 2026-07-28

### Changed
- Update parent from 8.5.5 to 8.5.6
- update jeap-starter from 24.5.0 to 24.6.0
- update jeap-opensearch-index-type from 1.11.0 to 1.12.0

## [2.5.0] - 2026-07-28
### Changed
- update jeap-starter from 24.4.0 to 24.5.0
- Load the existing monitoring and Actuator defaults early through
  `SpringBootActuatorEndpointActivator`, while retaining lower precedence than application
  configuration. Our working assumption is that loading these defaults later via
  `@PropertySource` allowed Spring Boot 4 to evaluate the Prometheus auto-configuration before the
  endpoint was enabled, so `/actuator/prometheus` was not registered and requests fell through to
  the application's OAuth security chain. The existing `management.endpoint.<id>.enabled`
  properties remain unchanged for backwards compatibility.

## [2.4.0] - 2026-07-25

### Changed
- Update parent from 8.5.4 to 8.5.5
- update jeap-opensearch-index-type from 1.10.0 to 1.11.0
- update jeap-starter from 24.3.0 to 24.4.0

## [2.3.0] - 2026-07-23

### Changed
- Update parent from 8.5.3 to 8.5.4
- update jeap-opensearch-index-type from 1.9.0 to 1.10.0
- update jeap-starter from 24.2.0 to 24.3.0

## [2.2.0] - 2026-07-23

### Changed
- Update parent from 8.5.2 to 8.5.3
- update jeap-opensearch-index-type from 1.8.0 to 1.9.0
- update jeap-starter from 24.1.0 to 24.2.0

## [2.1.0] - 2026-07-22

### Changed
- Update parent from 8.5.0 to 8.5.2
- update jeap-opensearch-index-type from 1.7.0 to 1.8.0
- update jeap-starter from 24.0.0 to 24.1.0

## [2.0.0] - 2026-07-17
### Changed
- update jeap-starter from 23.15.0 to 24.0.0
- Provide the official WireMock Spring Boot integration without exposing WireMock's Jetty dependencies, replacing direct WireMock standalone dependencies across all modules.

## [1.10.0] - 2026-07-15

### Changed
- Update parent from 8.4.0 to 8.5.0
- update jeap-opensearch-index-type from 1.6.0 to 1.7.0
- update jeap-starter from 23.14.0 to 23.15.0

## [1.9.0] - 2026-07-13

### Changed
- Update parent from 8.3.4 to 8.4.0
- update jeap-opensearch-index-type from 1.5.0 to 1.6.0
- update jeap-starter from 23.13.0 to 23.14.0

## [1.8.0] - 2026-07-09
### Changed
- update jeap-starter from 23.12.0 to 23.13.0
- `jeap-spring-boot-security-starter-test`: add named role profiles in `OidcAuthorizationMockServer` via `withRoleProfile(...)`, plus profile switching via `setActiveProfile(...)`.
- `jeap-spring-boot-security-starter-test`: add convenience identity-claim setters in `OidcAuthorizationMockServer` (`withGivenName(...)`, `withFamilyName(...)`, `withName(...)`, `withLocale(...)`) for access token, ID token and userinfo responses.
- `jeap-spring-boot-security-starter-test`: `OidcAuthorizationMockServer.reset()` now restores the default profile and clears runtime OAuth state without rotating the JWKS key.

## [1.7.0] - 2026-07-09
### Changed
- update jeap-starter from 23.11.0 to 23.12.0
- Add OIDC Authorization mock server.

## [1.6.0] - 2026-07-09
### Changed
- update jeap-starter from 23.10.2 to 23.11.0
- `jeap-spring-boot-swagger`: translate the actuator OpenAPI group's title and description from German to English ("Monitoring Endpunkte" → "Monitoring Endpoints")
- Update documentation 

## [1.5.2] - 2026-07-06
### Changed
- update jeap-starter from 23.10.1 to 23.10.2
- Fix deprecated `@Valid` container annotation on `authServers` in `ResourceServerProperties` (Hibernate Validator warning HV000271)

## [1.5.1] - 2026-07-01
### Changed
- update jeap-starter from 23.10.0 to 23.10.1
- Add missing `test` scope to test/mock dependencies (`spring-boot-webmvc-test` in swagger starter, `wiremock-standalone` in security starter)

## [1.5.0] - 2026-06-30

### Changed
- Update parent from 8.3.3 to 8.3.4
- update jeap-starter from 23.9.0 to 23.10.0
- update jeap-opensearch-index-type from 1.4.0 to 1.5.0

## [1.4.0] - 2026-06-23

### Changed
- Update parent from 8.3.2 to 8.3.3
- update jeap-opensearch-index-type from 1.3.0 to 1.4.0
- update jeap-starter from 23.8.0 to 23.9.0

## [1.3.0] - 2026-06-22

### Changed
- Update parent from 8.3.1 to 8.3.2
- update jeap-opensearch-index-type from 1.2.0 to 1.3.0
- update jeap-starter from 23.7.0 to 23.8.0

## [1.2.0] - 2026-06-18

### Changed
- Update parent from 8.3.0 to 8.3.1
- update jeap-opensearch-index-type from 1.1.0 to 1.2.0
- update jeap-starter from 23.6.0 to 23.7.0

## [1.1.0] - 2026-06-17

### Changed
- Update parent from 8.2.0 to 8.3.0
- update jeap-opensearch-index-type from 1.0.0 to 1.1.0
- update starter from 23.5.0 to 23.6.0

## [1.0.0] - 2026-06-15

### Changed
- initial release

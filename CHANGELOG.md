# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

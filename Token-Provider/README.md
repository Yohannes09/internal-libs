# Token-Provider

## Purpose

The `Token-Provider` library provides a robust framework for:

- Generating **JWT tokens** for secure access to internal and external resources.
- Managing and rotating **signing keys** while maintaining a history of public keys for token validation.
- Supporting stateless token issuance without storing user state or role-based access control.

This library is designed for authentication and authorization in distributed systems, offering flexibility for various token recipients and use cases.

---

## Overview

`Token-Provider` is a Spring-based library that issues signed JWT tokens and manages cryptographic keys. Key features include:

- **Pluggable Recipients**: Tokens can be issued for users (via authentication services), internal services (via bootstrap flows), or extended to custom recipient types.
- **Configurable Token Types**: Supports token templates for different use cases (e.g., access tokens, refresh tokens) with customizable validity periods.
- **Secure Key Rotation**: Provides seamless key rotation with a configurable history of public keys to validate tokens issued with older keys.
- **Spring Integration**: Leverages Spring's dependency injection and configuration properties for easy setup and customization.

---

## Key Components

### `SigningKeyManager`
- **Purpose**: Generates and manages signing key pairs (private/public) for token signing and validation.
- **Features**:
  - Thread-safe key rotation with configurable intervals.
  - Maintains an in-memory history of public keys (configurable size) and persists all keys to a `PublicKeyStore` for validating older tokens.
  - Provides methods to check key initialization (`isKeyInitialized`) and rotation status (`isRotationDue`).
- **Usage**: Consumers call `rotateSigningKey()` to initialize or rotate keys based on their security policy.

### `TokenConfigurationProperties`
- **Purpose**: Central configuration for token settings, mapped to the `token.*` prefix in application properties/YAML.
- **Features**:
  - Configures signing key properties (algorithm, key size, rotation interval).
  - Defines token templates (e.g., validity periods) and profiles (e.g., user, service).
  - Validates configuration at startup to ensure compatibility between key rotation and token validity.
- **Validation**: Ensures key rotation intervals and history size support token lifetimes, preventing issues with refresh token validation.

### `TokenFactory`
- **Purpose**: Issues signed JWT tokens with customizable claims.
- **Features**:
  - Integrates with `SigningKeyManager` for cryptographic operations.
  - Supports dynamic claims (e.g., roles, token type, recipient) via a flexible claims map.
  - Uses configuration from `TokenConfigurationProperties` for default claims and signing settings.

---

## Configuration Example

Below is an example YAML configuration for the library:

```yaml
token:
  signing:
    algorithm: RS256
    key-id: auth-key-1
    key-size: 2048
    rotation-interval-minutes: 60
  encoding:
    type: JWT
    compress: false
  validation:
    clock-skew-seconds: 60
  default-claims:
    audience: payme.internal
    issuer: auth.payme.internal
  templates:
    short-lived:
      validity-minutes: 15
    standard:
      validity-minutes: 60
    long-lived:
      validity-minutes: 10080  # 7 days for refresh tokens
  profiles:
    user:
      access-token:
        template: short-lived
      refresh-token:
        template: long-lived
    service:
      access-token:
        template: short-lived
      refresh-token:
        template: long-lived
      initialization-token:
        template: long-lived
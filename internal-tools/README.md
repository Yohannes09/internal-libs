# Common Models

**Version:** 1.0.0

## Overview
The `common-models` project is a shared Maven module containing entity models used across multiple microservices. It promotes code reuse, consistency, and maintainability by ensuring all services reference the same data structures.

## Purpose
- Provides shared JPA entities (e.g., `User`, `Role`)
- Eliminates redundant model definitions across microservices
- Standardizes entity relationships and annotations
- Can be extended to include DTOs and utility models

## Project Structure
- **BaseUser.java** → Common interface for user-related entities
- **User.java** → JPA entity representing application users
- **Role.java** → JPA entity for user roles and permissions

## How to Use
### 1.Install `common-models` Locally
Before using `common-models`, ensure it is installed in your local Maven repository:
```sh
cd path/to/common-models
mvn clean install
```
### 2.Add Dependency to other microservices
```
<dependency>
    <groupId>com.yourcompany.common</groupId>
    <artifactId>common-models</artifactId>
    <version>1.0.0</version>
</dependency>
```
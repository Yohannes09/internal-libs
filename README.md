<div id="top">

<!-- HEADER STYLE: CLASSIC -->
<div align="center">


# INTERNAL-LIBS

<em>Empowering Secure Connections Across Your Ecosystem</em>

<!-- BADGES -->
<img src="https://img.shields.io/github/last-commit/Yohannes09/internal-libs?style=flat&logo=git&logoColor=white&color=0080ff" alt="last-commit">
<img src="https://img.shields.io/github/languages/top/Yohannes09/internal-libs?style=flat&color=0080ff" alt="repo-top-language">
<img src="https://img.shields.io/github/languages/count/Yohannes09/internal-libs?style=flat&color=0080ff" alt="repo-language-count">

<em>Built with the tools and technologies:</em>

<img src="https://img.shields.io/badge/Markdown-000000.svg?style=flat&logo=Markdown&logoColor=white" alt="Markdown">
<img src="https://img.shields.io/badge/Spring-000000.svg?style=flat&logo=Spring&logoColor=white" alt="Spring">
<img src="https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white" alt="Docker">
<img src="https://img.shields.io/badge/XML-005FAD.svg?style=flat&logo=XML&logoColor=white" alt="XML">

</div>
<br>

---

## Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Usage](#usage)
    - [Testing](#testing)

---

## Overview

**internal-libs** is a powerful toolkit designed to streamline development within the PayMe ecosystem, offering robust solutions for secure token management and standardized models.

**Why internal-libs?**

This project enhances code quality and security across microservices. The core features include:

- 🔑 **Standardized JPA Entities:** Boosts code reuse and maintainability with common models like User and Role.
- 🔒 **Secure Token Management:** Facilitates JWT token generation and validation for robust authentication and authorization.
- 🔄 **Key Rotation Support:** Simplifies key management, ensuring security in distributed systems with seamless key rotation.
- ⚙️ **Centralized Exception Handling:** Improves user experience by providing clear error responses through a global handler.
- 🌐 **Integration with Spring Boot:** Streamlines configuration and enhances functionality across the application architecture.

---

## Getting Started

### Prerequisites

This project requires the following dependencies:

- **Programming Language:** Java
- **Package Manager:** Maven
- **Container Runtime:** Docker

### Installation

Build internal-libs from the source and intsall dependencies:

1. **Clone the repository:**

    ```sh
    ❯ git clone https://github.com/Yohannes09/internal-libs
    ```

2. **Navigate to the project directory:**

    ```sh
    ❯ cd internal-libs
    ```

3. **Install the dependencies:**

**Using [docker](https://www.docker.com/):**

```sh
❯ docker build -t Yohannes09/internal-libs .
```
**Using [maven](https://maven.apache.org/):**

```sh
❯ mvn install
```

### Usage

Run the project with:

**Using [docker](https://www.docker.com/):**

```sh
docker run -it {image_name}
```
**Using [maven](https://maven.apache.org/):**

```sh
mvn exec:java
```

### Testing

Internal-libs uses the {__test_framework__} test framework. Run the test suite with:

**Using [docker](https://www.docker.com/):**

```sh
echo 'INSERT-TEST-COMMAND-HERE'
```
**Using [maven](https://maven.apache.org/):**

```sh
mvn test
```

---

<div align="left"><a href="#top">⬆ Return</a></div>

---

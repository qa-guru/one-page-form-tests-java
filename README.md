# One Page Form — UI Tests (Java)

Automated browser tests for the static HTML demos in [qa-guru/one-page-form](https://github.com/qa-guru/one-page-form).

Tests use [Selenide](https://selenide.org/) and JUnit 5. Allure 3 reports are published to GitHub Pages from this repository.

[![UI Tests](https://qa-guru.github.io/one-page-form-tests-java/readme/badge.svg)](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)

## UI Tests Dashboard

[![UI Tests stats](https://qa-guru.github.io/one-page-form-tests-java/readme/stats.svg)](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)

<a href="https://qa-guru.github.io/one-page-form-tests-java/allure-reports/">
  <img
    src="https://qa-guru.github.io/one-page-form-tests-java/readme/dashboard-preview.png"
    alt="Allure 3 dashboard preview with branch trends"
    width="100%"
  />
</a>

> Preview and badges update automatically after each CI run on `main`.

| Link | Description |
|------|-------------|
| [Overview dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/) | Trends across all branches |
| [main dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/main/) | Latest `main` branch analytics |
| [Demo landing](https://qa-guru.github.io/one-page-form/) | Demo pages with embedded dashboard |

## Prerequisites

- Java 21
- Google Chrome installed locally
- Demo pages checked out as a sibling directory:

```text
IdeaProjects/
├── one-page-form/              # HTML demos
└── one-page-form-tests-java/   # this repository
```

## Run tests

From this directory:

```bash
./gradlew test
```

Run a single test class:

```bash
./gradlew test --tests LoginTests
./gradlew test --tests TextBoxTests
./gradlew test --tests RegistrationTests
```

Open the HTML report after a run:

```bash
open build/reports/tests/test/index.html
```

## Configuration

Browser settings live in `src/test/java/tests/TestBase.java`.

By default, local runs open pages from `../one-page-form/`. Override with:

```bash
./gradlew test -DbaseUrl=https://qa-guru.github.io/one-page-form/
```

## What is tested

| Test class          | Page                            | Scenarios |
|---------------------|---------------------------------|-----------|
| `LoginTests`        | `login.html`                    | Successful login, wrong password |
| `TextBoxTests`      | `text-box.html`                 | Fill form and verify output |
| `RegistrationTests` | `automation-practice-form.html` | Full registration form |

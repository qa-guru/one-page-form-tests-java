# One Page Form — UI Tests (Java)

Automated browser tests for the static HTML demos in [qa-guru/one-page-form](https://github.com/qa-guru/one-page-form).

Tests use [Selenide](https://selenide.org/) and JUnit 5. Allure reports are published to GitHub Pages from this repository.

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

## Allure reports (CI)

Published to GitHub Pages:

- [Overview dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)
- [main dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/main/)

Demo pages:

- [https://qa-guru.github.io/one-page-form/](https://qa-guru.github.io/one-page-form/)

## What is tested

| Test class          | Page                            | Scenarios |
|---------------------|---------------------------------|-----------|
| `LoginTests`        | `login.html`                    | Successful login, wrong password |
| `TextBoxTests`      | `text-box.html`                 | Fill form and verify output |
| `RegistrationTests` | `automation-practice-form.html` | Full registration form |

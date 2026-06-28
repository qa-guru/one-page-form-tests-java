<!-- Canonical metrics block for README files.
     Sync this section into:
       - one-page-form-tests-java/README.md  (relative asset paths)
       - one-page-form/README.md             (ASSETS_BASE = GitHub Pages URL)

     Asset paths in CI: pages/readme/ on gh-pages branch.
     Generator: .github/scripts/generate-readme-badge.sh
-->

## Automated Tests Dashboard

Three live SVG layers (compact → detailed → full panel), updated after each CI run on `main`:

| Layer | File | Use |
|-------|------|-----|
| Badge | `badge.svg` | Top of README, inline status |
| Stats | `stats.svg` | Breakdown bar + legend |
| Panel | `metrics-panel.svg` | Full metrics: total, pass rate, duration, stack |

[![UI Tests](readme/badge.svg)](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)

[![UI Tests stats](readme/stats.svg)](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)

[![UI Tests metrics](readme/metrics-panel.svg)](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/)

<a href="https://qa-guru.github.io/one-page-form-tests-java/allure-reports/">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="readme/dashboard-preview-dark.png">
    <img
      src="readme/dashboard-preview.png"
      alt="Allure 3 dashboard preview with branch trends"
      width="800"
    />
  </picture>
</a>

> Preview and badges update automatically after each CI run on `main`.

| Link | Description |
|------|-------------|
| [Overview dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/) | Trends across all branches |
| [main dashboard](https://qa-guru.github.io/one-page-form-tests-java/allure-reports/main/) | Latest `main` branch analytics |
| [Demo landing](https://qa-guru.github.io/one-page-form/) | Demo pages with embedded dashboard |

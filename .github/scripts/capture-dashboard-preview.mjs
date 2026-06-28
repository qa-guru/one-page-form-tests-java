import { chromium } from "playwright";
import { mkdir } from "node:fs/promises";
import { dirname } from "node:path";

const baseUrl =
  process.env.PREVIEW_URL ??
  "http://127.0.0.1:8765/allure-reports/main/dashboard/index.html";
const output =
  process.env.PREVIEW_OUTPUT ?? "pages/readme/dashboard-preview.png";
const viewportWidth = Number(process.env.PREVIEW_WIDTH ?? "1280");

await mkdir(dirname(output), { recursive: true });

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({
  viewport: { width: viewportWidth, height: 900 },
  deviceScaleFactor: 2,
});

try {
  await page.goto(baseUrl, { waitUntil: "networkidle", timeout: 90_000 });
  await page.waitForSelector('[data-testid="base-layout"]', {
    timeout: 30_000,
  });
  await page.waitForTimeout(3_000);
  await page.screenshot({ path: output, type: "png" });
  console.log(`Saved dashboard preview to ${output}`);
} finally {
  await browser.close();
}

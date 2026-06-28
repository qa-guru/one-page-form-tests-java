(function () {
  const RESIZE_MESSAGE = "allure-shell:resize";
  const RESPONSIVE_BREAKPOINT_PX = 768;
  const LAYOUT_SYNC_STYLE_ID = "shell-layout-sync";
  const NARROW_DASHBOARD_LAYOUT_CSS = `
[class*="styles_grid__"] {
  grid-template-columns: 1fr !important;
  width: 100% !important;
}
[class*="styles_grid-item__"] {
  grid-template-columns: 1fr !important;
  width: 100% !important;
}
[class*="styles_widget__"] {
  width: 100% !important;
  max-width: 100% !important;
}
`;
  const BASE_SCRIPT_RE =
    /<script>\s*const \{ origin, pathname \} = window\.location;[\s\S]*?appendChild\(baseEl\);\s*<\/script>\s*/m;

  function measureDocument(doc) {
    if (!doc) return 0;
    return Math.max(
      doc.documentElement?.scrollHeight || 0,
      doc.body?.scrollHeight || 0,
      doc.documentElement?.offsetHeight || 0
    );
  }

  function measureFrame(frame) {
    try {
      const doc = frame.contentDocument || frame.contentWindow?.document;
      return measureDocument(doc);
    } catch {
      return 0;
    }
  }

  function notifyParentResize() {
    if (window.parent === window) return;
    window.parent.postMessage(
      { type: RESIZE_MESSAGE, height: measureDocument(document) },
      "*"
    );
  }

  function resizeFrame(frame) {
    const height = measureFrame(frame);
    if (height > 0) {
      frame.style.height = `${height}px`;
      frame.style.minHeight = "0";
    }
    notifyParentResize();
  }

  function getDashboardDocument(frame) {
    if (!frame) return null;
    try {
      return frame.contentDocument || frame.contentWindow?.document || null;
    } catch {
      return null;
    }
  }

  function readStoredTheme(rawValue) {
    if (!rawValue) return null;
    try {
      return JSON.parse(rawValue);
    } catch {
      return rawValue.replace(/^"|"$/g, "");
    }
  }

  function applySiteTheme(theme) {
    const normalized = theme === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", normalized);
    return normalized;
  }

  function initSiteTheme() {
    applySiteTheme(localStorage.getItem("site-theme") === "dark" ? "dark" : "light");
  }

  function getDashboardTheme(frame) {
    const doc = getDashboardDocument(frame);
    if (doc) {
      const stored = readStoredTheme(doc.defaultView?.localStorage?.getItem("theme"));
      if (stored === "dark" || stored === "light") {
        return stored;
      }
      const attrTheme = doc.documentElement.getAttribute("data-theme");
      if (attrTheme === "dark" || attrTheme === "light") {
        return attrTheme;
      }
    }

    const siteTheme = localStorage.getItem("site-theme");
    return siteTheme === "dark" ? "dark" : "light";
  }

  function applyDashboardTheme(frame, theme) {
    const normalized = theme === "dark" ? "dark" : "light";
    localStorage.setItem("site-theme", normalized);
    applySiteTheme(normalized);

    const doc = getDashboardDocument(frame);
    if (doc) {
      doc.documentElement.setAttribute("data-theme", normalized);
      doc.defaultView?.localStorage?.setItem("theme", JSON.stringify(normalized));
    }

    window.dispatchEvent(
      new CustomEvent("dashboard-theme-change", { detail: { theme: normalized } })
    );
    return normalized;
  }

  function toggleDashboardTheme(frame) {
    const nextTheme = getDashboardTheme(frame) === "dark" ? "light" : "dark";
    return applyDashboardTheme(frame, nextTheme);
  }

  function isNarrowShellLayout() {
    return window.matchMedia(`(max-width: ${RESPONSIVE_BREAKPOINT_PX}px)`).matches;
  }

  function applyDashboardLayout(frame) {
    const doc = getDashboardDocument(frame);
    if (!doc) return;

    let style = doc.getElementById(LAYOUT_SYNC_STYLE_ID);
    if (!style) {
      style = doc.createElement("style");
      style.id = LAYOUT_SYNC_STYLE_ID;
      doc.head.appendChild(style);
    }
    style.textContent = isNarrowShellLayout() ? NARROW_DASHBOARD_LAYOUT_CSS : "";
    resizeFrame(frame);
  }

  function syncDashboardLayouts() {
    document.querySelectorAll("iframe.dashboard-frame").forEach(applyDashboardLayout);
  }

  function getOverridesUrl() {
    const shellScript = document.querySelector('script[src*="allure-shell.js"]');
    if (shellScript?.src) {
      return new URL("dashboard-overrides.css", shellScript.src).href;
    }
    return new URL("dashboard-overrides.css", document.baseURI).href;
  }

  function prepareDashboardHtml(html, dashboardUrl, overridesUrl) {
    const dashboardBase = new URL("./", dashboardUrl).href;
    let patched = html.replace(BASE_SCRIPT_RE, "");

    if (!patched.includes("dashboard-overrides")) {
      const overrideTag = `<link rel="stylesheet" type="text/css" href="${overridesUrl}" data-dashboard-overrides>`;
      patched = patched.replace("</head>", `    ${overrideTag}\n</head>`);
    }

    if (!/<base\s/i.test(patched)) {
      patched = patched.replace("<head>", `<head>\n    <base href="${dashboardBase}">`);
    }

    return patched;
  }

  async function loadDashboardFrame(frame, dashboardUrl) {
    const absoluteDashboardUrl = new URL(dashboardUrl, document.baseURI).href;
    const overridesUrl = getOverridesUrl();

    frame.dataset.dashboardUrl = absoluteDashboardUrl;

    try {
      const response = await fetch(absoluteDashboardUrl, { cache: "no-cache" });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const html = await response.text();
      frame.removeAttribute("src");
      frame.srcdoc = prepareDashboardHtml(html, absoluteDashboardUrl, overridesUrl);
    } catch {
      frame.removeAttribute("srcdoc");
      frame.src = absoluteDashboardUrl;
    }
  }

  function setupFrame(frame) {
    frame.setAttribute("scrolling", "no");

    const onLoad = () => {
      applyDashboardTheme(frame, getDashboardTheme(frame));
      applyDashboardLayout(frame);
      resizeFrame(frame);
      [300, 800, 1500, 3000].forEach((delay) => {
        window.setTimeout(() => resizeFrame(frame), delay);
      });
    };

    frame.addEventListener("load", onLoad);
    try {
      if (frame.contentDocument?.readyState === "complete") {
        onLoad();
      }
    } catch {
      // ignore cross-origin access errors
    }
  }

  function initDashboardFrames() {
    document.querySelectorAll("iframe.dashboard-frame").forEach((frame) => {
      if (frame.dataset.dashboardReady === "true") {
        setupFrame(frame);
        return;
      }

      const dashboardUrl = frame.dataset.dashboardUrl || frame.getAttribute("src");
      if (dashboardUrl) {
        loadDashboardFrame(frame, dashboardUrl).finally(() => {
          frame.dataset.dashboardReady = "true";
          setupFrame(frame);
        });
        return;
      }
      setupFrame(frame);
    });
    notifyParentResize();
  }

  window.addEventListener("message", (event) => {
    if (event.data?.type !== RESIZE_MESSAGE) return;

    document.querySelectorAll("iframe.dashboard-frame").forEach((frame) => {
      try {
        if (frame.contentWindow === event.source) {
          const height = event.data.height;
          if (height > 0) {
            frame.style.height = `${height}px`;
            frame.style.minHeight = "0";
          }
        }
      } catch {
        // ignore
      }
    });

    notifyParentResize();
  });

  initSiteTheme();

  window.AllureShell = {
    loadDashboardFrame,
    resizeFrame,
    applySiteTheme,
    applyDashboardTheme,
    getDashboardTheme,
    toggleDashboardTheme,
    responsiveBreakpointPx: RESPONSIVE_BREAKPOINT_PX,
    isNarrowShellLayout,
    syncDashboardLayouts,
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initDashboardFrames);
  } else {
    initDashboardFrames();
  }

  window.addEventListener("resize", () => {
    syncDashboardLayouts();
    document.querySelectorAll("iframe.dashboard-frame").forEach(resizeFrame);
    notifyParentResize();
  });
})();

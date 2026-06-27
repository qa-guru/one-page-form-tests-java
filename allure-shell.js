(function () {
  const RESIZE_MESSAGE = "allure-shell:resize";

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

  function setupFrame(frame) {
    frame.setAttribute("scrolling", "no");

    const onLoad = () => {
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

  function init() {
    document.querySelectorAll("iframe.dashboard-frame").forEach(setupFrame);
    notifyParentResize();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  window.addEventListener("resize", () => {
    document.querySelectorAll("iframe.dashboard-frame").forEach(resizeFrame);
    notifyParentResize();
  });
})();

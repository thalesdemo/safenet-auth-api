document.addEventListener("DOMContentLoaded", function () {
  // Add custom CSS to hide elements
  const style = document.createElement("style");
  style.textContent = `
        .swagger-ui .topbar { display: none !important; }
        .swagger-ui .servers { display: none !important; }
        .swagger-ui .servers-title { display: none !important; }
        `;
  document.head.append(style);
});

window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: "",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout" ,

  "configUrl" : "/v3/api-docs/swagger-config",
  "validatorUrl" : ""

  });

  //</editor-fold>
};

// Custom JS to hide elements
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

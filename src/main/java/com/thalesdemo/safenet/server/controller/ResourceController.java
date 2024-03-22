package com.thalesdemo.safenet.server.controller;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ResourceController {

    private static final Set<String> SWAGGER_RESOURCES = Set.of(
            "swagger-ui-bundle.js",
            "swagger-ui-bundle.js.map",
            "swagger-ui-standalone-preset.js",
            "swagger-ui-standalone-preset.js.map",
            "swagger-ui.css",
            "swagger-ui.css.map",
            "index.css",
            "index.html");

    private static final Set<String> STATIC_RESOURCES = Set.of(
            "custom-script.js",
            "custom-style.css",
            "favicon-32x32.png",
            "favicon-16x16.png",
            "favicon.ico",
            "swagger-initializer.js");


    // Handles requests for the Swagger UI resources based on allowed file names.
    // Catches the swagger-initializer.js file and forwards it to the custom folder.
    // NOTE: This method may need to be revised if ever the original Swagger UI resources change 
    // and no longer point to relative paths (i.e., without /swagger-ui).
    @GetMapping("/{filename:.+}")
    public String forwardSwaggerAndCustomStaticResources(@PathVariable String filename, HttpServletResponse response) {

        if (SWAGGER_RESOURCES.contains(filename)) {
            return "forward:/swagger-ui/" + filename;
        }

        if (STATIC_RESOURCES.contains(filename)) {
            return "forward:/custom/" + filename;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        throw new ResourceNotFoundException();
    }

}
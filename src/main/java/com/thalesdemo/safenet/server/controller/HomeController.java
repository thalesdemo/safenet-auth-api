package com.thalesdemo.safenet.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectSwaggerUi(HttpServletRequest request) {
    // Forward to /swagger-ui/index.html without changing the URL in the browser
    request.setAttribute("internalForward", true);
    return "forward:/swagger-ui/index.html";
    }

}

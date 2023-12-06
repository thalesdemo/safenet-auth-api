// package com.thalesdemo.safenet.auth.api;

// import java.nio.charset.Charset;
// import java.nio.charset.StandardCharsets;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.client.RestTemplate;

// import com.thalesdemo.safenet.token.list.api.SOAPClientService;
// import com.thalesdemo.safenet.token.list.api.SOAPConfiguration;

// @RestController
// class TestController {

//     @Autowired
//     private SOAPClientService soapClientService;

//     @GetMapping(value = "/api/test/{username}", produces = "application/soap+xml; charset=utf-8")
//     public String test(@PathVariable String username) {
//         RestTemplate restTemplate = new RestTemplate();

//         try {
//             SOAPConfiguration configuration = soapClientService.getConfiguration();
//             String baseUrl = configuration.getBaseUrl();
//             List<String> cookiesList = configuration.getCookies();

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(new MediaType("application", "soap+xml", Charset.forName("UTF-8")));
//             // Assuming cookies are in "name=value" format, joining them with ";"
//             if (cookiesList != null && !cookiesList.isEmpty()) {
//                 headers.set("Cookie", String.join("; ", cookiesList));
//             }

//             String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
//                     + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
//                     + "  <soap12:Body>"
//                     + "    <GetTokensByOwner xmlns=\"http://www.cryptocard.com/blackshield/\">"
//                     + "      <userName>" + username + "</userName>"
//                     + "      <organization>Cina Demo</organization>"
//                     + "    </GetTokensByOwner>"
//                     + "  </soap12:Body>"
//                     + "</soap12:Envelope>";

//             HttpEntity<String> request = new HttpEntity<>(body, headers);

//             String response = restTemplate.postForObject(baseUrl, request, String.class);
//             return response;

//         } catch (Exception e) {
//             // Handle the exception gracefully
//             return "Error occurred: " + e.getMessage();
//         }
//     }
// }

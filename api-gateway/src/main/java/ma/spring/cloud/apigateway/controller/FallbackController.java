package ma.spring.cloud.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer plus tard.");
        response.put("service", "user-service");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/registration-service")
    public ResponseEntity<Map<String, Object>> registrationServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Le service d'inscription est temporairement indisponible. Veuillez réessayer plus tard.");
        response.put("service", "registration-service");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> generalFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Le service demandé est temporairement indisponible.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

//package ma.spring.cloud.apigateway.controller;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/fallback")
//public class FallbackController {
//
//    @GetMapping("/user-service")
//    public ResponseEntity<Map<String, Object>> userServiceFallback() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
//        response.put("error", "Service Unavailable");
//        response.put("message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer plus tard.");
//        response.put("service", "user-service");
//
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
//    }
//
//    @GetMapping("/general")
//    public ResponseEntity<Map<String, Object>> generalFallback() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
//        response.put("error", "Service Unavailable");
//        response.put("message", "Le service demandé est temporairement indisponible.");
//
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
//    }
//}
////package ma.spring.cloud.apigateway.controller;
////
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.GetMapping;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RestController;
////
////import java.time.LocalDateTime;
////import java.util.HashMap;
////import java.util.Map;
////
////@RestController
////@RequestMapping("/fallback")
////public class FallbackController {
////
////    @GetMapping("/user-service")
////    public ResponseEntity<Map<String, Object>> userServiceFallback() {
////        Map<String, Object> response = new HashMap<>();
////        response.put("timestamp", LocalDateTime.now());
////        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
////        response.put("error", "Service Unavailable");
////        response.put("message", "Le service d'authentification est temporairement indisponible. Veuillez réessayer plus tard.");
////        response.put("service", "user-service");
////
////        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
////    }
////
////    @GetMapping("/general")
////    public ResponseEntity<Map<String, Object>> generalFallback() {
////        Map<String, Object> response = new HashMap<>();
////        response.put("timestamp", LocalDateTime.now());
////        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
////        response.put("error", "Service Unavailable");
////        response.put("message", "Le service demandé est temporairement indisponible.");
////
////        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
////    }
////}
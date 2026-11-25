
package com.careerpilot.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping("/test")
    public ResponseEntity<String> test(Authentication authentication) {
        return ResponseEntity.ok(
                "Admin access granted. User ID: " + authentication.getName()
        );
    }
}

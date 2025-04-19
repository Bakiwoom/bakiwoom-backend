package com.javaex.idea.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@PostMapping("/signup")
	public String ping1() {
        return "pong";
    }
	
	@PostMapping("/login")
	public String ping2() {
        return "pong";
    }

}

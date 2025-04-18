package com.javaex.idea.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserMyPageController {
	
	@GetMapping("/profile")
	public String ping() {
        return "pong";
    }

}

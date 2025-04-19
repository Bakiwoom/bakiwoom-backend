package com.javaex.idea.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/me")
public class CompanyPageController {
	
	@GetMapping("/profile")
	public String ping() {
        return "pong";
    }

}

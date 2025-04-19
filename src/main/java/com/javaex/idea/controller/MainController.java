package com.javaex.idea.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
public class MainController {
	
	@GetMapping("/jobs")
	public String ping() {
        return "ping";
    }

}

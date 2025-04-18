package com.javaex.idea.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobInformationController {
	
	@GetMapping("/list")
	public String ping() {
        return "pong";
    }

}

package com.enterprise.costiq.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Welcome to CostIQ");
        return "public/home";
    }

    @GetMapping("/public/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About CostIQ");
        return "public/about";
    }
}

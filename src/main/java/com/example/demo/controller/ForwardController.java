package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardController {

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String forward() {
        // Forward any path without a dot (not a file) to index.html
        return "forward:/index.html";
    }
}

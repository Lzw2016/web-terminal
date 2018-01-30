package org.pty4j.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "home";
    }

    @RequestMapping("/xterm")
    public String xterm() {
        return "xterm";
    }

    @RequestMapping("/terminado")
    public String terminado() {
        return "terminado";
    }
}

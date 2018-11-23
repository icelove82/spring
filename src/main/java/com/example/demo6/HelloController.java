package com.example.demo6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        //return "Hello, World! Im here!";
        StringBuffer sb = new StringBuffer();
        sb.append("<h1>OWA~ Its amazing!</h1>");
        return sb.toString();
    }

    @RequestMapping("/getname")
    public String getName() {
        return "My name is YunMyeonghun";
    }
    
    @RequestMapping("/getteamname")
    public String getTeamName() {
        return "The team name is UI Team!";
    }
    
}

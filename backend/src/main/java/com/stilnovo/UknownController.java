package com.stilnovo;

import org.springframework.stereotype.Controller;


@Controller
public class UknownController {
    
    @GetMapping("/uknown")
    public String uknown(Model model){

        model.addAllAttributes("name", "World"); //{{Clave}}, Su-valor

        return "uknown_template"; //vista html con esa info
    }
}
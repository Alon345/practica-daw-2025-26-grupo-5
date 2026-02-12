package es.stilnovo.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    @GetMapping("/payment-page")
    public String showPaymentPage(Model model) {
        // Aquí puedes añadir lógica en el futuro (precio final, etc.)
        return "payment-page"; 
    }
}
package es.stilnovo.library.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.model.Inquiry;
import es.stilnovo.library.repository.InquiryRepository;
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.service.MailService;
import es.stilnovo.library.service.ProductService;
import jakarta.mail.MessagingException;

@Controller
public class NotificationController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private ResourceLoader resourceLoader; // To load the logo from classpath

    //  MAL--> /api/v1/notifications/send-inquiry
    //  BIEN --> /notifications/send-inquiry
    @PostMapping("/notifications/send-inquiry")
    public String sendInquiry(@RequestParam long productId,
                                @RequestParam(required = false) String phone,
                                @RequestParam String type,
                                @RequestParam String message,
                                Principal principal) {
        
        // 1. Validate Product and User authentication
        Product product = productService.findById(productId).orElseThrow();
        if (principal == null) {
            return "redirect:/contact-seller-page/" + productId + "?error=auth";
        }

        User buyer = userRepository.findByName(principal.getName()).orElse(null);
        if (buyer == null) {
            return "redirect:/contact-seller-page/" + productId + "?error=auth";
        }

        // 2. Cooldown Logic: Prevent spam (30 minutes wait)
        Inquiry lastInquiry = inquiryRepository
                .findTopByBuyerIdAndProductIdOrderByCreatedAtDesc(buyer.getUserId(), product.getId());
        if (lastInquiry != null) {
            long secondsSince = Duration.between(lastInquiry.getCreatedAt(), LocalDateTime.now()).getSeconds();
            long cooldown = 1800 - secondsSince;
            if (cooldown > 0) {
                long minutes = (long) Math.ceil(cooldown / 60.0);
                return "redirect:/contact-seller-page/" + productId + "?cooldown=" + minutes;
            }
        }

        // 3. Prepare Email Resources (Logo)
        Resource logoResource = resourceLoader.getResource("classpath:static/images/logo.png");
        String logoCid = "stilnovoLogo";
        String sellerEmail = product.getSeller().getEmail();
        String phoneValue = (phone == null || phone.isBlank()) ? "Not provided" : phone;

        // 4. Generate Professional HTML Templates
        String sellerHtml = MailTemplates.proSellerInquiry(
                product.getId(), product.getName(), type, message, 
                buyer.getName(), buyer.getEmail(), phoneValue, logoCid
        );
        String buyerHtml = MailTemplates.buyerConfirmation(product.getName(), type, message);

        // 5. Create Inquiry Record for Database
        Inquiry inquiry = new Inquiry();
        inquiry.setProductId(product.getId());
        inquiry.setProductName(product.getName());
        inquiry.setSellerId(product.getSeller().getUserId());
        inquiry.setSellerEmail(sellerEmail);
        inquiry.setBuyerId(buyer.getUserId());
        inquiry.setBuyerName(buyer.getName());
        inquiry.setBuyerEmail(buyer.getEmail());
        inquiry.setBuyerPhone(phoneValue);
        inquiry.setInquiryType(type);
        inquiry.setMessage(message);
        inquiry.setCreatedAt(LocalDateTime.now());

        // 6. Send Emails and Save Status
        try {
            mailService.sendHtmlWithInline(sellerEmail, "New Inquiry: " + product.getName(), sellerHtml, logoCid, logoResource);
            mailService.sendHtml(buyer.getEmail(), "Confirmation: Message sent to seller", buyerHtml);
            
            inquiry.setStatus("SENT");
            inquiryRepository.save(inquiry);
            return "redirect:/contact-seller-page/" + productId + "?sent=true";

        } catch (MailException | MessagingException ex) {
            inquiry.setStatus("FAILED_MAIL");
            inquiryRepository.save(inquiry);
            return "redirect:/contact-seller-page/" + productId + "?error=mail";
        }
    }

    /**
     * Inner class to manage HTML Email Templates with inline CSS for compatibility.
     */
    private static class MailTemplates {
        
        private static String proSellerInquiry(long productId, String productName, String type, String message,
                                                String buyerName, String buyerEmail, String phone, String logoCid) {
            return """
                <!DOCTYPE html>
                <html>
                <body style="margin: 0; padding: 0; background-color: #f4f7f6;">
                    <div style="font-family: Arial, sans-serif; color: #1a1f2e; max-width: 600px; margin: 20px auto; border: 1px solid #e6e9f2; border-radius: 16px; background-color: #ffffff; overflow: hidden;">
                        <div style="background-color: #ffffff; padding: 30px; text-align: center; border-bottom: 1px solid #f0f0f0;">
                            <img src="cid:%s" alt="Stilnovo" width="60" style="display: block; margin: 0 auto;">
                            <h1 style="color: #2f6ced; margin: 15px 0 0; font-size: 24px;">New Inquiry Received!</h1>
                        </div>
                        <div style="padding: 30px;">
                            <p style="font-size: 16px;">Good news! Someone is interested in your treasure:</p>
                            <h2 style="margin: 10px 0; font-size: 20px; color: #1a1f2e;">%s</h2>
                            
                            <div style="background-color: #eef4ff; padding: 20px; border-radius: 12px; margin: 25px 0;">
                                <p style="margin: 0 0 10px 0; font-weight: bold; color: #2f6ced;">Buyer Contact Information:</p>
                                <ul style="list-style: none; padding: 0; margin: 0; font-size: 14px; line-height: 1.8;">
                                    <li>👤 <strong>Name:</strong> %s</li>
                                    <li>✉️ <strong>Email:</strong> <a href="mailto:%s" style="color: #2f6ced; text-decoration: none;">%s</a></li>
                                    <li>📞 <strong>Phone:</strong> %s</li>
                                </ul>
                            </div>

                            <p><strong>Inquiry type:</strong> %s</p>
                            <div style="background: #ffffff; border-left: 4px solid #2f6ced; padding: 15px; border-radius: 4px; margin: 20px 0; font-style: italic; background-color: #f9fbff;">
                                "%s"
                            </div>

                            <div style="text-align: center; margin-top: 35px;">
                                <a href="https://localhost:8443/info-product-page/%d" 
                                    style="background-color: #2f6ced; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 30px; font-weight: bold; display: inline-block;">
                                    View Product & Reply
                                </a>
                            </div>
                        </div>
                        <div style="padding: 20px; text-align: center; font-size: 12px; color: #888; background-color: #fafafa; border-top: 1px solid #f0f0f0;">
                            <p>© 2026 Stilnovo Marketplace • Giving design a second life.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(logoCid, escape(productName), escape(buyerName), escape(buyerEmail), escape(buyerEmail), escape(phone), escape(type), escape(message), productId);
        }

        private static String buyerConfirmation(String productName, String type, String message) {
            return """
                <div style="font-family: Arial, sans-serif; color: #1a1f2e; padding: 20px; border: 1px solid #e6e9f2; border-radius: 12px;">
                    <h2 style="color:#2f6ced;">We've received your inquiry</h2>
                    <p>Your message has been sent to the seller of <strong>%s</strong>.</p>
                    <p><strong>Type:</strong> %s</p>
                    <hr style="border:none; border-top:1px solid #eee; margin: 20px 0;">
                    <p style="font-style: italic; color: #555;">"%s"</p>
                    <p style="font-size: 12px; color: #888; margin-top: 30px;">Thanks for using Stilnovo.</p>
                </div>
                """.formatted(escape(productName), escape(type), escape(message));
        }

        private static String escape(String value) {
            if (value == null) return "";
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
        }
    }
}
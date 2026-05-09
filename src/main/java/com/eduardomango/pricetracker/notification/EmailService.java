package com.eduardomango.pricetracker.notification;

import com.eduardomango.pricetracker.common.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPriceAlertEmail(String to, String productName, Price targetPrice, Price currentPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Price Alert: " + productName);
            message.setText(String.format(
                    "Hello,\n\nThe price for '%s' has reached your alert threshold.\n" +
                    "Your target: %s %s\n" +
                    "Current price: %s %s\n\n" +
                    "Happy shopping!\nPriceTracker Team",
                    productName, 
                    targetPrice.amount(), targetPrice.currency(),
                    currentPrice.amount(), currentPrice.currency()
            ));

            mailSender.send(message);
            log.info("Price alert email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}

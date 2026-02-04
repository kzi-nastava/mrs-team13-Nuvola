package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // Method 1
    // To send a simple email
    public String sendSimpleMail(EmailDetails details) {
        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Method 2
    // To send an email with attachment
    public String sendMailWithAttachment(EmailDetails details) {
        // Creating a mime message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachments to
            // be send
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(
                    details.getSubject());

            // Adding the attachment
            FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));

            mimeMessageHelper.addAttachment(
                    file.getFilename(), file);

            // Sending the mail
            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {

            // Display message when exception occurred
            return "Error while sending mail!!!";
        }
    }

    public String sendTrackingPage(EmailDetails details) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setSubject(details.getSubject());

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>" +
                    "<table role='presentation' style='width: 100%; border-collapse: collapse;'>" +
                    "<tr>" +
                    "<td style='padding: 40px 0; text-align: center;'>" +
                    "<table role='presentation' style='width: 600px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +
                    "<tr>" +
                    "<td style='padding: 40px 30px; background: #0A1128; text-align: center;'>" +
                    "<h1 style='margin: 0; color: #ffffff; font-size: 28px;'>Nuvola Ride Tracking</h1>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 40px 30px;'>" +
                    "<h2 style='margin: 0 0 20px 0; color: #333333; font-size: 24px;'>Hello!</h2>" +
                    "<p style='margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;'>" +
                    "Your ride is on the way! Click the button below to track your ride in real-time." +
                    "</p>" +
                    "<table role='presentation' style='margin: 30px 0;'>" +
                    "<tr>" +
                    "<td style='border-radius: 4px; background: #0A1128;'>" +
                    "<a href='" + details.getLink() + "' style='display: inline-block; padding: 16px 36px; font-size: 16px; color: #ffffff; text-decoration: none; font-weight: bold;'>" +
                    "Track Your Ride" +
                    "</a>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "<p style='margin: 20px 0 0 0; color: #999999; font-size: 14px; line-height: 1.6;'>" +
                    "If the button doesn't work, copy and paste this link:" +
                    "</p>" +
                    "<p style='margin: 10px 0 0 0; color: #667eea; font-size: 14px; word-break: break-all;'>" +
                    details.getLink() +
                    "</p>" +
                    "</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 30px; background-color: #f8f9fa; text-align: center; border-top: 1px solid #e0e0e0;'>" +
                    "<p style='margin: 0; color: #999999; font-size: 14px;'>© 2025 Nuvola. All rights reserved.</p>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // true = HTML format

            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully";
        } catch (MessagingException e) {
            return "Error while sending mail!!!";
        }
    }


    public String sendRideFinished(EmailDetails details) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setSubject(details.getSubject());

            // msgBody mozes da popunis npr: "Ride ID: 123\nFrom: ...\nTo: ...\nTotal price: ..."
            // i ovde ga pretvorimo u HTML (da zadrzi prelom reda)
            String body = details.getMsgBody() == null ? "" : details.getMsgBody().replace("\n", "<br/>");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "</head>" +
                    "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>" +
                    "<table role='presentation' style='width: 100%; border-collapse: collapse;'>" +
                    "<tr>" +
                    "<td style='padding: 40px 0; text-align: center;'>" +
                    "<table role='presentation' style='width: 600px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +

                    // Header
                    "<tr>" +
                    "<td style='padding: 40px 30px; background: #0A1128; text-align: center;'>" +
                    "<h1 style='margin: 0; color: #ffffff; font-size: 28px;'>Nuvola Ride</h1>" +
                    "</td>" +
                    "</tr>" +

                    // Body
                    "<tr>" +
                    "<td style='padding: 40px 30px;'>" +
                    "<h2 style='margin: 0 0 20px 0; color: #333333; font-size: 24px;'>Your ride is complete</h2>" +
                    "<p style='margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;'>" +
                    "Thank you for riding with Nuvola. We hope you had a great experience." +
                    "</p>" +

                    // Optional details (koristi msgBody ako hoces da prikazes detalje)
                    (body.isBlank()
                            ? ""
                            : "<div style='margin-top: 20px; padding: 16px; background-color: #f8f9fa; border: 1px solid #e0e0e0; border-radius: 6px; color: #555555; font-size: 14px; line-height: 1.6;'>" +
                            body +
                            "</div>") +

                    "<p style='margin: 20px 0 0 0; color: #999999; font-size: 14px; line-height: 1.6;'>" +
                    "If you have any questions, reply to this email and we’ll help you out." +
                    "</p>" +
                    "</td>" +
                    "</tr>" +

                    // Footer
                    "<tr>" +
                    "<td style='padding: 30px; background-color: #f8f9fa; text-align: center; border-top: 1px solid #e0e0e0;'>" +
                    "<p style='margin: 0; color: #999999; font-size: 14px;'>© 2025 Nuvola. All rights reserved.</p>" +
                    "</td>" +
                    "</tr>" +

                    "</table>" +
                    "</td>" +
                    "</tr>" +
                    "</table>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);

            return "Mail sent Successfully";
        } catch (MessagingException e) {
            return "Error while sending mail!!!";
        }
    }


}

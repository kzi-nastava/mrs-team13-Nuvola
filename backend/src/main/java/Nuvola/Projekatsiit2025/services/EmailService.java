package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.util.EmailDetails;

public interface EmailService {
    // Method
    // To send a simple email
    String sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);
    String sendTrackingPage(EmailDetails details);
    String sendRideFinished(EmailDetails details);

}


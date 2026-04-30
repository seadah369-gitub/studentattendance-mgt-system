package com.attendance.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends emails via SMTP using settings stored in system_config.
 *
 * Config keys:
 *   email_enabled   — "true" / "false"  (default: false)
 *   email_host      — SMTP host          (e.g. smtp.gmail.com)
 *   email_port      — SMTP port          (e.g. 587)
 *   email_username  — sender address     (e.g. school@gmail.com)
 *   email_password  — SMTP password / app password
 *   email_from_name — display name       (e.g. Attendance System)
 *   email_tls       — "true" / "false"   (STARTTLS, default: true)
 *
 * For Gmail: enable 2FA and create an App Password at
 *   https://myaccount.google.com/apppasswords
 */
public class EmailService {

    private final SystemConfigService config;

    public EmailService(SystemConfigService config) {
        this.config = config;
    }

    /** Returns true if email sending is configured and enabled. */
    public boolean isEnabled() {
        return "true".equalsIgnoreCase(config.get("email_enabled", "false"))
            && !config.get("email_host", "").isBlank()
            && !config.get("email_username", "").isBlank()
            && !config.get("email_password", "").isBlank();
    }

    /**
     * Sends a plain-text email.
     *
     * @param toAddress  recipient email address
     * @param subject    email subject
     * @param body       plain-text body
     * @throws MessagingException if sending fails
     */
    public void send(String toAddress, String subject, String body) throws MessagingException {
        if (!isEnabled()) return;
        if (toAddress == null || toAddress.isBlank()) return;

        String host     = config.get("email_host",      "smtp.gmail.com");
        String port     = config.get("email_port",      "587");
        String username = config.get("email_username",  "");
        String password = config.get("email_password",  "");
        String fromName = config.get("email_from_name", "Attendance System");
        boolean tls     = !"false".equalsIgnoreCase(config.get("email_tls", "true"));

        Properties props = new Properties();
        props.put("mail.smtp.host",                 host);
        props.put("mail.smtp.port",                 port);
        props.put("mail.smtp.auth",                 "true");
        props.put("mail.smtp.starttls.enable",      String.valueOf(tls));
        props.put("mail.smtp.starttls.required",    String.valueOf(tls));
        props.put("mail.smtp.ssl.trust",            host);
        props.put("mail.smtp.connectiontimeout",    "10000");
        props.put("mail.smtp.timeout",              "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(username, fromName, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            msg.setFrom(new InternetAddress(username));
        }
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");

        Transport.send(msg);
    }

    /**
     * Tests the SMTP connection by sending a test email to the configured address.
     * Returns null on success, or an error message string on failure.
     */
    public String testConnection() {
        try {
            send(config.get("email_username", ""),
                 "Attendance System — Test Email",
                 "This is a test email from the Student Attendance System.\n\n" +
                 "If you received this, your email configuration is working correctly.");
            return null; // success
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}

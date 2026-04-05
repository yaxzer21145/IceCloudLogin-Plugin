package com.icecloud.login;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailManager {
    private final IceCloudLogin plugin;

    public EmailManager(IceCloudLogin plugin) {
        this.plugin = plugin;
    }

    public boolean sendEmail(String to, String subject, String content) {
        if (!plugin.getConfigManager().isEmailEnabled()) {
            plugin.getLogger().warning("邮件功能未启用！");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", plugin.getConfigManager().getEmailHost());
        props.put("mail.smtp.port", String.valueOf(plugin.getConfigManager().getEmailPort()));
        props.put("mail.smtp.auth", "true");
        
        if (plugin.getConfigManager().isEmailSSL()) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        if (plugin.getConfigManager().isEmailTLS()) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        plugin.getConfigManager().getEmailUsername(),
                        plugin.getConfigManager().getEmailPassword()
                );
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(plugin.getConfigManager().getEmailFrom()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setText(content, "UTF-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            plugin.getLogger().severe("发送邮件失败: " + e.getMessage());
            return false;
        }
    }
}

package com.iot.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * 邮件服务
 */
@Slf4j
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送简单文本邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送是否成功
     */
    public boolean sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("thingslinks@yeah.net");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("邮件发送成功：收件人={}, 主题={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败：收件人={}, 主题={}, 错误信息={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML格式）
     * @return 发送是否成功
     */
    public boolean sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("thingslinks@yeah.net");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // true 表示内容是HTML格式
            
            mailSender.send(message);
            log.info("HTML邮件发送成功：收件人={}, 主题={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("HTML邮件发送失败：收件人={}, 主题={}, 错误信息={}", to, subject, e.getMessage(), e);
            return false;
        }
    }
}

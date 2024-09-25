package cn.colin.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailUtil {
    // QQ邮箱的 SMTP 服务器地址
    private static final String EMAIL_SMTP_HOST = "smtp.qq.com";
    // QQ邮箱的 SMTP 服务器端口
    private static final String EMAIL_PORT = "587";
    // 发件人邮箱地址
    private static final String EMAIL_ACCOUNT = "119642704@qq.com";
    // 发件人昵称
    private static final String EMAIL_NICK = "王钟毓";
    // 发件人邮箱授权码
    private static final String EMAIL_PASSWORD = "ssaufwvsgpflbide";

    @SneakyThrows
    public static void sendQQMail(String toEmail, String title, String content, String filePath) {
        // 配置邮件服务器的属性
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true"); // 开启认证
        properties.put("mail.smtp.starttls.enable", "true"); // 开启TLS加密
        properties.put("mail.smtp.host", EMAIL_SMTP_HOST);
        properties.put("mail.smtp.port", EMAIL_PORT);
        // 创建邮件会话
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_ACCOUNT, EMAIL_PASSWORD);
            }
        });
        // 创建邮件内容
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_ACCOUNT, EMAIL_NICK));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(title);
        // 创建用于容纳多个部分（文本和附件）的 Multipart 对象
        Multipart multipart = new MimeMultipart();
        // 添加文本部分
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(content);
        multipart.addBodyPart(textPart);
        // 添加附件部分
        if (StringUtils.isNotEmpty(filePath)) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filePath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(source.getName());
            multipart.addBodyPart(attachmentPart);
        }
        message.setContent(multipart);

        // 发送邮件
        Transport.send(message);
    }
}

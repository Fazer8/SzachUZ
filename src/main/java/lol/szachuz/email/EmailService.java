package lol.szachuz.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "szachuz044@gmail.com";
    private final String password = "eckv mdmv ckwr wftp";
    private final String host = "smtp.gmail.com";
    private final int port = 587;

    public void sendGameStartEmail(String recipientEmail, String opponentName, String matchId) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "SzachUZ"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

            message.setSubject("♟️ Twój mecz w SzachUZ wystartował!");

            String htmlContent = buildHtmlContent(opponentName, matchId);

            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);

        } catch (Exception e) {
            System.err.println("Błąd wysyłania maila do " + recipientEmail + ": " + e.getMessage());
        }
    }

    private String buildHtmlContent(String opponentName, String matchId) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    margin: 0; padding: 0;
                    background-color: #f4f4f4;
                }
                .container {
                    max-width: 600px;
                    margin: 40px auto;
                    background-color: #ffffff;
                    border-radius: 12px;
                    overflow: hidden;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                }
                .header {
                    background-color: #013220;
                    color: #ffffff;
                    padding: 30px;
                    text-align: center;
                }
                .header h1 {
                    margin: 0;
                    font-size: 28px;
                    font-weight: 600;
                    letter-spacing: 1px;
                }
                .content {
                    padding: 40px 30px;
                    color: #333333;
                    text-align: center;
                }
                .welcome-text {
                    font-size: 18px;
                    margin-bottom: 25px;
                }
                .match-card {
                    background-color: #f8f9fa;
                    border: 1px solid #e9ecef;
                    border-radius: 8px;
                    padding: 20px;
                    margin: 20px 0;
                    text-align: left;
                }
                .match-card p {
                    margin: 8px 0;
                    font-size: 16px;
                }
                .label {
                    color: #666;
                    font-weight: bold;
                    text-transform: uppercase;
                    font-size: 12px;
                }
                .value {
                    color: #000;
                    font-weight: 600;
                }
                .button {
                    display: inline-block;
                    background-color: #013220;
                    color: #ffffff !important;
                    padding: 15px 35px;
                    text-decoration: none;
                    border-radius: 50px;
                    font-weight: bold;
                    font-size: 16px;
                    margin-top: 20px;
                    transition: background-color 0.3s;
                }
                .footer {
                    background-color: #eeeeee;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #888888;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>♟️ SzachUZ</h1>
                </div>
                <div class="content">
                    <p class="welcome-text">Twój przeciwnik został znaleziony!</p>
                    
                    <div class="match-card">
                        <p><span class="label">PRZECIWNIK</span><br><span class="value">%s</span></p>
                        <hr style="border: 0; border-top: 1px solid #e9ecef; margin: 10px 0;">
                        <p><span class="label">ID MECZU</span><br><span class="value">%s</span></p>
                    </div>

                    </div>
                <div class="footer">
                    &copy; 2026 SzachUZ &bull;
                </div>
            </div>
        </body>
        </html>
        """.formatted(opponentName, matchId, matchId);
    }
}
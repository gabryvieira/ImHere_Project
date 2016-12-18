/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

/**
 *
 * @author ubuntu
 */
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class SendMail {
    
    Properties properties;
    public SendMail(){
        properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
    }

   public void send(String from, String to, String subject, String text) {         

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("esimhere@gmail.com", "imherees");
            }

        });

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject(subject);

         // Now set the actual message
         message.setText(text);
         
         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      }catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}

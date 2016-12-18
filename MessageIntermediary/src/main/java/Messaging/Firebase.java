/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Messaging;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import slack.SlackApi;

/**
 *
 * @author ubuntu
 */
public class Firebase {
    
     public String postRequest(String title, String text, String to){
        String key = "AIzaSyA1sCd9ryfTferhjADhsGLcPC_sl0M-Nrw";
         
        StringBuilder response = new StringBuilder();
        try {
            String url = "https://fcm.googleapis.com/fcm/send";
            URL obj = new URL(url);
            
            
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + key);
            
            conn.setRequestMethod("POST");
           
            
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(buildNotification(title, text, to));
            wr.flush();
            wr.close();
            
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " );
            System.out.println("Response Code : " + responseCode);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;            

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();

            
        } catch (MalformedURLException ex) {
            Logger.getLogger(SlackApi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SlackApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response.toString();
    }
     
    public String buildNotification(String title, String text, String to){
        JsonObject notification = Json.createObjectBuilder()
                .add("body", text)
                .add("title", title)
                .add("sound", "default")
                .add("priority", "high")
                .build();
        JsonObject data = Json.createObjectBuilder()
                .add("op_id", 2)
                .build();
        JsonObject obj = Json.createObjectBuilder()
                .add("to", to)
                .add("data", data)
                .add("notification",notification)
                .build();
        
        System.out.println(obj.toString());
        
        String example = "{\"to\":\"fnqGYDrclkY:APA91bF2XKdeJGfNWUAOip716nvbhXBhP5EkpswIp2uIjTvKsy_dVPPwMW0L2xUfwNGOk9fl0xCLOqv-kxKCyEGbT_feKIoFxjJT2MTj-ug0mMWT4ituLkcoVRTDSzibczlmiNVzq5kZ\","
                + "\"data\":{\"op_id\":0},"
                + "\"notification\":{"
                + "\"body\": \"Notification message\","
                + "\"title\": \"title\","
                + "\"sound\": \"default\","
                + "\"priority\": \"high\"}}";
        
        
        return obj.toString();
    }
    
    
    
}

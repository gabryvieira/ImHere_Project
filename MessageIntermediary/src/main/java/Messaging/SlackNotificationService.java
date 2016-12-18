/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Messaging;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ubuntu
 */
@Path("/Notification")
public class SlackNotificationService {
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String receiveNotification(
            @FormParam("token") String token,
            @FormParam("team_id") String team_id,
            @FormParam("team_domain") String team_domain,
            @FormParam("channel_id") String channel_id,
            @FormParam("channel_name") String channel_name,
            @FormParam("timestamp") String timestamp,
            @FormParam("user_id") String user_id,
            @FormParam("user_name") String user_name,
            @FormParam("text") String text,
            @FormParam("trigger_word") String trigger_word){
        
        System.out.println("Notification: " + text);
        
        return "";
    }
    
}

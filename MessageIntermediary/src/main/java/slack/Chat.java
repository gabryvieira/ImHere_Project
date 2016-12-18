/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ubuntu
 */
public class Chat extends SlackApi{
    
    private String accesstoken = "xoxp-99703402119-99692193510-99704053622-c30444d72bd44f48bc6b8d40b6cd9f08";
    
    public Chat(String token){
        this.accesstoken = token;
    }
    
    public String postMessage(String channel, String text){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channel);
        args.put("text", text);
        args.put("as_user","true");
        
        String method ="chat.postMessage";
        
        String response = postRequest(method, args);               
        return response;
    }
    
}

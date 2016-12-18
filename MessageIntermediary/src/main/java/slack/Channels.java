/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 *
 * @author ubuntu
 */
public class Channels extends SlackApi{
    
    private String method;
    private String response;
    private String accesstoken = "xoxp-99703402119-99692193510-99704053622-c30444d72bd44f48bc6b8d40b6cd9f08";
    
    
    public Channels(String token){
        this.accesstoken = token;
    }
    
     public String createChannel(String channelName){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("name", channelName);
        
        method = "channels.create";
        response = postRequest(method, args);
        return response;       
    }
    
    public String renameChannel(String channelCode, String channelName){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channelCode);
        args.put("name", channelName);
        
        method ="channels.rename";
        
        response = postRequest(method, args);               
        return response;
    }
    
    public String listAllChannels() {
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method ="channels.list";
        
        response = getRequest(method, args);               
        return response;
    }
    
    public String ChannelInfo(String channelName){ // obter info de um chat (GET)
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channelName);
        
        method = "channels.info";
        
        response = getRequest(method, args);               
        return response;
    }
    
    public String JoinsToChannel(String channelName){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("name", channelName);
        
        method ="channels.join";
        
        response = postRequest(method, args);               
        return response;
    }
    
    public String listPrivateChannels(){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method = "groups.list";
        response = getRequest(method, args);               
        return response;
    }
    
    public String messagesOfChannel(String channelCode, int messagesNr){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channelCode);
        args.put("count", messagesNr + "");
        args.put("unreads", "1");
        
        method = "channels.history";
        response = getRequest(method, args);               
        return response;
    }
    
    public String messagesOfGroup(String channelCode, int messagesNr){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channelCode);
        args.put("count", messagesNr + "");
        args.put("unreads", "1");
        
        method = "groups.history";
        response = getRequest(method, args);               
        return response;
    }
    
    public String leavesChannel(String channelID){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", channelID);
        
        method = "channels.leave";
        response = postRequest(method, args);               
        return response; 
    }
    
     
    public String inviteUserToChat(String username, String chatName){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("channel", chatName);
        args.put("user", username);
        
        method = "channels.invite";
        response = postRequest(method, args);
        return response;
    }
    
    public String directMessageToUser(String user){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("user", user);
        
        method = "im.open";
        response = postRequest(method, args);
        return response;
        
    }
    
    public void userIdentity(String email){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method = "users.list";
        response = getRequest(method, args);
        String userId = isMemberInList(response, email);
        if(userId != null){
            //associate email or other if to token 
        }
        
    }
    
    private String isMemberInList(String list, String email){
        JsonReader reader = Json.createReader(new StringReader(list));
        JsonObject obj = reader.readObject();        
        JsonArray members = obj.getJsonArray("members");
                
        for (JsonValue member : members) {
            String id = ((JsonObject) member).getString("id");
            
            Map<String, String> temp = new HashMap<>();
            temp.put("token", accesstoken);
            temp.put("user", id);
            
            response = getRequest("users.info", temp);
            reader = Json.createReader(new StringReader(response));
            obj = reader.readObject();        
            JsonObject user = obj.getJsonObject("user").getJsonObject("profile");
            String email2 = user.getString("email");
            
            if(email.equalsIgnoreCase(email2)){
                return id;
            }            
        }
        return null;
    }
    
}

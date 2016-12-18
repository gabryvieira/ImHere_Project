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
 * @author gabriel
 */
public class Users extends SlackApi{
    private String method;
    private String response;
    private String accesstoken = "xoxp-99703402119-99692193510-99704053622-c30444d72bd44f48bc6b8d40b6cd9f08";
    
    public Users(String accesstoken){
        this.accesstoken = accesstoken;        
    }
    
    public String listUsers(){
              
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method = "users.list";
        response = getRequest(method, args);
        return response;
    }
    
    public String getUserInfo(String user){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("user", user);
        
        method = "users.info";
        response = getRequest(method, args);
        return response;
    }
   
    
    public String getUserIdentity(){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method = "users.identity";
        response = getRequest(method, args);
        return response;
    }
    
    public String getUserPresence(String user){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("user", user);
        
        method = "users.getPresence";
        response = getRequest(method, args);
        return response;
    }
    
    public String setUserPresence(String presence){
        Map<String,String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("presence", presence);
        
        method = "users.setPresence";
        response = postRequest(method, args);
        return response;
    }
}

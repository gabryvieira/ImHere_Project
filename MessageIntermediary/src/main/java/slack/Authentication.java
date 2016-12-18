/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;


import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import rabbitmq.MessageBroker;

/**
 *
 * @author ubuntu
 */
public class Authentication extends SlackApi{
    
    //App Credentials
    private String clientId = "89179064241.89178516167";
    private String passcode = "a49570a47c49e74b7e591aadf5899eb9";
    
    //Will be received in the redirect uri get parameter 'code'
    private String clientCode;
    
    public Authentication(String code){
        this.clientCode = code;
    }

    public String getAccessToken(){
        Map<String,String> args = new HashMap<>();
        args.put("client_id", clientId);
        args.put("client_secret", passcode);
        args.put("code", clientCode);
        
        String method = "oauth.access";
        
        String response = getRequest(method, args);
        System.out.println(response);
        
        JsonReader reader = Json.createReader(new StringReader(response));
        JsonObject obj = reader.readObject();
        
        
        return obj.getString("access_token");        
    }

    public boolean saveToken(String token) {
        try {
            MessageBroker broker = new MessageBroker();
            broker.connect();
            JsonObject jsonObj = Json.createObjectBuilder()
                    .add("op_id", 10)
                    .add("token", token)
                    .build();
            broker.publish("hello", jsonObj.toString());
            broker.closeConnection();
            
            
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
}

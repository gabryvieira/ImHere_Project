/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 *
 * @author ubuntu
 */
public class test {

    //Deti token
    private static String accesstoken = "xoxp-99703402119-99692193510-99704053622-c30444d72bd44f48bc6b8d40b6cd9f08";
    private static String baseURL = "https://slack.com/api/";
    
    public static void main(String[] args){
        Channels channel = new Channels(accesstoken);    
        String list = channel.listPrivateChannels();
        System.out.println(list);
        System.out.println(channel.listAllChannels());
        System.out.println("--------------------------");
        
        
        String message = channel.messagesOfChannel("C2XLW1E22", 4);
        System.out.println(message);
        
        
        System.out.println("-------------------------------------------");
        JsonReader read = Json.createReader(new StringReader(channel.listAllChannels()));
        JsonObject obj = read.readObject();
        JsonArray IDsChannels = obj.getJsonArray("channels");
        System.out.println(IDsChannels);
        System.out.println("-----------------------------");
        String channelID = "";
        String channelName = "";
        for(JsonValue jsonValue : IDsChannels){
            channelID = ((JsonObject) jsonValue).getString("id");
            channelName = ((JsonObject) jsonValue).getString("name");
            System.out.println("channel ID: "+channelID+ "name: "+channelName);
        }
        
        
    }        
}

package Messaging;



import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.jivesoftware.smack.AbstractXMPPConnection;
import rabbitmq.MessageBroker;
import xmppCommunication.XmppOpenfire;
import slack.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ubuntu
 */
public class ProcessMessage{
    
    
    private XmppOpenfire openfire;
    
    private Connection connectionDB;
    private SQLliteDatabase sqllite;
    
    private Map<String, AbstractXMPPConnection> connections;
    
    public ProcessMessage(){
        openfire = new XmppOpenfire();        
        sqllite = new SQLliteDatabase();
        connectionDB = sqllite.ConnectDB();
        connections = new HashMap<>();
    }
    
    public void process(String message){
        try {
            JsonReader reader = Json.createReader(new StringReader(message));
            JsonObject obj = reader.readObject();
            int id = obj.getInt("op_id");
            
            switch(id){
                case 0:
                    login(obj);
                    break;
                case 1:
                    createChat(obj);
                    break;
                case 2:
                    sendMessage(obj);
                    break;
                case 3:
                    slackLogin(obj);
                    break;
                case 4:
                    joinChat(obj);
                    break;
                case 5:
                    leftChat(obj);
                    break;
                case 6:
                    deleteChat(obj);
                    break;
                case 7:
                    chatHistory(obj);
                    break;
                case 8:
                    getChatInfo(obj);
                    break;
                case 9:
                    getAvailableChats(obj);
                    break;
                case 10:
                    updateSlackToken(obj);
                    break;
                default:
                    System.err.println("OPTION not found");
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void login(JsonObject obj) throws Exception{
        String userId = obj.getString("user_id");
        String hash = obj.getString("hash");
        String username = obj.getString("user_name");
        String device_token = obj.getString("device_token");
        
        if (!sqllite.userExists(connectionDB, userId)){
            //create new user
            System.out.println("LOGIN: CREATED USER");
            MessageBroker br = new MessageBroker();
            br.connect();
            br.createQueue(hash);
            br.closeConnection();
            sqllite.createUser(connectionDB, userId, hash, device_token);
            //openfire
            AbstractXMPPConnection connection = openfire.createConnection("admin", "admin");            
            
            String openUser = userId.split("@")[0];
            openfire.createUser(connection, openUser , hash);
            connection.disconnect();
            
            //Send mail to join team
            //SendMail mail = new SendMail();
            //String mailText = "Signup in the folowing link to join imhere slack team https://eschanneltest.slack.com/signup";
            //mail.send("esimhere@gmail.com", userId, "Register Imhere Slack team", mailText);
        }
        
        if(!connections.containsKey(hash)){
           connections.put(hash, openfire.createConnection(userId.split("@")[0], hash));           
        }
        
        sendResponseMessage(0, true, hash, null);
    }
    
    
    public void createChat(JsonObject obj) throws Exception{
        String userHash = obj.getString("hash");
        String chatName = obj.getString("chat_name").replace(' ', '_').toLowerCase();
        int geoId = Integer.parseInt(obj.getString("chat_id"));
        String chatDescription = obj.getString("chat_description");
        int chatTime = (int) Double.parseDouble(obj.getString("chat_time"));
        String chatEvent = obj.getString("chat_event");
        
        boolean ok = true;
        
        ok = sqllite.createChat(connectionDB, userHash, chatName, geoId, chatDescription, chatTime, chatEvent);
                
        if(connections.containsKey(userHash)){
            openfire.createChat(connections.get(userHash), chatName);
        }
        else{
            System.out.println("[openfire] No connection found");
        }
                
        // slack
        String token = sqllite.getSlackToken(connectionDB, userHash);
        if(token != null){
            Channels cha = new Channels(token);
            String response = cha.createChannel(chatName);   
            System.out.println("[slack] " + response);
        }
        
        //Response 1 -> error, 0 -> ok
        sendResponseMessage(1, ok, userHash, null);        
              
    }
    
    public void getChatInfo(JsonObject obj) {
        try{
            String userHash = obj.getString("hash");
            JsonArray chats = obj.getJsonArray("chat_id");
            JsonArrayBuilder jsonList = Json.createArrayBuilder();
            for (JsonValue chat : chats) {
                int geoId = (int) Double.parseDouble(chat.toString());
                System.out.println("GET_GEOID: " + geoId);
                
                ResultSet rs = sqllite.getChatInfo(connectionDB, geoId);
                if (rs.next()){
                    JsonObjectBuilder objBuilder = Json.createObjectBuilder()
                            .add("chat_id", geoId)
                            .add("chat_name", rs.getString("name"))
                            .add("chat_description", rs.getString("description"))
                            .add("chat_time", rs.getInt("time"))
                            .add("chat_event", rs.getString("event"));

                    jsonList.add(objBuilder);
                }
            }
            
            sendResponseMessage(8, true, userHash, jsonList.build());

        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }
            
    public void joinChat(JsonObject obj) throws Exception{
        String userHash = obj.getString("hash");
        int chatId = (int) Double.parseDouble(obj.getString("chat_id"));
        System.out.println("ID" + chatId);
        
        String chatName = sqllite.getChatName(chatId);
        
        if(connections.containsKey(userHash)){
            openfire.joinToChatRoom(connections.get(userHash), chatName);
        }
        else{
            System.out.println("[openfire] No connection found");
        }


        //slack
        String token = sqllite.getSlackToken(connectionDB, userHash);
        if(token != null){
            Channels cha = new Channels(token);
            
            if(chatName != null){
                String response = cha.JoinsToChannel(chatName);
                System.out.println("[slack] " + response);
            }
            else{
                sendResponseMessage(4, false, userHash, null);
            }
        }
        
        boolean ok = sqllite.joinChat(connectionDB, userHash, chatId);
        //[{"message":"", "date":""},{...},...]
        List<String[]> history = sqllite.getChatHistory(connectionDB, chatId, 10);
        JsonArrayBuilder jsonList = Json.createArrayBuilder();
        for(int i = history.size()-1; i >= 0; i--){
            String[] message = history.get(i);
            JsonObjectBuilder objBuilder = Json.createObjectBuilder()
                    .add("message", message[0])
                    .add("author", message[1].split("@")[0]);
            jsonList.add(objBuilder);
        }
                 
        //Response 1 -> error, 0 -> ok
        sendResponseMessage(4, ok, userHash, jsonList.build());
        
    }
    
    public void leftChat(JsonObject obj){
        String userHash = obj.getString("hash");
        String chatName = obj.getString("chat_id");
        int chatId = (int) Double.parseDouble(chatName);
        
        boolean ok = sqllite.leftChat(connectionDB, userHash, chatId);
        
        //Response 1 -> error, 0 -> ok
        sendResponseMessage(5, ok, userHash, null);//missing messages
    }
   
    public void sendMessage(JsonObject obj) throws Exception{
        String userHash = obj.getString("hash");
        int chatId = (int) Double.parseDouble(obj.getString("chat_id"));
        String msg = obj.getString("msg");
        if(obj.containsKey("chat_name")){
            chatId = sqllite.getChatId(obj.getString("chat_name"));
        }
        
        //Persist message
        sqllite.messageToChat(connectionDB, msg, chatId, userHash);
        // slack
        String token = sqllite.getSlackToken(connectionDB, userHash);
        if(token != null){
            Chat chatslack = new Chat(token);
            String response = chatslack.postMessage(sqllite.getChatName(chatId), msg);
            System.out.println("[slack] " + response);
        }
        //OpenFire
        String chatName = sqllite.getChatName(chatId);    
        if(connections.containsKey(userHash)){
            openfire.sendPublicMessage(connections.get(userHash), chatName, msg);
        }
        else{
            System.out.println("[OpenFire] Chat or user null");
        }
        
        //notify the other users
        List<String[]> users = sqllite.usersOfChat(connectionDB, chatId);
        String senderEmail = sqllite.getUserId(connectionDB, userHash).split("@")[0];
        Firebase notification = new Firebase();
        for (String[] user : users) {
            if(userHash.equals(user[1])){
                continue;
            }
            notification.postRequest("New Message ImHere",msg,user[2]);
            sendNotificationMessage(senderEmail, user[1], msg, chatId);
        } 
         
        //Response 1 -> error, 0 -> ok
        sendResponseMessage(2, true, userHash, null);
    }
        
    
    public void chatHistory(JsonObject obj){
        String userId = obj.getString("user_id");
        String chatId = obj.getString("chat_id");  
        
        //slack
        String accessToken = obj.getString("access_token");
        Channels cha = new Channels(accessToken);
        cha.messagesOfChannel(chatId, 10);
    }
    
    public void deleteChat(JsonObject obj){
        try {
            String hash = obj.getString("hash");
            int chatId = (int) Double.parseDouble(obj.getString("chat_id"));
            sqllite.deleteChat(connectionDB, chatId);
        } catch (Exception ex) {
            Logger.getLogger(ProcessMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    private void getAvailableChats(JsonObject obj) {
        String userHash = obj.getString("hash");
        List<String> chats = sqllite.listAllChats(connectionDB);
        
        JsonArrayBuilder jsonList = Json.createArrayBuilder();
        for (String chat : chats) {
            jsonList.add(chat);
        }
                
        sendResponseMessage(9, true, userHash, jsonList.build());
    }
    
                
    public void slackLogin(JsonObject obj){
        String userHash = obj.getString("hash");
        sqllite.slackLoginCheck(connectionDB, userHash);
        sendResponseMessage(3, true, userHash, null);
    }
        
    public void sendResponseMessage(int op_id, boolean ok, String hash, JsonArray data){
        try {
            if (data == null){
                data = Json.createArrayBuilder().build();
            }
            int response_id = (ok) ? 0 : 1;
            MessageBroker broker = new MessageBroker();
            broker.connect();
            JsonObject jsonObj = Json.createObjectBuilder()
                    .add("op_id", op_id)
                    .add("response_id", response_id)
                    .add("data", data)
                    .build();
            
            broker.publish(hash, jsonObj.toString());
            broker.closeConnection();
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean updateSlackToken(JsonObject obj) {
        String token = obj.getString("token");        
        return sqllite.updateSlackToken(connectionDB, token);
    }

    
    private void sendNotificationMessage(String username, String hash, String msg, int id){
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("author", username)
                .add("message", msg)
                .add("id", id)
                .build();
        JsonArray arr = Json.createArrayBuilder()
                .add(jsonObj)
                .build();
        sendResponseMessage(11, true, hash, arr);        
    }
    
    
}

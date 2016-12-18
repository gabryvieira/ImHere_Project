/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppCommunication;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 *
 * @author gabriel
 */
public class UserHistoryRoom {
    private String userName;
    private String roomName;
    private String message;
    
    private final HashMap<String, String> histMess;
    
    public UserHistoryRoom(String userName, String roomName, String message){
        this.userName = userName;
        this.roomName = roomName;
        this.message = message;
        
        histMess = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    /*public HashMap<String, String> getHistory(String chatName) throws Exception{
        Field field = getClass().getDeclaredField("roomName");
        Field[] fields = getClass().getDeclaredFields(); // get all the fields from your class.
        for (Field f : fields) {                         // iterate over each field...
            try {
               if(field.equals(f.get(chatName))){
                   histMess.put(userName, message);
               }
            } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }*/
    
   @Override
   public String toString(){
       return "User: "+userName+" Message: "+message;
   }
}

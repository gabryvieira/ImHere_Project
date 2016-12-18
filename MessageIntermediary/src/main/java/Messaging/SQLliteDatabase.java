/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Messaging;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gabriel
 * SQLiteDatabase to manage user, channels and messages
 */
public class SQLliteDatabase {
    private Connection conn = null;
    private Statement statement;
    private ResultSet resultSet;
    private final String StringDB = "jdbc:sqlite:src/main/java/databases/imhereDatabase.sqlite";


    
    public SQLliteDatabase(){        

    }
    
    public Connection ConnectDB() {
        try{
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(StringDB);        
            return conn;
            
        }catch(Exception e){
            System.out.println("Error establishing connection!");
        }
        
        return null;
    }
    
    
    /*------------------------------------------------------------------------------*/
    
    // Users 
    public List<String> listAllUsers(Connection conn){
        try{
            List<String> users = new ArrayList<>();
            String query = "Select name from user";

            resultSet = queryStatement(conn, query);

            // percorrer a coluna 'username'
            while(resultSet.next()){
                String username = resultSet.getString("name");
                users.add(username);
            }

            //System.out.println(users.toString());
            resultSet.close();
            return users;

        }catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }
    
    public boolean deleteUser(Connection conn, String user_id) throws Exception{    
        String query = "delete from user where id = '"+user_id+"'";
        return updateStatement(conn, query);     
    }
    
    public ResultSet getUserInfo(Connection conn, String user_id) throws Exception{
        String query = "SELECT name, hash "
                     + "FROM user "
                     + "WHERE id = " + user_id;
        
        return queryStatement(conn, query);
    }

    
       
    public boolean createUser(Connection conn, String username, String password, String device_token){
        String query = "Insert into user(name, hash, device_token) values ('"+username+"','"+password+"','" + device_token +"')";            
        return updateStatement(conn, query);
    }
    
    public List<String[]> usersOfChat(Connection conn, int geoId){
        try {
            List<String[]> tokens = new ArrayList<>();
            
            String query = "SELECT u.device_token, u.hash, u.name "
                  + " FROM user u "
                  + " INNER JOIN user_chat uc ON u.id = uc.user_id"
                  + " INNER JOIN chat c ON c.id = uc.chat_id"
                  + " WHERE c.geo_id = " + geoId + ";";
            resultSet = queryStatement(conn, query);
            while (resultSet.next()){
                String[] users = new String[3];
                users[0] = resultSet.getString("name");
                users[1] = resultSet.getString("hash");
                users[2] = resultSet.getString("device_token");
                tokens.add(users);
            }
            resultSet.close();
            return tokens;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }        
    }
    
    public boolean userExists(Connection conn, String username){
        try {
            String queryStr = "SELECT * FROM user WHERE name = '" + username + "';";
            resultSet = queryStatement(conn, queryStr);
            return resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public String getUserId(Connection conn, String userHash) {
        try {
            String query = "SELECT name FROM user WHERE hash = '" + userHash + "';";
            ResultSet rs = queryStatement(conn, query);
            if(rs.next()){
                String name = rs.getString("name");
                return name;
            }
            else{
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public boolean slackLoginCheck(Connection conn, String hash){
        String query = "UPDATE user SET slack_login = 1 "
                     + "WHERE hash = '" + hash + "';";
        return updateStatement(conn, query);
    }
    
    public boolean updateSlackToken(Connection conn, String token){
        String query = "UPDATE user SET slack_token = '" + token + "' "
                     + "WHERE slack_login = 1;";
        boolean ok = updateStatement(conn, query);
        if(ok){
            query = "UPDATE user SET slack_login = 0 "
              + "WHERE slack_login = 1;";
            return updateStatement(conn, query);
        }
        return false;
    }
    
    
    public String getSlackToken(Connection conn, String hash){
        try {
            String query = "SELECT slack_token FROM user "
                    + "WHERE hash = '" + hash + "';";
            ResultSet rs = queryStatement(conn, query);
            if(rs.next()){
                String token = rs.getString("slack_token");
                rs.close();
                return token;
            }
            else{
                rs.close();
                return null;
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /*---------------------------------------------------------------------------------------------*/
    
    
    
        // DB Channels/ Chats
    public boolean createChat(Connection conn, String hash, String name, int geoId, String description, int time, String event){
        try {
            String getUserId = "SELECT id FROM user WHERE hash = '" + hash + "';";
            resultSet = queryStatement(conn, getUserId);
            int userId = -1;
            if(resultSet.next()){
                userId = resultSet.getInt("id");
                resultSet.close();
            }            
            else{
                System.out.println("ERROR: No results");
                resultSet.close();
                return false;
            }
                                    
            String queryStr =  "Insert into chat(name, description, time, event, user_id, geo_id)"
                    + " values('"+name+"','" + description + "',"
                    + " '" + time + "', '"+ event +"', " + userId + ", " + geoId+")";
            return updateStatement(conn, queryStr);
            
        } catch (SQLException e) {
            while (e != null) {
                String errorMessage = e.getMessage();
                System.err.println("sql error message:" + errorMessage);

                String sqlState = e.getSQLState();
                System.err.println("sql state:" + sqlState);

                int errorCode = e.getErrorCode();
                System.err.println("error code:" + errorCode);
                e = e.getNextException();
              }
        }
        return false;
    }
    
    public boolean deleteChat(Connection conn, int chaID) throws Exception{
        String query = "delete from chat where geo_id = " + chaID + ";";
        return updateStatement(conn, query);           
    }
    
    public ResultSet getChatInfo(Connection conn, int geoId){
        String query = "SELECT name, description, time, event "
                     + "FROM chat "
                     + "WHERE geo_id = " + geoId;
        return queryStatement(conn, query);
    }
    
    public boolean joinChat(Connection conn, String hash, int geo_id){
        try {
            leftChat(conn, hash, geo_id);
            String query = "SELECT id FROM user WHERE hash = '" + hash + "'";
            resultSet = queryStatement(conn, query);
            int userId = -1;
            if(resultSet.next()){
                userId = resultSet.getInt("id");
                resultSet.close();
            }            
            else{
                System.out.println("ERROR: No results");
                resultSet.close();
                return false;
            }
            
            query = "SELECT id FROM chat WHERE geo_id = " + geo_id + "";
            resultSet = queryStatement(conn, query);
            int chat_id = -1;
            if(resultSet.next()){
                chat_id = resultSet.getInt("id");
                resultSet.close();
            }
            else{
                System.out.println("ERROR: No results Geo_id");
                resultSet.close();
                return false;
            }
            
            query = "INSERT INTO user_chat (chat_id, user_id) "
                    + "VALUES(" + chat_id + "," + userId + ")";
            return updateStatement(conn, query);
            
        } catch (SQLException e) {
            while (e != null) {
                String errorMessage = e.getMessage();
                System.err.println("sql error message:" + errorMessage);

                String sqlState = e.getSQLState();
                System.err.println("sql state:" + sqlState);

                int errorCode = e.getErrorCode();
                System.err.println("error code:" + errorCode);
                e = e.getNextException();
              }
            
            return false;
        }
    }
    
    public boolean leftChat(Connection conn, String hash, int geo_id){
        try {
            String query = "SELECT uc.id from user_chat uc "
                    + "INNER JOIN user u ON uc.user_id = u.id "
                    + "INNER JOIN chat c ON uc.chat_id = c.id "
                    + "WHERE u.hash = '" + hash + "' "
                    + "AND c.geo_id = " + geo_id + ";";
            ResultSet rs = queryStatement(conn, query);
            if(rs.next()){
                query = "DELETE FROM user_chat "
                        + "WHERE id = " + rs.getInt("id") + ";";
                return updateStatement(conn, query);
            }
            else{
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
        
    public List<String> listAllChats(Connection conn){
        try{
            List<String> chats = new ArrayList<>();
            String query = "Select name from chat;";
            
            resultSet = queryStatement(conn, query);
            
            // percorrer a coluna 'username'
            while(resultSet.next()){
                String chatName = resultSet.getString("name");
                chats.add(chatName);
            }
  
            resultSet.close();
            return chats;
            //System.out.println(chats.toString());
            //return chats.toString().replace("[", "").replace("]", "");
        } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);     
        }
        return new ArrayList<>();     
    }
    
   
    public boolean chatExists(Connection conn, String chatname){
        try{
            String queryStr = "SELECT * FROM chat WHERE name = '"+chatname+"';";
            resultSet = queryStatement(conn, queryStr);
            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    
    /* chats em que um user esta inserido*/
    public ResultSet getJoinedChats(Connection conn, String user_id){
        String query = "SELECT chat_id "
                     + "FROM user_chat "
                     + "WHERE user_id = " + user_id;
        
        return queryStatement(conn, query);
    }
    
    
    public List<String[]> getChatHistory(Connection conn, int geo_id, int limit){
        try{
            List<String[]> chatHistory = new ArrayList<>();
            
            
            String query = "SELECT m.text_message, u.name "
                  + "FROM messages m "
                  + "INNER JOIN chat c ON c.id = m.chat_id "
                  + "INNER JOIN user u ON u.id = m.user_id "
                  + "WHERE c.geo_id = " + geo_id + " "
                  + "ORDER BY m.message_id desc "
                  + " limit " + limit + ";";
            resultSet = queryStatement(conn, query);
            while(resultSet.next()){
                String[] messages = new String[2];
                messages[0] = resultSet.getString("text_message");
                messages[1] = resultSet.getString("name");
                chatHistory.add(messages);
            }
            
            return chatHistory;          
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }        
    }
    
    public String getChatName(int geo_id){
        try {
            String query = "SELECT name FROM chat WHERE geo_id = " + geo_id;
            ResultSet result = queryStatement(conn, query);
            if(result.next()){
                return result.getString("name");
            }
            else{
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public int getChatId(String chat_name){
        try {
            String query = "SELECT geo_id FROM chat WHERE name = '" + chat_name + "';";
            ResultSet result = queryStatement(conn, query);
            if(result.next()){
                return result.getInt("geo_id");
            }
            else{
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    /*------------------------------------------------------------------------------------*/
    // Messages
    
    public boolean messageToChat(Connection conn, String message, int geoId, String hash){
        try{  
            String query = "SELECT id FROM user WHERE hash = '" + hash + "'";
            resultSet = queryStatement(conn, query);
            resultSet.next();            
            int user_id = resultSet.getInt("id");
            resultSet.close();
            
            query = "SELECT id FROM chat WHERE geo_id = " + geoId + ";";
            resultSet = queryStatement(conn, query);
            resultSet.next();            
            int chat_id = resultSet.getInt("id");
            resultSet.close();
            
            query = "INSERT INTO messages (text_message, user_id, chat_id) "
                    + "VALUES('" + message + "'," + user_id + "," + chat_id +");";
           
            return updateStatement(conn, query);
         } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    public boolean messageToPrivateChat(Connection conn, String message, int chatID, String hash, int dest_id){
        try{  
            String query = "SELECT id FROM user WHERE hash = '" + hash + "'";
            resultSet = queryStatement(conn, query);
            resultSet.next();            
            int user_id = resultSet.getInt("id");
            
            query = "INSERT INTO messages (text_message, user_id, chat_id, dest_id) "
                    + "VALUES('" + message + "'," + user_id + "," + chatID +"," + dest_id +")";
           
            return updateStatement(conn, query);
         } catch (SQLException ex) {
            Logger.getLogger(SQLliteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    public boolean deleteMessage(Connection conn, String hash) throws Exception{
        String query = "SELECT id FROM user WHERE hash = '" + hash + "'";
        resultSet = queryStatement(conn, query);
        resultSet.next();            
        int user_id = resultSet.getInt("id");
        
        query = "delete from messages where user_id = "+user_id; 
        return updateStatement(conn, query);           
    }
    
    public boolean deleteAll(Connection conn){
        String query = "DELETE FROM user_chat;"
                     + "DELETE FROM messages;"
                     + "DELETE FROM chat;"
                     + "DELETE FROM user;";
        return updateStatement(conn, query);       
        
    }

     /*------------------------------------------------------------------------*/
    // execute and update queries
    public ResultSet queryStatement(Connection conn, String queryStr){
    
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(queryStr);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return resultSet;         
    }
    
    public boolean updateStatement(Connection conn, String queryStr){
        try{
            statement = conn.createStatement();
            statement.executeUpdate(queryStr);  
            statement.close();
            return true;
        } catch (SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    
    
    
   
}

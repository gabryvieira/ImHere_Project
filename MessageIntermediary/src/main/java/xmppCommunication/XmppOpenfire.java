/**
 * Created by gabriel on 03-10-2016.
 */
package xmppCommunication;

import java.io.IOException;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import rabbitmq.MessageBroker;


public class XmppOpenfire {

    private String loginUsername;
    private String loginPassword;

    private final String hostAddress = "192.168.215.165";
    private final int port = 5222; // client port number
    private AbstractXMPPConnection connection;

    private int chatID;
    private String serviceHostname = "@conference.ubuntu";
    
    private List<String> chatrooms = new ArrayList<>();

    private MultiUserChatManager manager;
    public static MessageListener messageListener;
    public ChatMessageListener chatMessage;
    
    private List<UserHistoryRoom> hist;
    private final ArrayList<HashMap<String, String>> userList;
    private final HashMap<String, String> map;
    
    public XmppOpenfire(){
        hist = new LinkedList<>();
        userList = new ArrayList<>();
        map = new HashMap<>();
    }


    // criacao da ligacao
    public AbstractXMPPConnection createConnection(String loginUsername, String loginPassword) throws Exception {
        XMPPTCPConnectionConfiguration connConfig = XMPPTCPConnectionConfiguration
                .builder()
                .setServiceName("ubuntu")
                .setHost(hostAddress)
                .setPort(port)
                .setCompressionEnabled(false)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                .setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                })
                .setDebuggerEnabled(false)
                .setSendPresence(true)
                .setUsernameAndPassword(loginUsername, loginPassword).build(); // user com que se esta a "mexer"

        connection = new XMPPTCPConnection(connConfig);
        if(connection.isConnected()){
            connection.disconnect();
        }
        connection.connect();
        connection.login();// credentials by default to access openfire (ADMIN ONLY)
        System.out.print("Connected to openfire server!");


        return connection;
    }


            // criacao de um novo user
    public void createUser(AbstractXMPPConnection connection, String newUsername, String newUserPass){
        try {
            AccountManager ac = AccountManager.getInstance(connection);
            ac.createAccount(newUsername, newUserPass);
            System.out.println("[openfire] User created :)");        
            
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException ex) {
           System.out.println("[openfire] User already exists");
        }        
    }

    public void createChat(AbstractXMPPConnection connection, String chatName) throws Exception {

        // Get the MultiUserChatManager
        manager = getInstanceForConnection(connection);
        //MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);

        chatrooms.add(chatID,chatName);
        // Get a MultiUserChat using MultiUserChatManager
        
        MultiUserChat muc = manager.getMultiUserChat(chatName+this.serviceHostname);
                        
        // persistent chat
        muc.create("chats"); // nickname
        
        muc.addMessageListener(new MessageListener() {
                @Override
                public void processMessage(Message message) {
                    System.out.println("[Openfire] Received message: "
                            + (message != null ? message.getBody() : "NULL"));
                    System.out.println("[Openfire] Message sender :" + message.getFrom());
                    String msg = message.getBody().replace('\n', ' ').replace('\'', ' ');
                    
                    
                    String sender = message.getFrom().split("@")[1];
                    if(sender.equals("conference.ubuntu/bot")){
                        try {
                            MessageBroker broker = new MessageBroker();
                            broker.connect();
                            JsonObject jsonObj = Json.createObjectBuilder()
                                    .add("op_id", 2)
                                    .add("hash", "bot19")
                                    .add("chat_id", "1.0")
                                    .add("chat_name", message.getFrom().split("@")[0])
                                    .add("msg", msg)
                                    .build();
                            
                            broker.publish("hello", jsonObj.toString());
                        } catch (IOException ex) {
                            Logger.getLogger(XmppOpenfire.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                     
                }
            });
        
        Form form = muc.getConfigurationForm();
        Form answerForm = form.createAnswerForm();
        answerForm.setAnswer("muc#roomconfig_publicroom", true);
        answerForm.setAnswer("muc#roomconfig_persistentroom", true);
        answerForm.setAnswer("muc#roomconfig_roomname", chatName);

        muc.sendConfigurationForm(answerForm);
        System.out.println("Persistent chat was created :)!");
        chatID++;

    }

    public void joinToChatRoom(AbstractXMPPConnection connection, String chatToJoin) throws Exception{
        // Create a MultiUserChat using an XMPPConnection for a room
        manager = getInstanceForConnection(connection);

        MultiUserChat muc2 = manager.getMultiUserChat(chatToJoin+this.serviceHostname);
        
        // User2 joins the new room using a password and the nickname is your username
        // the amount of history to receive. In this example we are requesting the last 10 messages.
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(10); // request the last 10 messages
        
        if(!muc2.isJoined()){
            muc2.join(connection.getUser(), "password", history, connection.getPacketReplyTimeout());
            System.out.println("Joined to chat :)");
        }
        else{
            System.out.println("Already Joined to chat");
        }

    }
    public void inviteUserToChat(AbstractXMPPConnection connection, String username, String chatName) throws Exception{
        manager = getInstanceForConnection(connection);
        MultiUserChat mc = manager.getMultiUserChat(chatName+this.serviceHostname);
        mc.addInvitationRejectionListener(new InvitationRejectionListener() {
            @Override
            public void invitationDeclined(String invitee, String reason) {
                System.out.println("Sorry, but not this moment");
            }
        });
        // invite user
        mc.invite(username+"@admin/Smack", "Hi "+username+", join to "+chatName+" room");     
    }    
        // send a public message to chatroom
    public void sendPublicMessage(AbstractXMPPConnection connection, String chatName, String msg) throws Exception{
        manager = getInstanceForConnection(connection);
        MultiUserChat muc2 = manager.getMultiUserChat(chatName+this.serviceHostname);
        Message message = new Message(chatName + this.serviceHostname, Message.Type.groupchat);

        message.setBody(msg);
        message.setType(Message.Type.groupchat);
        message.setTo(chatName);
        muc2.sendMessage(message);
       
    }

    // send private message to one user inside of the same room
    public void sendPrivateMessage(AbstractXMPPConnection connection, String userName, String chatName, String msg) throws Exception{
        manager = getInstanceForConnection(connection);
        MultiUserChat muser = manager.getMultiUserChat(chatName+this.serviceHostname);
        Chat chat = muser.createPrivateChat(chatName+ this.serviceHostname +"/"+userName+"@admin/Smack", chatMessage);
        System.out.println("Send a message: (input @back to turn back to menu)");
        
        chat.sendMessage(msg);
    }
    
    public String getRoomInfo(AbstractXMPPConnection connection, String chatroom) throws Exception{
        manager = getInstanceForConnection(connection);
        StringBuilder sb = new StringBuilder();
        // room information
        RoomInfo info = manager.getRoomInfo(chatroom+this.serviceHostname);
        System.out.println("Number of occupants: " + info.getOccupantsCount());
        System.out.println("Room Name: "+info.getName());
        System.out.println("Description: "+info.getDescription());
        sb.append("Number of occupants: ");
        sb.append(info.getOccupantsCount());
        sb.append("\n");
        sb.append("Room Name: ");
        sb.append(info.getName());
        sb.append("\n");
        sb.append("Description: ");
        sb.append(info.getDescription());
        return sb.toString();
    }
    
    public List<String> getJoinedRooms(AbstractXMPPConnection connection, String loginUsername) throws Exception{
        // Get the MultiUserChatManager
        manager = getInstanceForConnection(connection);
        // Get the rooms where user3@host.org has joined
        List<String> joinedRooms = manager.getJoinedRooms(loginUsername+"@admin/Smack");
        return joinedRooms;
    }
    
       
    public String getUserInfo(AbstractXMPPConnection connection) throws Exception{
        AccountManager ac = AccountManager.getInstance(connection);
        //connection.getAccountManager().getAccountAttribute("name");
        StringBuilder sbUser = new StringBuilder();
        sbUser.append(ac.getAccountAttributes());
        return sbUser.toString();        
    }
    
    public String getlistUsers(AbstractXMPPConnection connection) throws Exception{
        
        Roster roster = Roster.getInstanceFor(connection);

        if (!roster.isLoaded()) 
            roster.reloadAndWait();

        Collection<RosterEntry> entries = roster.getEntries();

        for (RosterEntry entry : entries) {
                Presence entryPresence = roster.getPresence(entry.getUser());
                Presence.Type type = entryPresence.getType();
                map.put("STATUS", type.toString());              
                map.put("USER", entry.getName());
                userList.add(map);
        }
        
        return userList.toString().replace("[", "").replace("]", "");
    }
    
    // user presence
    public String getUserPresence(AbstractXMPPConnection connection, String user) throws Exception{
        Roster roster = Roster.getInstanceFor(connection);

        if (!roster.isLoaded()) 
            roster.reloadAndWait();
        
        Presence entryPresence = roster.getPresence(user);
        Presence.Type type = entryPresence.getType();
        if(type.equals(Presence.Type.available)){
            System.out.print("Available");
           
        }
        
        return type.toString();                   
    }
            
           
    
    public String getLoginUsername(){
        return loginUsername;
    }

    public String getLoginPassword(){
        return loginPassword;
    }

    public List<String> getChatRooms(){
        return chatrooms;
    }

    public List<UserHistoryRoom> getList() {
        return hist;
    }
    // get instance connection
    public  MultiUserChatManager getInstanceForConnection(AbstractXMPPConnection connection){
        manager = MultiUserChatManager.getInstanceFor(connection);
        return manager;
    }


}

package pt.ua.tomasr.imhere.rabitt;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.ua.tomasr.imhere.modules.InfoChat;

public class MessageBroker {
    private final static String QUEUE_NAME = "hello";
    private Channel channel;
    private Connection connection;

    public ArrayList<InfoChat> chatinfos = new ArrayList<InfoChat>();
    public ArrayList<String> chatmessages = new ArrayList<String>();
    public String one_message = new String();

    public void connect(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            //factory.setHost("x.x.x.x.servidordo.jorge");
            factory.setHost("192.168.8.217");
            factory.setPort(5012);
            factory.setUsername("es");
            factory.setPassword("imhere");
            connection = factory.newConnection();
            channel = connection.createChannel();  
            
        } catch (Exception ex) {
            Logger.getLogger(MessageBroker.class.getName()).log(Level.SEVERE, null, ex);
            Log.e("Erro","erro no connect - rabbit");
        }
    }
    
    public void createQueue(String queue_name) throws IOException{
        channel.queueDeclare(queue_name, false, false, false, null);        
    }
    
    public void publish(String queue_name, String message) throws IOException{
        channel.basicPublish("", queue_name, null, message.getBytes());
        Log.i("Rabbit publish",""+message);
    }
    
    public void consume(String queue_name) throws IOException{
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                try {

                    String message = new String(body, "UTF-8");
                    JSONObject obj = new JSONObject(message);
                    if(obj.getInt("op_id")==0)/*LOGIN*/ imprime_error(obj);
                    else if(obj.getInt("op_id")==1)/*CREATE CHAT*/{imprime_error(obj);}
                    else if(obj.getInt("op_id")==2)/*SEND MESSAGE*/ imprime_error(obj);
                    else if(obj.getInt("op_id")==3)/*LOGIN W/ SLACK*/ imprime_error(obj);
                    else if(obj.getInt("op_id")==4)/*JOIN CHAT*/ {
                        chatmessages = parseMessages(obj);
                        imprime_error(obj);
                    }
                    else if(obj.getInt("op_id")==6)/*DELETE CHAT*/ imprime_error(obj);
                    else if(obj.getInt("op_id")==8)/*GET CHAT INFO*/ {
                        chatinfos = parseAvailableChats(obj);
                        imprime_data(obj);
                    }
                    else if(obj.getInt("op_id")==11)/*UPDATE MESSAGE*/{
                        one_message = parse_oneMessage(obj);
                        imprime_error(obj);
                    }


                }catch(JSONException e){
                    e.printStackTrace();
                    Log.e("erro","Rabbit - JSON Exception");

                }
            }
        };
        channel.basicConsume(queue_name, true, consumer);
    }
    
    public void closeChannel() throws IOException, TimeoutException{
        channel.close();
    }
    
    public void closeConnection() throws IOException{
        connection.close();
    }

    public void imprime_data(JSONObject obj) throws JSONException{
        Log.i("Rabbbit data",""+obj.getString("data"));
    }

    public void imprime_error(JSONObject obj) throws JSONException{
        Log.i("Rabbbit data",""+obj.getString("response_id"));
    }

    public ArrayList<InfoChat> getChatInfos() throws JSONException{
        return chatinfos;
    }

    public ArrayList<String> getChatMessages() throws JSONException{
        return chatmessages;
    }

    public ArrayList<InfoChat> parseAvailableChats(JSONObject obj) throws JSONException{

        JSONArray jArray = obj.getJSONArray("data");

        for (int i=0; i < jArray.length(); i++) {

            JSONObject oneObject = jArray.getJSONObject(i);

            // Pulling items from the Objects
            Integer id = oneObject.getInt("chat_id");
            String name = oneObject.getString("chat_name");
            String description = oneObject.getString("chat_description");
            String time = oneObject.getString("chat_time");
            String event = oneObject.getString("chat_event");

            //Add to the list
            InfoChat infoChat = new InfoChat(id,name,description,time,event);
            chatinfos.add(infoChat);
        }

        return chatinfos;

    }

    public ArrayList<String> parseMessages(JSONObject obj) throws JSONException{

        JSONArray jArray = obj.getJSONArray("data");

        for (int i=0; i < jArray.length(); i++) {

            JSONObject oneObject = jArray.getJSONObject(i);

            // Pulling items from the Objects
            String msg = oneObject.getString("message");
            String author = oneObject.getString("author");
            String msg_final = author+": "+msg;

            //Add to the list
            chatmessages.add(msg_final);
        }
        Log.i("msg no rabbit",""+chatmessages);

        return chatmessages;

    }

    public String parse_oneMessage(JSONObject obj) throws JSONException{

        JSONArray jArray = obj.getJSONArray("data");

        for (int i=0; i < jArray.length(); i++) {

            JSONObject oneObject = jArray.getJSONObject(i);

            // Pulling items from the Objects
            String msg = oneObject.getString("message");
            String author = oneObject.getString("author");
            Integer id = oneObject.getInt("id");
            String msg_final = id+"#"+author+": "+msg;

            //Add to the list
            one_message = msg_final;
        }
        Log.i("one_message no rabbit",""+one_message);

        return one_message;

    }
    
}

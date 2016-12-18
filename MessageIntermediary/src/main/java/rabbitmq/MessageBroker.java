/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbitmq;

import Messaging.ProcessMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ubuntu
 */
public class MessageBroker {
    private Channel channel;
    private Connection connection;
    private ProcessMessage processor;
    
    public void connect(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.215.165");
            //factory.setHost("192.168.8.217");
            factory.setPort(5012);
            factory.setUsername("es");
            factory.setPassword("imhere");
            processor = new ProcessMessage();
            connection = factory.newConnection();
            channel = connection.createChannel();
            
        } catch (IOException ex) {
            Logger.getLogger(MessageBroker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(MessageBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void createQueue(String queue_name) throws IOException{
        channel.queueDeclare(queue_name, false, false, false, null);        
    }
    
    public void publish(String queue_name, String message) throws IOException{
        channel.basicPublish("", queue_name, null, message.getBytes());
        System.out.println(" [x] Sent [" + queue_name.substring(0, 5) + "] '" + message + "'");
    }
    
    public void consume(String queue_name) throws IOException{
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                throws IOException {
              String message = new String(body, "UTF-8");              
              processor.process(message);             
              
            
              System.out.println(" [x] Received '" + message + "'");
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
    
}



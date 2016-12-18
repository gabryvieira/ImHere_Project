/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbitmq;

import java.io.IOException;

/**
 *
 * @author ubuntu
 */
public class RabbitTest {
    
    public static void main(String [] args) throws IOException{
        MessageBroker test = new MessageBroker();
        test.connect();
        System.out.println("connected");
        //test.createQueue("hello");
        test.consume("bot19");        
        
        //test.publish("hello","{\"op_id\":9, \"hash\": bot19}");
        //test.publish("hello","{test 2}");
    }
    
    
    public void initialNegotiation() throws IOException{
        String channelName = "negotiation";
        MessageBroker broker = new MessageBroker();
        broker.consume(channelName);
        
        
    }
    
}

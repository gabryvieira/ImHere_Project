/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import rabbitmq.MessageBroker;

/**
 *
 * @author ubuntu
 */
@Path("/rabbit")
public class RunRabbit {
    private MessageBroker test = null;
    
    @GET
    @Path("/run")
    public String run(){        
        try {           
            MessageBroker test = new MessageBroker();
            test.connect();
            System.out.println("connected");
            test.consume("hello");
            
            return "Running";
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Exception - Not Running : " + ex.toString();
        }
    }
    
    @GET 
    @Path("/stop")
    public String stop(){
        try {
            if(test == null){     
                return "Rabbit is not started";                
            }
            else{
                test.closeConnection();
                return "Stopped with success";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Exception : " + ex.toString();
        }
    }
    
}

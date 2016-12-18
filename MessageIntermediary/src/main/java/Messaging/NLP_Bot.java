/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Messaging;
import rabbitmq.MessageBroker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import slack.test;


/**
 *
 * @author ubuntu
 */
public class NLP_Bot {
    private static final String baseURL = "http://192.168.8.217:";
    private static final String port = "5010";
    public NLP_Bot(){
        
    }
    
    public static void main(String[] args){
        NLP_Bot bot = new NLP_Bot();
        bot.searchNotation("ola @sup dude");
    }
    
    public void searchNotation(String text){
        int index = text.indexOf("@");
        if (index >= 0){
            StringBuilder builder = new StringBuilder();
            for(int i = index+1; i < text.length(); i++){
                char c = text.charAt(i);
                builder.append(c);
            }
            System.out.println(builder.toString());
        }
    }
    
    public void redirectRequest(String queueName, String message) throws Exception{
        
        
        MessageBroker bk = new MessageBroker();
        bk.connect();
        bk.createQueue(queueName);
        bk.consume(queueName);
        
	

        String url = baseURL + port + "?" + "message= "+message;
        
        URL urlObj = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");

        System.out.println("Redirect to URL : " + url);	

	BufferedReader in = new BufferedReader(
                              new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer html = new StringBuffer();
        
	while ((inputLine = in.readLine()) != null) {
		html.append(inputLine);
	}
	in.close();
        
	System.out.println("URL Content... \n" + html.toString());
	System.out.println("Done");
        
        bk.publish(queueName, message);
    }
    
    
    public String getRequest(String message){
        String url = baseURL + port + "?" + "message= "+message;
//gms
        //String url = baseURL + method + "?" + buildArgs(args);
        StringBuilder response = new StringBuilder();
        
        try {
            URL urlObj = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
                        

            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (MalformedURLException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return response.toString();
    }
        
}

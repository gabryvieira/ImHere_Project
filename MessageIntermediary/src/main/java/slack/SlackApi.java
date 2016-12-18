/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author ubuntu
 */
public abstract class SlackApi {
    
    private static final String baseURL = "https://slack.com/api/";
    
    public String getRequest(String method, Map<String,String> args){
        String url = baseURL + method + "?" + buildArgs(args);        
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
    
    public String postRequest(String method, Map<String,String> args){
        StringBuilder response = new StringBuilder();
        try {
            String url = baseURL + method;
            URL obj = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();            
            conn.setRequestMethod("POST");
            
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(buildArgs(args));
            wr.flush();
            wr.close();
            
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + buildArgs(args));
            System.out.println("Response Code : " + responseCode);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;            

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();

            
        } catch (MalformedURLException ex) {
            Logger.getLogger(SlackApi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SlackApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response.toString();
    }
    
    private String buildArgs(Map<String,String> args){
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = args.keySet().iterator();
                      
        while(iterator.hasNext()) {
            String key = iterator.next();
            String value = args.get(key); 
            sb.append(key);
            sb.append("=");
            sb.append(value);
            
            if(iterator.hasNext()){
                sb.append("&");
            }            
        }        
        return sb.toString();
    }
    
}

package services;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import slack.Authentication;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ubuntu
 */
@Path("/auth")
public class Auth {
    
    	@GET
	public String autorization(@QueryParam("code") String code) {
            System.out.println("CODE");
            System.out.println(code);
            
            Authentication auth = new Authentication(code);
            String token = auth.getAccessToken();
            System.out.println(token);
            
            if(auth.saveToken(token)){
                return "Success Slack Authentication";
            }
            else{
                return "ERROR: Not authenticated";
            }
        }
    
}

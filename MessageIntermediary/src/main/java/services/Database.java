/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import Messaging.SQLliteDatabase;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author ubuntu
 */
@Path("/db")
public class Database {
    
    @GET
    @Path("/clear")
    public String clear(){     
        SQLliteDatabase db = new SQLliteDatabase();
        Connection conn = db.ConnectDB();
        boolean ok = db.deleteAll(conn);                
        if(ok){
            return "Deleted with success";
        }
        else{
            return "ERROR: Not deleted";
        }
    }
    
    
}

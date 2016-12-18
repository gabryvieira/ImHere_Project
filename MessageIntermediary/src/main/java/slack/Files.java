/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gabriel
 */
public class Files extends SlackApi{
    
    private String method;
    private String response;
    private String accesstoken = "xoxp-99703402119-99692193510-99713288343-bfda02fc8f56a6116088bca15da145ee";
 
    public Files(String accesstoken){
        this.accesstoken = accesstoken;
    }
    
    public String getAllFiles(){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        
        method = "files.list";
        response = getRequest(method, args);
        return response;
    }
    
    
    public String getFilesInfo(long fileID){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("file", fileID + "");
        
        method = "files.info";
        response = getRequest(method, args);
        return response;
    }
    
    public String uploadFile(String filename){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("filename", filename);
        
        method = "files.upload";
        response = getRequest(method, args);
        return response;
    }
    
    public String deleteFile(long fileID){
        Map<String, String> args = new HashMap<>();
        args.put("token", accesstoken);
        args.put("file", fileID+"");
        
        method = "files.delete";
        response = getRequest(method, args);
        return response;
    }
    
}

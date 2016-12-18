/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slack;

/**
 *
 * @author ubuntu
 */
public class testMail {
    public static void main(String[] args){
        SendMail mail = new SendMail();
        mail.send("esimhere@gmail.com", "pereira.jorge@ua.pt", "TestMail", "TEST1");
    }
}

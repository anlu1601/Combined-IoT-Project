/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import it.unipr.netsec.mjcoap.coap.message.*;
import it.unipr.netsec.mjcoap.coap.server.*;
import java.io.FileReader;
import java.net.SocketException;
import java.util.Scanner;
import java.time.LocalTime;

/**
 *
 * @author André
 */
public class CoapServer extends AbstractCoapServer{
    //CoapServer server = new CoapServer(5000);
                        
    
    //server.setResource("/test", CoapResource.FORMAT_TEXT_PLAIN_UTF8,"Hello world!".getBytes());

    public CoapServer(int port) throws SocketException{
        super(port);
    }
    
  
    @Override
    protected void handleGetRequest(CoapRequest req){
        
        // start timer
        LocalTime time = LocalTime.now();
        System.out.println("Coap Server Request at " + time);
        
        String resource_name = req.getRequestUriPath();
        try {
            String path = "..\\..\\sensors\\"+resource_name+".txt";
            System.out.println("Accessing sensor " + resource_name);
            StringBuilder sb = null;
            try (Scanner in = new Scanner(new FileReader(path)) //"..\\..\\..\\sensors\\test.txt"
            ) {
                sb = new StringBuilder();
                while(in.hasNext()) {
                    if(in.hasNext()){
                        sb.append(in.next());
                    } else
                        break;
                }
                
            } catch (Exception e){
                System.err.println(e);
            }
            String outString = sb.toString();
            
            CoapResponse resp = CoapMessageFactory.createResponse(req,CoapResponseCode._2_05_Content);
            resp.setPayload(CoapResource.FORMAT_TEXT_PLAIN_UTF8,outString.getBytes());
            
            
            time = LocalTime.now();
            System.out.println("Coap Server Respond at " + time + "\n");
        
            respond(req,resp);
            
        } catch (Exception e) {
            System.err.println(e);
            CoapResponse resp = CoapMessageFactory.createResponse(req,CoapResponseCode._4_04_Not_Found);
            respond(req,resp);
        }
        
    }

}

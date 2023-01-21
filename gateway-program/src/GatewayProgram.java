/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Scanner;
//import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.time.LocalTime;

/**
 *
 * @author André
 * 
 * RUN ORDER:
 * Sensor -> Broker -> Gateway
 * 
 * 
 */
public class GatewayProgram {
    
    
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        String path = "livingroom\\h";  // max-size 12 char?? \\ = -1 char
        String path2 = "livingroom\\t";
        String path3 = "livingroom\\b";
        
        String path4 = "kitchen\\b";

        String[] paths = {"livingroom\\t", "livingroom\\h", "livingroom\\b", "kitchen\\t", "kitchen\\h", "kitchen\\b"};
        
        // foreach sensor do this: runprogram(coap get), mqttpublis  
        
        boolean run = true;
        while(run){
            
            GatewayProgram program = new GatewayProgram();		
            /*String content = program.runProgram(path);
            String content2 = program.runProgram(path2);
            String content3 = program.runProgram(path3);
            String content4 = program.runProgram(path4);
            System.out.println("The content in "+path+ " is "+ content);

            program.mqttPublish(path, content);
            program.mqttPublish(path2, content2);
            program.mqttPublish(path3, content3);
            program.mqttPublish(path4, content4);*/
            
            for (String p : paths) {
                String content = program.runProgram(p);
                program.mqttPublish(p, content);
            }
            //String content = program.runProgram(path);
            //program.mqttPublish(path, content);
            
            Thread.sleep(30000);
        }
        
        
        
        
    }
        //program.testTypes();
        
        //CoapClient coapClient = new CoapClient();
        //coapClient.setParameters("./well-known/core", "Discover");
        //coapClient.showMoreInfo(true);
        //coapClient.send();
    
    
    String runProgram(String path){
        
        LocalTime time = LocalTime.now();
        
        System.out.println("Before Coap:" + time);
        
        String address = "127.0.0.1";
        int port = 5000;
        String payload = "";
        
        String type = "GET";
        
        Scanner scan = new Scanner(System.in);
        System.out.println("Send "+type+ " to " +address+"/" +path+ ":" + port);
        //scan.next();
        
        
        CoapClient coapClient = new CoapClient(address, port);
        coapClient.showMoreInfo(true);
       
            
        coapClient.setParameters(path, type);
        coapClient.setMessage(payload);
        coapClient.send();
        
        time = LocalTime.now();
        System.out.println("After Coap:" + time);
        
        return coapClient.getPayload();
    }
    
    void mqttPublish(String path, String content){
        String topic        = path;
        //String content      = "Message from MqttPublishSample";
        int qos             = 0;
        String broker       = "tcp://127.0.0.1:1883";
        String clientId     = "publish_sensor_data_client";
        MemoryPersistence persistence = new MemoryPersistence();
        
        
        try {
                MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: "+broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: "+content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
                sampleClient.disconnect();
                System.out.println("Disconnected");
                //System.exit(0);
            } catch(MqttException me) {
                System.out.println("reason "+me.getReasonCode());
                System.out.println("msg "+me.getMessage());
                System.out.println("loc "+me.getLocalizedMessage());
                System.out.println("cause "+me.getCause());
                System.out.println("excep "+me);
                me.printStackTrace();
            }
        
        LocalTime time = LocalTime.now();
        System.out.println("After MQTT:" + time);
    }
}

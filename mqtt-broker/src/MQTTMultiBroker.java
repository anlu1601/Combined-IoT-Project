/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author André
 */
public class MQTTMultiBroker extends Thread {
    ServerSocket serverSocket;	
    int port = 1883;
    ArrayList<subscribe> subs;
    ArrayList<String> clients;
    Map<String, String> retains;
    boolean runProgram = true;

    @Override
    public void run(){
        try {
            serverSocket = new ServerSocket(port);
            subs = new ArrayList<>();
            clients = new ArrayList<>();
            retains = new HashMap<>();
            
            while(runProgram){
                new MQTTBroker(serverSocket.accept()).start();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
		runProgram = false;
		
                System.exit(0);
    }
    
    private class MQTTBroker extends Thread{

	
	private Socket clientSocket;
        
	public MQTTBroker(Socket socket) {		
		try {
			this.clientSocket = socket;
                        
                    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//This will run in its own thread
		
		
			try {	
				//Accept incoming connecting
				//Socket socket = serverSocket.accept();	
                                Socket socket = this.clientSocket;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
						
                                System.out.println("Incoming connection");
                                
				//Print out the request
                                
                                int fixedHeader = 0;
                                int remLength = 0;
				try {
                                    fixedHeader = is.read();
                                    remLength = is.read();
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
				
                                
                                byte[] buffer = new byte[remLength+2];
				String incomingData = new String(buffer);				
				System.out.println(incomingData);
                                buffer[0] = (byte) fixedHeader;
                                buffer[1] = (byte) remLength;
                                is.read(buffer, 2, remLength);
				int packetType = (buffer[0] & 240)>> 4;
                                

                                String client = "";
                                
                                boolean quit = true;
                                
                                if(packetType == 1){ // CONNECT
                                    
                                    byte fH1 = (byte) 32; // Connack
                                    byte fH2 = (byte) 2;

                                    byte vH1 = (byte) 0;
                                    byte vH2 = (byte) 0; // Success


                                    
                                    quit = false;
                                    
                                    int fixedIndex = 4;
                                    int flags = 4;
                                    int protSize = buffer[fixedIndex-1];
                                    int sizeOfPay = 2;
                                    int payloadIndex = fixedIndex+protSize+flags+sizeOfPay;
                                    byte[] cl = new byte[buffer[payloadIndex-1]]; // make byte of correct size
                                    cl = Arrays.copyOfRange(buffer, payloadIndex, payloadIndex+buffer[payloadIndex-1]);
                                    client = new String(cl);
                                    
                                    
                                    byte conFlags = buffer[9];
//                                    System.out.println(conFlags[3]+" "+conFlags[2] +" "+ conFlags[1]+" "+conFlags[0]);
                                    System.out.println(conFlags);
                                    
                                   
                                    
                                    System.out.println("[" +client+ "]" + " connecting to server");
                                    
                                    if(clients.contains(client)){
                                        System.out.println("Error, same id");
                                        vH2 = (byte) 133; // Client Identifier not valid // 2 before why dont know
                                        quit = true;
                                    } else if((conFlags & 1) == 1){
                                        System.out.println("Error, Malformed packet");
                                        vH2 = (byte) 129; // malformed packet
                                        quit = true;
                                        
                                    }else {
                                        clients.add(client); // MQTT_FX_Client
                                    }
                                    
                                    byte[] outData = {fH1, fH2, vH1, vH2};
                                    

                                   
                                    os.write(outData);
                                    
                                    if(quit){
                                        os.close();
                                        is.close();
                                        socket.close();
                                    }
                                    
                                }
                                
                                
//                                System.out.println(client);
                                
                                while(!quit){
                                    
//                                    Arrays.fill(buffer, (byte)0);
                                    try {
                                        fixedHeader = is.read();
                                        remLength = is.read();
                                    } catch (Exception e) {
                                        System.out.println("Client "+client+" disconnected "+e.getMessage());
                                        quit = disconnect(client, socket);
                                    }
                                    
                                    buffer = new byte[remLength+2];
                                    buffer[0] = (byte) fixedHeader;
                                    buffer[1] = (byte) remLength;
                                    is.read(buffer, 2, remLength);
                                    
                                    packetType = (buffer[0] & 240)>> 4; // make control packet to 4-bit unsigned value
//                                    System.out.println("packetType: " + packetType);
                                    incomingData = new String(buffer);				
                                    //System.out.println(incomingData);
//                                    System.out.println(Arrays.toString(buffer));
//                                    System.out.println(buffer[0] & 0xff);
//                                    System.out.println(buffer[1] & 0xff);
                                    
                                    if(packetType == 14){ // DISCONNECT 14
                                        quit = disconnect(client, socket);
                                    }
                                    
                                    
                                    if(packetType == 8){  // SUBSCRIBE 8
                                        
                                        
                                        byte[] packetId = {buffer[2], buffer[3]};
                                        byte[] topic = new byte[buffer[5]];
                                        topic = Arrays.copyOfRange(buffer, 6, 4+buffer[5]+2);
                                        String topicString = new String(topic);
//                                        System.out.println(topicString);
                                        
                                        boolean sameSubError = false;

                                        System.out.println("["+client + "] subscribed to " + topicString);
                                        boolean existsTopic = false;
                                        
                                        for (subscribe sub1 : subs) {
                                            if(sub1.getTopic().equalsIgnoreCase(topicString)){
                                                if(sub1.exists(client)){
                                                    existsTopic = true;
                                                    sameSubError = true;
                                                    break;
                                                }else{
                                                    sub1.add(client, socket);
                                                    existsTopic = true;
                                                }

                                            }
                                        }
                                        if(!existsTopic){
                                            subscribe sub = new subscribe(topicString);
                                            sub.add(client, socket);
                                            subs.add(sub);
                                        }
                                        
                                        
                                        
                                        
                                        if(retains.containsKey(topicString)){
                                            byte b1 = (byte) 49;
                                            byte b2 = (byte) 0; // change to actual size
                                            
                                            byte b3 = (byte) 0;
                                            byte b4 = (byte) topic.length;
                                            
                                            
                                            String msg = retains.get(topicString);
//                                            byte[] messageBytes = new byte[msg.length()];
                                            

                                            byte[] messageBytes = msg.getBytes();
                                            
                                            b2 = (byte) (2+topic.length+messageBytes.length);
                                            
                                            byte[] first = {b1, b2, b3, b4};
                                            
                                            byte[] second = topic;
                                            
                                            byte[] sendBytes = new byte[b2+2];
                                            
                                            ByteBuffer buff = ByteBuffer.wrap(sendBytes);
                                            buff.put(first);
                                            buff.put(second);
                                            buff.put(messageBytes);
                                            
                                            byte[] comb = buff.array();
                                            
                                            os.write(comb);
                                        }
                                        
//                                        for (subscribe subss : subs) {
//                                             System.out.println(subss.getTopic());
//                                             System.out.println(subss.getClientList());
//                                        }
                                       
                                        
                                        // make resp message
                                        byte subfH1 = (byte) 144;
                                        byte subfH2 = (byte) 3;

                                        byte subvH1 = buffer[2];
                                        //byte subvH2 = (byte) 1; // must match client
                                        byte subvH2 = buffer[3];
                                        
                                        byte subPay = (byte) 0;
                                        
                                        if(sameSubError){
                                            subPay = (byte) 0x80; // 128
                                        }
                                        
                                        
                                        byte[] subAck = {subfH1, subfH2, subvH1, subvH2, subPay};
                                        os.write(subAck);
                                        
                                    }
                                    
                                    if(packetType == 12){ // 12 = PINGREQ
                                        System.out.println("Ping");
                                        
                                        byte pingH1 = (byte) 208;
                                        byte pingH2 = (byte) 0;
                                        
                                        byte[] pingResp = {pingH1, pingH2};
                                        os.write(pingResp);
                                        
                                    }
                                    
                                    if(packetType == 3){ // 3 = PUBLISH   
                                        
                                    
                                        int topicSize = buffer[3];
                                        String topic = new String(buffer, 4, topicSize);
//                                        System.out.println(5+topicSize + " " + (remLength - topicSize-2));
                                        String message = new String(buffer, 4+topicSize, remLength-topicSize-2);
//                                        System.out.println(topic + " " + message);
                                        
                                        if((buffer[0] & 1) == 1){
                                            retains.put(topic, message);
                                        }
                                        
                                        System.out.println("["+client + "] published in " + topic);
                                        
                                        publish(buffer, topic);
                                    }
                                    
                                    if(packetType == 10){ // UNSUBCRIBE
                                        
                                        boolean isRemoved = false;
                                        int length = buffer[5];
                                        String topicUnsub = new String(buffer, 6, length);
                                        //subs.removeIf(topic -> topic.equals(topicUnsub));
                                        for (subscribe sub : subs) {
//                                            System.out.println(topicUnsub + " == " + sub.getTopic());
                                            if(topicUnsub.equalsIgnoreCase(sub.getTopic())){
                                                sub.remove(client, socket);
                                                isRemoved = true;
                                                break;
                                            }
                                        }
                                        
                                        System.out.println(client + " unsubscribing from " + topicUnsub);
                                        
                                        byte b1 = (byte) 176;
                                        byte b2 = (byte) 3 ;
                                        
                                        byte b3 = buffer[2];
                                        byte b4 = buffer[3];
                                        
                                        byte bPayload = (byte) 0;
                                        
                                        if(!isRemoved){
                                            bPayload = (byte) 17;
                                        }
                                        
                                        byte[] retByte = {b1, b2, b3, b4, bPayload};
                                        
                                        os.write(retByte);
                                        
                                    }
                                    
//                                    System.out.println("Currently in SUBs: ");
//                                    for (subscribe sub : subs) {
//                                        System.out.println("Topic: " + sub.getTopic());
//                                        System.out.println("Clients: " + sub.getClientList());
//                                    }
                                }
				
//				String[] split = incomingData.split("\r\n");
//				//System.out.println(split[0]);				
//				String requestLine = split[0];
//				
//				String split2[] = requestLine.split(" ");
//				
//				String command = split2[0]; 
//				String path = split2[1]; 
//				String protocol = split2[2]; 
//				System.out.println(command);
//				System.out.println(path);
//				System.out.println(protocol);
//				
//				String responseHead = "";
//				String responseBody = "";
//				
//				
//				
//				String sendData = responseHead + responseBody;
//				os.write(sendData.getBytes());
			
			} catch (IOException e) {
				e.printStackTrace();
			}
				
	}
        
        
    }
    
    private void publish(byte[] buf, String topic){
            
            subscribe sub = null;
            for (subscribe sub1 : subs) {
                if(sub1.getTopic().equals(topic)){
                    sub = sub1;
                }
            }
            
            if(sub != null){
              try {
                
                for (Socket clientSocket : sub.getClientSockets()) {
                    OutputStream os = clientSocket.getOutputStream();
                    os.write(buf);
                
                }
                } catch (Exception e) {
                    System.out.println(e);
                }  
            }else{
                System.out.println("This topic doesn't exist");
            }
    
    }
    
    private boolean disconnect(String client, Socket socket){
        System.out.println("["+client+ "] disconnected");
        
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            clients.remove(client);
            for (subscribe sub : subs) {
                sub.remove(client, socket);
            }
            System.out.println("Active clients: " + clients);

            os.close();
            is.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Couldn't disconnect client "+ e.getMessage());
        }
        
        
        return true;
    }
    
    private class subscribe{
            private String topic;
            private ArrayList<String> clientList;
            private ArrayList<Socket> clientSockets;
            
            public subscribe(String topic) {
                this.topic = topic;
                this.clientList = new ArrayList<>();
                this.clientSockets = new ArrayList<>();
            }
            
            public void add(String client, Socket socket){
                this.clientList.add(client);
                this.clientSockets.add(socket);
            }
            
            public void remove(String client, Socket socket){
                this.clientList.remove(client);
                this.clientSockets.remove(socket);
            }
            
            public boolean exists(String client){
                return clientList.contains(client);
            }
            
            public String getTopic(){
                return this.topic;
            }
            
            public ArrayList<String> getClientList(){
                return this.clientList;
            }
            
            public ArrayList<Socket> getClientSockets(){
                return this.clientSockets;
            }
            
        } 
    
}

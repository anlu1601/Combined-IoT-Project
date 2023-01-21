/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author André
 */
public class CoapClient {

    private String path;
    private int port;
    private String address;
    private String type;
    private int msgId;
    private String payload;
    private boolean showInfo;
    private String recPayload;

    public CoapClient() {
        this.address = "coap.me";
        this.port = 5683;
        this.type = "GET";
        this.path = "test";
        this.payload = "";
        this.recPayload = "";
        this.msgId = ThreadLocalRandom.current().nextInt(0, 65535);
        this.showInfo = false;
    }
    
    
    public CoapClient(String address, int port) {
        this.address = address;
        this.port = port;
        this.type = "GET";
        this.path = "test";
        this.payload = "";
        this.recPayload = "";
        this.msgId = ThreadLocalRandom.current().nextInt(0, 65535);
        this.showInfo = false;
    }
    
    public void setDest(String address, int port){
        this.address = address;
        this.port = port;
    }
    
    public void setParameters(String path, String type){
        this.path = path;
        this.type = type;
    }
    public void setMessage(String payload){
        this.payload = payload;
    }
    
    public void showMoreInfo(boolean b){
        this.showInfo = b;
    }
    
    public void send(){
        
        //System.out.println("STARTED...");
        DatagramSocket udpSocket;
        
        byte[] message = chooseType();
        System.out.println("Packet generated...");
        this.recPayload = "";
        
        //printMessage(message);
        //InetAddress address;
        
        try {
            if(this.showInfo){
                System.out.println(Arrays.toString(message));
                printHead(message);
            }
            InetAddress address = InetAddress.getByName(this.address); //coap.me/test
            DatagramPacket packet = new DatagramPacket(message, message.length, address, this.port);
            
            udpSocket = new DatagramSocket();
            udpSocket.send(packet);
            System.out.println(this.type + " packet sent to " + this.address + "/" + this.path + ":" + this.port);
            
            message = new byte[65536];
            packet = new DatagramPacket(message, message.length);
            udpSocket.receive(packet);
            System.out.println("Packet received...");
            byte[] recByte = shortenPacket(packet.getData());
            
            ArrayList<byte[]> list = getHeaderPayload(recByte);
            if(this.showInfo){
                System.out.println(Arrays.toString(recByte));
                printHead(list.get(0));
            }
            String recPayload = new String(list.get(1), 0, list.get(1).length);
            
            System.out.println("Received payload: " + recPayload);
            this.recPayload = recPayload;
            
            udpSocket.close();
            //System.out.println("CLOSED");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private byte[] chooseType(){
        type = this.type;
        byte[] msg = null;
        
        try {
            if(type.equalsIgnoreCase("get")){
            msg = getMessage();
            } else if(type.equalsIgnoreCase("put")){
                msg = putMessage();
            } else if(type.equalsIgnoreCase("post")){
                msg = postMessage();
            } else if(type.equalsIgnoreCase("delete")){
                msg = deleteMessage();
            } else if(type.equalsIgnoreCase("discover")){
                msg = discoverMessage();
            } else{
                //System.err.println("ERROR, NO SUCH TYPE EXIST");
                throw new Exception("ERROR, NO SUCH METHOD EXIST");
            }
        } catch (Exception e) {
            //System.err.println(e);
           e.printStackTrace();
        }
        
        return msg;
    }
    
    private ArrayList<byte[]> getHeaderPayload(byte[] msg){
        byte[] payload = new byte[1024];
        byte[] header = new byte[256];
        ArrayList<byte[]> arr = new ArrayList<>();
        boolean head = true;
        byte ffByt[] = new byte[] {(byte) 255};
        //System.out.println(ffByt[0]);
//        for (int i = 0; i < msg.length; i++) {
//            
//            if(msg[i] == ffByt[0]){ //b == "11111111"
//                head = false;
//                //.out.println("TESTING");
//                continue;
//            }
//            //System.out.println(i + " " + msg[i] + " " + b);
//            if(head == false){
//                payload[i] = msg[i];
//            } else if(head){
//                header[i] = msg[i];
//            }
//            //p[i] = msg[i];
//            
//        }
        int breakIndex;
        for (breakIndex = 0; breakIndex < msg.length; breakIndex++) {
            if(msg[breakIndex] == ffByt[0]){
                break;
            }    
        }
        
        header = Arrays.copyOfRange(msg, 0, breakIndex-1);
        payload = Arrays.copyOfRange(msg, breakIndex+1, msg.length);

        arr.add(header);
        arr.add(payload);
        
        return arr;
    }
    
    private void printHead(byte[] msg){
        byte first = msg[0];
        byte code = msg[1];
        byte id1 = msg[2];
        byte id2 = msg[3];
        
        byte version = first;
        version = (byte) ((byte) version >> 6);
        
        byte type = first;
        type = (byte) ((byte) type >> 4);
        type = (byte) (type & (byte)3);
        
        byte tL = first;
        tL = (byte) (tL & (byte)15);
        
        short id = (short)id1;
        id <<= 8;
        id |= id2;
        
        System.out.println("HEADER*************");
        System.out.println("Version: " + version);
        System.out.println("Type: " + type);
        System.out.println("Token Length: " + tL);
        System.out.println("Code: " + (code & 0xff));
        System.out.println("MessageID: " + (id & 0xffff ));
        System.out.println("*******************");
    }
    
    private byte[] shortenPacket(byte[] msg){
        int zeroBytes = 0;
        for (int i = 0; i < msg.length; i++) {
            byte b = msg[i];
            if(b == 0x00)
                zeroBytes++;
        }
        //System.out.println("MSGsize" + msg.length + " zerobytes" + zeroBytes);
        byte[] newPacket = new byte[msg.length-zeroBytes];
        
        for (int i = 0; i < newPacket.length; i++) {
            newPacket[i] = msg[i];
            
        }
        
        return newPacket;
    }
    
    
    private byte[] getMessage(){
        
        path = this.path;
        int msgId = this.msgId;
        byte header1 = (byte)80;// 0101 0000
        byte header2 = (byte)1; // 00000001
        byte msgIdRnd1 = (byte)(msgId & 0xff);//(byte)170; // 10101010 // Need to make random and increase
        byte msgIdRnd2 = (byte)((msgId >>> 8) & 0xff);//(byte)85; // 01010101
        this.msgId++;
        
        int pathLength = path.length();
        byte pL = (byte)pathLength;
        
        
        byte uriHeader = (byte)176; //1011 0000 size = 4
        
        uriHeader = (byte) (uriHeader | pL);
        //String g = "test";
        byte[] uriPath = path.getBytes();
        
        byte formatHeader = (byte)16; // 0001 0000
        
        byte[] message = new byte[6+uriPath.length];
        message[0] = header1;
        message[1] = header2;
        message[2] = msgIdRnd1;
        message[3] = msgIdRnd2;
        message[4] = uriHeader;
        for (int i = 0; i < uriPath.length; i++) {
            message[i+5] = uriPath[i];
            
        }
        
        message[5+uriPath.length] = formatHeader;
        
        
        return message;
    }
    
    private byte[] postMessage(){
        
        path = this.path;
        String payL = this.payload;
        int msgId = this.msgId;
        byte header1 = (byte)80;// 0101 0000
        byte header2 = (byte)2; // 00000001
        byte msgIdRnd1 = (byte)(msgId & 0xff);//(byte)170; // 10101010 // Need to make random and increase
        byte msgIdRnd2 = (byte)((msgId >>> 8) & 0xff);//(byte)85; // 01010101
        this.msgId++;
        
        int pathLength = path.length();
        byte pL = (byte)pathLength;
        
        
        byte uriHeader = (byte)176; //1011 0000 size = 4
        
        uriHeader = (byte) (uriHeader | pL);
        //String g = "test";
        byte[] uriPath = path.getBytes();
        
        byte formatHeader = (byte)16; // 0001 0000
        
        byte br = (byte) 0xff;
        
        byte[] payload = payL.getBytes();
        
        
        byte[] message = new byte[7+uriPath.length + payload.length];
        message[0] = header1;
        message[1] = header2;
        message[2] = msgIdRnd1;
        message[3] = msgIdRnd2;
        message[4] = uriHeader;
        for (int i = 0; i < uriPath.length; i++) {
            message[i+5] = uriPath[i];
            
        }
        
        message[5+uriPath.length] = formatHeader;
        message[6+uriPath.length] = br;
                
        for (int i = 0; i < payload.length; i++) {
            message[i+7+uriPath.length] = payload[i];
            
        }
        
        
        return message;
    }
    
    private byte[] putMessage(){
        
        path = this.path;
        String payL = this.payload;
        int msgId = this.msgId;
        byte header1 = (byte)80;// 0101 0000
        byte header2 = (byte)3; // 00000001
        byte msgIdRnd1 = (byte)(msgId & 0xff);//(byte)170; // 10101010 // Need to make random and increase
        byte msgIdRnd2 = (byte)((msgId >>> 8) & 0xff);//(byte)85; // 01010101
        this.msgId++;
        
        int pathLength = path.length();
        byte pL = (byte)pathLength;
        
        
        byte uriHeader = (byte)176; //1011 0000 size = 4
        
        uriHeader = (byte) (uriHeader | pL);
        //String g = "test";
        byte[] uriPath = path.getBytes();
        
        byte formatHeader = (byte)16; // 0001 0000
        
        byte br = (byte) 0xff;
        
        byte[] payload = payL.getBytes();
        
        
        byte[] message = new byte[7+uriPath.length + payload.length];
        message[0] = header1;
        message[1] = header2;
        message[2] = msgIdRnd1;
        message[3] = msgIdRnd2;
        message[4] = uriHeader;
        for (int i = 0; i < uriPath.length; i++) {
            message[i+5] = uriPath[i];
            
        }
        
        message[5+uriPath.length] = formatHeader;
        message[6+uriPath.length] = br;
                
        for (int i = 0; i < payload.length; i++) {
            message[i+7+uriPath.length] = payload[i];
            
        }
        
        return message;
    }
    
    private byte[] deleteMessage(){
        
        path = this.path;
        String payL = this.payload;
        int msgId = this.msgId;
        byte header1 = (byte)80;// 0101 0000
        byte header2 = (byte)4; // 00000001
        byte msgIdRnd1 = (byte)(msgId & 0xff);//(byte)170; // 10101010 // Need to make random and increase
        byte msgIdRnd2 = (byte)((msgId >>> 8) & 0xff);//(byte)85; // 01010101
        this.msgId++;
        
        int pathLength = path.length();
        byte pL = (byte)pathLength;
        
        
        byte uriHeader = (byte)176; //1011 0000 size = 4
        
        uriHeader = (byte) (uriHeader | pL);
        //String g = "test";
        byte[] uriPath = path.getBytes();
        
        byte formatHeader = (byte)16; // 0001 0000
        
        byte br = (byte) 0xff;
        
        byte[] payload = payL.getBytes();
        
        
        byte[] message = new byte[7+uriPath.length + payload.length];
        message[0] = header1;
        message[1] = header2;
        message[2] = msgIdRnd1;
        message[3] = msgIdRnd2;
        message[4] = uriHeader;
        for (int i = 0; i < uriPath.length; i++) {
            message[i+5] = uriPath[i];
            
        }
        
        message[5+uriPath.length] = formatHeader;
        message[6+uriPath.length] = br;
                
        for (int i = 0; i < payload.length; i++) {
            message[i+7+uriPath.length] = payload[i];
            
        }
        
        
        return message;
    }
    
    private byte[] discoverMessage(){
        path = this.path;
        int msgId = this.msgId;
        byte header1 = (byte)80;// 0101 0000
        byte header2 = (byte)1; // 00000001
        byte msgIdRnd1 = (byte)(msgId & 0xff);//(byte)170; // 10101010 // Need to make random and increase
        byte msgIdRnd2 = (byte)((msgId >>> 8) & 0xff);//(byte)85; // 01010101
        this.msgId++;
        
        int pathLength = path.length();
        byte pL = (byte)pathLength;
        
        
        byte uriHeader = (byte)176; //1011 0000 size = 4
        
        uriHeader = (byte) (uriHeader | 15);
        //String g = "test";
        byte optionDeltaEx = (byte)0;
        byte optionLengthEx = (byte)1;
        byte[] uriPath = path.getBytes();
        String s = new String(uriPath, 0, uriPath.length);
        System.out.println(s);
        System.out.println(Arrays.toString(uriPath));
        System.out.println((uriHeader & 0xff));
        byte formatHeader = (byte)16; // 0001 0000
        
        byte[] message = new byte[8+uriPath.length];
        message[0] = header1;
        message[1] = header2;
        message[2] = msgIdRnd1;
        message[3] = msgIdRnd2;
        message[4] = uriHeader;
        message[5] = optionDeltaEx;
        message[6] = optionLengthEx;
        for (int i = 0; i < uriPath.length; i++) {
            message[i+7] = uriPath[i];
            
        }
        
        message[7+uriPath.length] = formatHeader;
        
        return message;
    }
    
    private void printMessage(byte[] msg){
        for (byte b : msg) {
            System.out.println(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
    }
    
    private void parseReturn(byte[] rec){
        String i = Integer.toBinaryString((rec[0] & 0xFF) + 0x100).substring(1);
        System.out.println(i.substring(0, 2));
        if(i.substring(0, 2).equals("01")){
            System.out.println("Version 1");
        }
        if(i.substring(2, 4).equals("01")){
            System.out.println("Non-Confirmable");
        }
        if(i.substring(4).equals("0000")){
            System.out.println("No token");
        }
        System.out.println(i);
        i = Integer.toBinaryString((rec[1] & 0xFF) + 0x100).substring(1);        
        System.out.println("Code is " + i);
        i = Integer.toBinaryString((rec[1] & 0xFF) + 0x100).substring(1);    
        String id = i;
        i = Integer.toBinaryString((rec[1] & 0xFF) + 0x100).substring(1);    
        id = id + i;
        System.out.println("Message ID is " + id);
    }
    
    public String getPayload(){
        return this.recPayload;
    }
    
}

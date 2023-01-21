/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Scanner;

/**
 *
 * @author André
 * 
 * 
 * RUN ORDER:
 * Sensor -> Broker -> Gateway
 * 
 * 
 */
public class BrokerProgram {
    public static void main(String[] args) {
		BrokerProgram program = new BrokerProgram();		
		program.runProgram();
	}
	
	
	void runProgram(){
		try {
			//Create our server		
			//MQTTBroker mqttBroker = new MQTTBroker();
					
			//Start it in another thread
			//mqttBroker.start();
			
                        
                        MQTTMultiBroker mqttBroker = new MQTTMultiBroker();
                        
                        mqttBroker.start();
                        
                        
			//Main program wait
			Scanner scan = new Scanner(System.in);
                        System.out.println("MQTT Broker Running, Press any key to exit ");
                        scan.nextLine();
                        
                        
			mqttBroker.shutdown();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

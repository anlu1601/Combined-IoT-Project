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
public class SensorProgram {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        SensorProgram program = new SensorProgram();		
	program.runProgram();
        
        
        
    }
    void runProgram(){
		try {
			
			
                        
                        CoapServer server = new CoapServer(5000);
                        
                        
			//Main program wait
			Scanner scan = new Scanner(System.in);
                        System.out.println("Sensor server Running, Press any key to exit ");
                        scan.nextLine();
                        
                        
			System.exit(0);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
}

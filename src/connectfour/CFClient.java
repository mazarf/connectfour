package connectfour;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class CFClient {
	
	public static void main(String args[]) throws IOException {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket = new Socket("10.0.0.14", 6789);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		System.out.println("Connected! Waiting for player 1...");
		while(true) {
			String line = "";
			while((line = inFromServer.readLine()) != null) {
				if(!line.equals("GO"))
					System.out.println(line);
				else
					break;
			}
			String choice = inFromUser.readLine();
			outToServer.writeBytes(choice + '\n');
		}
	}
}

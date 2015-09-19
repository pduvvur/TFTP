import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TFTPClient {

	public static void sendRRQ(String fileName) throws IOException{
		DatagramSocket sock = new DatagramSocket(7011);
		byte[] recvData = new byte[516];
		InetAddress ipAddr = InetAddress.getByName("glados.cs.rit.edu");
		DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
		int flag = -1;

		String filePath = fileName.substring(15,fileName.length());
		FileOutputStream fos = null;  

		byte[] fileNameByteArr = fileName.getBytes();

		byte[] sendInitialArray = new byte[200];
		sendInitialArray[0] = 0;
		sendInitialArray[1] = 1;

		for(int i = 2 ; i < fileName.length()+2 ; i++){
			sendInitialArray[i] = fileNameByteArr[i-2];
		}
		sendInitialArray[2 + fileName.length()] = 0;

		byte[] octetArr = new String("octet").getBytes();

		for(int i = fileName.length()+3 ; i < fileName.length()+8 ; i++){
			sendInitialArray[i] = octetArr[i - fileName.length()-3];
		}

		sendInitialArray[8 + fileName.length()] = 0;
		DatagramPacket sendPacket = new DatagramPacket(sendInitialArray, sendInitialArray.length, ipAddr, 69 );
		sock.send(sendPacket);

		int createFOS = -1;
		do {
			sock.receive(recvPacket);
			recvData = recvPacket.getData();
			if(recvData[1] == 5){
				String errMsg = new String(recvData);
				System.out.println("Server says: " + errMsg.substring(4));
				flag = 1;
			} else {
				if(createFOS == -1){
					fos = new FileOutputStream(filePath);
					createFOS = 1;
				} 
				byte[] tempBuf = new byte[recvPacket.getLength() - 4];
				System.arraycopy(recvData, 4, tempBuf, 0, recvPacket.getLength()-4);
				fos.write(tempBuf);  

				byte[] ackArrPacket = new byte[4];
				ackArrPacket[0] = 0;
				ackArrPacket[1] = 4;
				ackArrPacket[2] = recvData[2];
				ackArrPacket[3] = recvData[3];
				DatagramPacket sendPacket2 = new DatagramPacket(ackArrPacket, ackArrPacket.length, recvPacket.getAddress(), recvPacket.getPort() );
				sock.send(sendPacket2); 

				if(recvPacket.getLength() < 516){
					flag = 1;
					sock.close();
				}
			}
		} while(flag != 1);
	}

	public static void main(String[] args) throws IOException, InterruptedException{

		Scanner scan = new Scanner (System.in);
		while(true){
			System.out.println("Enter one of the following keywords - CONN / PUT / GET / QUIT / ?");

			String option = scan.nextLine().toUpperCase();
			if(option.equals("CONN")){
				System.out.println("Connection has been established with TFTP server.");
				System.out.println("Enter PUT / GET / EXIT / ?");
				option = scan.nextLine().toUpperCase();
				if(option.equals("PUT")){
					System.out.println("You do not have permission to write to the TFTP server.");
					break;
				} else if(option.equals("GET")){
					System.out.println("Enter the file name with full path:");
					String fName = scan.nextLine();
					sendRRQ(fName);
					break;
				} else if(option.equals("QUIT")){
					System.out.println("Disconnecting from server...");
					System.exit(0);
				}
			} else if(option.equals("PUT")){
				System.out.println("You do not have permission to write to the TFTP server.");
				continue;
			} else if(option.equals("GET")){
				System.out.println("Please connect to the server and then try this option.");
				continue;
			} else if(option.equals("QUIT")){
				System.exit(0);
			} else if(option.equals("?")){
				System.out.println("Steps to receive a file from the server:");
				System.out.println("   1. Establish connection to the server by typing CONN.");
				System.out.println("   2. Next, enter the GET option.");
				System.out.println("   3. Then enter the file name. The path should be /local/sandbox/yourfile.ext");
				System.out.println();
				System.out.println("If you would like to quit. Please enter QUIT");
				System.out.println();
				System.out.println("You will not have access to use PUT.");
				continue;
			} else {
				System.out.println("Invalid choice");
				continue;
			}
		}
	}
}


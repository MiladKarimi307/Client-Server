import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static DataInputStream socketInputStream;
	private static DataOutputStream socketOutputStream;
	private static Socket socket;
	private static Scanner scanner;

	public static void main(String[] args) {

		try {
			// localhost needs to be changed to args[0]
			socket = new Socket("localhost", 2222); // connect to the server
			socketInputStream = new DataInputStream(socket.getInputStream()); // socket input stream
			socketOutputStream = new DataOutputStream(socket.getOutputStream()); // socket output stream
			scanner = new Scanner(System.in);
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.out.println("Please enter the host name or ip address");
			// aiobe.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException uhe) {
			System.out.println("Unable to connect to the server");
			// uhe.printStackTrace();
			System.exit(2);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(3);
		}
		
		displayMenu();
		String comm;
		do {
			while(!scanner.hasNextLine()) {
				try {
					// because of netstat
					if (socketInputStream.available() > 0) System.out.println(socketInputStream.readUTF());

					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e2) {
					System.out.println(e2);
				}
			}
			comm = scanner.nextLine().toLowerCase();
			if (isValid(comm)) {
				try {
					socketOutputStream.writeUTF(comm);
					comm = socketInputStream.readUTF();
					System.out.println(comm);
				} catch (IOException e1) {
					System.out.println("Unable to connect to the server.");
					System.out.println("Program closed");
					System.exit(3);
					
				}
			} else { 
				if(comm.equals("quit") || comm.equals("7")) break;
				System.out.println("'"+ comm +"' is not a valid entry.");
				System.out.println(" Please enter a command from the following choices ");
				displayMenu();
			}
		} while (true);
		System.out.println("Program closed");
		try {
			socketOutputStream.writeUTF("quit");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// print client menue on screen
	private static void displayMenu() {
		System.out.println("\n1] Host current Date and Time\n2] Host uptime\n"
				+ "3] Host memory use\n4] Host Netstat\n5] Host current users\n"
				+ "6] Host running processes\n7] Quit");

	}

	private static boolean isValid(String str) {
		String[] commList = {"host current date and time","current date and time","date and time","date","time",
				"host uptime","uptime","host memory use","memory use","memory","host netstat","netstat",
				"host current users","host running processes","1","2","3","4","5","6"};
		for (Integer i = 0; i < commList.length; i++) {
			if ((str.compareTo(commList[i]) == 0))
				return true;
		}
		return false;
	}
}

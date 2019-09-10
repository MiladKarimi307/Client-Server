import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static DataInputStream socketInputStream;
	private static DataOutputStream socketOutputStream;
	private static Socket socket;
	private static Scanner scanner;

	public Client(String hostAddress) throws UnknownHostException, IOException, NumberFormatException{

		try {
			// ask user for port number & connect to the server
			System.out.print("Enter port number: ");
			socket = new Socket(hostAddress, Integer.valueOf(scanner.nextLine()));
		} catch (NumberFormatException nfe) {
			System.out.println("Invalid port number");
			System.exit(1);
		}
		// socket input stream
		socketInputStream = new DataInputStream(socket.getInputStream());

		// socket output stream
		socketOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	public static void main(String[] args) {

		try {
			scanner = new Scanner(System.in);
			new Client(args[0]);
			
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.out.println("Please enter the host name or ip address\n");
			System.exit(0);
		} catch (UnknownHostException uhe ) {
			System.out.println("Unable to connect to the server");
			System.exit(1);
		} catch (ConnectException ce ) {
			System.out.println("Connection refused.");
			System.exit(1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(4);
		}
		System.out.println("\nSelect from one of the following choices:");
		
		// display the menu to the user
		displayMenu();
		System.out.print("\n>> ");
		String comm;
		do {
			while (!scanner.hasNextLine()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// get user input
			comm = scanner.nextLine().toLowerCase();
			
			// if the command is valid send it to the server 
			if (isValid(comm)) {
				try {
					
					// send request to the serve
					socketOutputStream.writeUTF(comm);
					
					if(comm.equals("4") || comm.contains("netstat")) {
						System.out.println(" This process could take a while");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(" Please wait...");
					} 
					
					// receive reply from server
					comm = socketInputStream.readUTF();
					
					// print the reply
					System.out.println(comm);
					System.out.print("\n>> ");
				} catch (IOException e1) {
					System.out.println("Unable to connect to the server.");
					System.out.println("Program closed");
					System.exit(3);

				}
			} else {
				
				// leave the main loop if user command 'quit'.
				if (comm.equals("quit") || comm.equals("7") || comm.equals("exit"))
					break;
				
				// display menu if user enters 'menu' or '0'
				if (comm.equals("menu") || comm.equals("0")) {
					System.out.println();
					displayMenu();
					System.out.print("\n>> ");
					continue;
				}
					
				// print an error and redisplay menu if user's command is not valid 
				System.out.println("'" + comm + "' is not a valid entry.");
				System.out.println(" Please enter a command from the following choices \n");
				displayMenu();
				System.out.print("\n>> ");
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
		System.out.println(" 1] Host current Date and Time\n 2] Host uptime\n"
				+ " 3] Host memory use\n 4] Host Netstat\n 5] Host current users\n"
				+ " 6] Host running processes\n 7] Quit");

	}
	
	// check for validity of user input
	private static boolean isValid(String str) {
		String[] commList = { "host current date and time", "current date and time", "date and time", "date", "time",
				"host uptime", "uptime", "host memory use", "memory use", "memory", "host netstat", "netstat",
				"host current users", "current users", "users", "host running processes", "processes", "1", "2", "3", "4", "5", "6" };
		for (Integer i = 0; i < commList.length; i++) {
			if ((str.compareTo(commList[i]) == 0))
				return true;
		}
		return false;
	}
}

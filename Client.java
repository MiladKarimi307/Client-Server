import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

	private static DataInputStream socketInputStream;
	private static DataOutputStream socketOutputStream;
	private static Socket socket;
	private static Scanner scanner;
	private static Integer port;
	private static String hostAddress;

	public Client(String hostAddress, Integer port) throws UnknownHostException, IOException, NumberFormatException {

		// connect to the server
		socket = new Socket(hostAddress, port);

		// socket input stream
		socketInputStream = new DataInputStream(socket.getInputStream());

		// socket output stream
		socketOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	public static void main(String[] args) {

		// ask for port number & connect to the server
		try {
			// FIXME: host address needs to be changed to args[0];
			hostAddress = "localhost";
			scanner = new Scanner(System.in);
			System.out.print("Enter port number: ");
			port = Math.abs(Integer.valueOf(scanner.nextLine()));
			new Client(hostAddress, port);

		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.out.println("Please enter the host name or ip address\n");
			System.exit(0);
		} catch (NumberFormatException nfe) {
			System.out.println("Invalid port number");
			System.exit(1);
		} catch (ConnectException ce) {
			System.out.println("Connection refused.");
			System.exit(1);
		} catch (UnknownHostException uhe) {
			System.out.println("Unable to connect to the server");
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

					if (comm.equals("4") || comm.contains("netstat")) {
						System.out.println(" This process could take a while");
						try {
							Thread.sleep(1500);
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

				if (comm.equals("8")) {
					int n = 0;
					System.out.print(" Enter number of clients: ");
					try {
						n = Math.abs(Integer.valueOf(scanner.nextLine()));
					} catch (NumberFormatException nfe) {
						System.out.println(" Invalid input: 'number of clients' must be an integer number\n");
					}
					System.out.println("\n Select a command to be executed " + n + " times.");
					displayMenu();
					System.out.println(">> ");
					String s = scanner.nextLine();
					ArrayList<thread> cList = new ArrayList<>();
					Long average = 0L;
					if (isValid(s)) {
						synchronized (cList) {
							// create n number of threads of clinet class
							for (int i = 0; i < n; i++) {

								try {

									// add each client to the list
									cList.add(new thread(hostAddress, port, s));
								} catch (NumberFormatException e) {
									e.printStackTrace();
								} catch (UnknownHostException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							// start the threads and calculate the avg latency
							for (int i = 0; i < n; i++) {
								cList.get(i).run();
								System.out.println(" " + (i + 1) + ")   Latency: "
										+ (cList.get(i).timeB - cList.get(i).timeA) + "ms");
								average += cList.get(i).timeB - cList.get(i).timeA;
							}
							System.out.println(" The average latency is: " + average / n
									+ "ms and the whole proccess took " + average + "ms/" + average / 1000 + "s");
							try {
								while (socketInputStream.available() > 0)
									socketInputStream.readUTF();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						System.out.println();
						displayMenu();
						System.out.print("\n>> ");
						continue;
					}
				}

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
				"host current users", "current users", "users", "host running processes", "processes", "1", "2", "3",
				"4", "5", "6" };
		for (Integer i = 0; i < commList.length; i++) {
			if ((str.compareTo(commList[i]) == 0))
				return true;
		}
		return false;
	}

	public static class thread extends Client implements Runnable {

		Long timeA;
		Long timeB;
		String request;
		String reply;

		public thread(String hostAddress, Integer port, String request)
				throws UnknownHostException, IOException, NumberFormatException {
			super(hostAddress, port);
			this.request = request;
		}

		public void run() {
			timeA = System.currentTimeMillis();
			try {
				socketOutputStream.writeUTF(request);
				reply = socketInputStream.readUTF();
				timeB = System.currentTimeMillis();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

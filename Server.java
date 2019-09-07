
import java.io.*;
import java.net.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {

	static ServerSocket serverSocket;
	static userInfo user;
	static DataInputStream userInput;
	static DataOutputStream userOutput;
	static Listener listener;
	static ArrayList<userInfo> connectionsList;
	static Long startTime;

	public static void main(String[] args) throws IOException, InterruptedException {

		serverSocket = new ServerSocket(2222); // listen to a specific port
		startTime = System.currentTimeMillis();
		connectionsList = new ArrayList<userInfo>();
		listener = new Listener();
		listener.start();
		System.out.println("Server is listening...");
		while (true) {
			while (connectionsList.isEmpty())
				Thread.sleep(1000);
			
			user = connectionsList.get(0);
			userInput = user.inputStream; // socket input stream
			userOutput = user.outputStream; // socket output stream
			if (userInput.available() == 0) { // if 0 data available in socket input stream
				connectionsList.add(connectionsList.remove(0));
				continue;
			}
			java.awt.Toolkit.getDefaultToolkit().beep();
			user.request = userInput.readUTF();

			switch (user.request) {
			case "1":
			case "host current date and time":
			case "current date and time":
			case "date and time":
			case "date":
			case "time":
				userOutput.writeUTF(currentDateTime());
				break;
			case "2":
			case "host uptime":
			case "uptime":
				userOutput.writeUTF(upTime());
				break;
			case "3":
			case "host memory use":
			case "memory use":
			case "memory":
				userOutput.writeUTF(memoryUse());
				break;
			case "4":
			case "host netstat":
			case "netstat":
				for (String str : netstat()) {
					user.outputStream.writeUTF(str);
				}
				break;
			case "5":
			case "host current users":
			case "current users":
			case "users":
				userOutput.writeUTF(currentUsers());
				break;
			case "6":
			case "host running processes":
			case "running processes":
			case "processes":
				runningProcesses();
				break;
			case "7":
			case "quit":
			case "exit":
				userOutput.writeUTF("case 7");
				user.socket.close();
				connectionsList.remove(0);
				break;

			default:
				userOutput.writeUTF("Not a valid entry");
				break;
			}
		}

	}

	@SuppressWarnings("deprecation")
	private static String currentDateTime() {
		return new Time(System.currentTimeMillis()).toLocaleString();

	}

	private static String upTime() {
		return "Server uptime: " + new Time(System.currentTimeMillis() - startTime).toString().substring(3)
				+ "\nUser uptime: " + new Time(System.currentTimeMillis() - user.loginTime).toString().substring(3);

	}

	private static String memoryUse() {
		return "   Free Memory\t  Memory in Use    Total Memory\n"
				+ "  -------------  ---------------  --------------\n    "
				+ String.valueOf(Runtime.getRuntime().freeMemory() / (1024L * 1024L)) + "."
				+ String.valueOf(Runtime.getRuntime().freeMemory() % (1024L * 1024L)).substring(0, 2) + "MB\t    " +

				String.valueOf(
						(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L))
				+ "."
				+ String.valueOf(
						(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) % (1024L * 1024L))
						.substring(0, 3)
				+ "MB\t    " +

				String.valueOf(Runtime.getRuntime().totalMemory() / (1024L * 1024L)) + "."
				+ String.valueOf(Runtime.getRuntime().totalMemory() % (1024L * 1024L)).substring(0, 2) + "MB";
	}

	private static ArrayList<String> netstat() {
		try {
			Process p = Runtime.getRuntime().exec("cmd netstat");
			DataInputStream netstatInputStream = new DataInputStream(p.getInputStream());
			ArrayList<String> netstatOutput = new ArrayList<String>();
			while (netstatInputStream.available() > 0) {

				netstatOutput.add(netstatInputStream.readUTF());
			}
			return netstatOutput;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private static String currentUsers() {
		String result = "";
		for (int i = 0; i < connectionsList.size(); i++) {
			if(isOnline)
			result += "\n" + (i + 1) + ")   IP Address:"
					+ connectionsList.get(i).socket.getRemoteSocketAddress().toString() + "   Local Address:"
					+ connectionsList.get(i).socket.getInetAddress().toString();
		}
		return result;
	}

	private static ArrayList<String> runningProcesses() {
		
		try {
			ArrayList<String> result = new ArrayList<String>();
		    Process p = Runtime.getRuntime().exec("cmd /c tasklist");
		    BufferedReader input =
		            new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while (result.add(input.readLine())) {
		    	// System.out.println(line); 
		    }
		    input.close();
		    for(String str: result) System.out.println(str);
		    return result;
		} catch (Exception err) {
		    err.printStackTrace();
		}
		return null;
		
		
	}

	public static class userInfo {
		Socket socket;
		String ipAddress;
		String localAddress;
		Long loginTime;
		String request;
		DataInputStream inputStream;
		DataOutputStream outputStream;

		public userInfo(Socket soc) {
			socket = soc;
			loginTime = System.currentTimeMillis();
			try {
				inputStream = new DataInputStream(socket.getInputStream());
				outputStream = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static class Listener extends Thread {

		public Listener() {
		}

		public void run() {
			while (true) {
				try {
					connectionsList.add(new userInfo(serverSocket.accept()));
					System.out.println("New connection!");

					connectionsList.get(connectionsList.size()
							- 1).ipAddress = connectionsList.get(connectionsList.size() - 1).socket.getInetAddress()
									.toString();

					connectionsList.get(connectionsList.size()
							- 1).localAddress = connectionsList.get(connectionsList.size() - 1).socket.getLocalAddress()
									.toString();

					connectionsList.get(connectionsList.size() - 1).socket.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

}
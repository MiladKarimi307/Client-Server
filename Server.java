
import java.io.*;
import java.net.*;
import java.sql.Time;
import java.util.*;

public class Server {

	static ServerSocket serverSocket;
	static userInfo user;
	static Listener listener;
	static ArrayList<userInfo> connectionsList;

	public Server(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		connectionsList = new ArrayList<userInfo>();// list of users
		listener = new Listener();
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		new Server(2222);
		Long startTime = System.currentTimeMillis(); // server start up time.
		// listen for new connections
		listener.start();
		System.out.println("Server is listening...");

		// main while loop
		while (true) {

			synchronized (connectionsList) {

				if (connectionsList.isEmpty())
					connectionsList.wait();

				// let current user be the first on the list
				user = connectionsList.get(0);

				// setup input/output stream for user
				user.inputStream = user.inputStream;
				user.outputStream = user.outputStream;

				// move users with no request to the end of the list
				if (user.inputStream.available() == 0) {
					connectionsList.add(connectionsList.remove(0));
					continue;
				}
				// read user's input
				user.request = user.inputStream.readUTF();

				// process user's request and send reply
				switch (user.request) {
				case "1":
				case "host current date and time":
				case "current date and time":
				case "date and time":
				case "date":
				case "time":
					user.outputStream.writeUTF(currentDateTime());
					break;
				case "2":
				case "host uptime":
				case "uptime":
					user.outputStream.writeUTF(upTime(startTime));
					break;
				case "3":
				case "host memory use":
				case "memory use":
				case "memory":
					user.outputStream.writeUTF(memoryUse());
					break;
				case "4":
				case "host netstat":
				case "netstat":
					user.outputStream.writeUTF(runningProcesses("netstat"));
					break;
				case "5":
				case "host current users":
				case "current users":
				case "users":
					user.outputStream.writeUTF(currentUsers());
					break;
				case "6":
				case "host running processes":
				case "running processes":
				case "processes":
					if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
						user.outputStream.writeUTF(runningProcesses("tasklist"));
					else
						user.outputStream.writeUTF(runningProcesses("ps"));
					break;
				case "7":
				case "quit":
				case "exit":
					user.inputStream.close();
					user.outputStream.close();
					user.socket.close();
					connectionsList.remove(0);
					System.out.println(" A user left the server   @" + new Time(System.currentTimeMillis()).toString());
					break;
				default:
					user.outputStream.writeUTF("Not a valid entry");
					break;
				}

			}
		}

	}

	private static String currentDateTime() {
		return "  " + new Date().toString();
	}

	private static String upTime(Long serverStartTime) {
		return "  Server uptime: " + new Time(System.currentTimeMillis() - serverStartTime).toString().substring(3)
				+ "\n  Client uptime: " + new Time(System.currentTimeMillis() - user.loginTime).toString().substring(3);
	}

	private static String memoryUse() {
		Byte dp = 3;
		while (dp > 0) {
			try {
				return "   Free Memory\t  Memory in Use    Total Memory\n"
						+ "  -------------  ---------------  --------------\n    "
						+ String.valueOf(Runtime.getRuntime().freeMemory() / (1024L * 1024L)) + "."
						+ String.valueOf(Runtime.getRuntime().freeMemory() % (1024L * 1024L)).substring(0, dp)
						+ "MB\t    " +

						String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
								/ (1024L * 1024L))
						+ "." + String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
								% (1024L * 1024L)).substring(0, dp)
						+ "MB\t    " +

						String.valueOf(Runtime.getRuntime().totalMemory() / (1024L * 1024L)) + "."
						+ String.valueOf(Runtime.getRuntime().totalMemory() % (1024L * 1024L)).substring(0, dp) + "MB";

			} catch (StringIndexOutOfBoundsException siobe) {
				dp--;
				continue;
			}
		}
		return "   Free Memory\t  Memory in Use    Total Memory\n"
				+ "  -------------  ---------------  --------------\n    "
				+ String.valueOf(Runtime.getRuntime().freeMemory() / (1024L * 1024L)) + " MB\t    " +

				String.valueOf(
						(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L))
				+ " MB\t    " +

				String.valueOf(Runtime.getRuntime().totalMemory() / (1024L * 1024L)) + " MB";

	}

	@SuppressWarnings("static-access")
	private static String currentUsers() {
		String result = "";
		userInfo client;
		String userName;
		Integer userNameLength = 16;
		String ipAddress;
		String clientUpTime;
		String clientLoginTime;

		// iterate through the list and collect user info
		result = "  #       COMPUTER-NAME           IP-ADDRESS         LOGIN TIME     UPTIME\n"
			   + " ----  --------------------    -----------------    ------------  -----------\n";
		for (int i = 0; i < connectionsList.size(); i++) {
			try {
				client = connectionsList.get(i);
				userName = client.socket.getInetAddress().getLocalHost().toString().substring(0,
						client.socket.getInetAddress().getLocalHost().toString().lastIndexOf('/'));

				ipAddress = client.socket.getRemoteSocketAddress().toString().replace('/', ' ').trim();
				clientLoginTime = new Time(user.loginTime).toString();
				clientUpTime = new Time(System.currentTimeMillis() - user.loginTime).toString();

				try {
					// end user names that are too big with "..."
					userName = userName.substring(0, userNameLength) + "...";
				} catch (StringIndexOutOfBoundsException siobe) {
					for (int j = 0; j < userNameLength + 3 - userName.length() + 1; j++) {
						userName = userName + " ";
					}
				}
				for(int j = 0; j < (3 - String.valueOf(i+1).length()); j++)
				result += " ";
				result +=  " "+ (i+1) +"    "+ userName + "      " + ipAddress + "       " + clientLoginTime + "      "
						+ clientUpTime+"\n";
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private static String runningProcesses(String str) {
		String result = "";
		Process p;
		try {

			// execute the command
			p = Runtime.getRuntime().exec(str);

			// make the thread wait until the process is finished
			// if netstat is the command being processed
			if (str.contains("netstat"))
				p.waitFor();
			String temp;
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// collect the output
			while ((temp = br.readLine()) != null) {
				result += temp + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("the process was not able to complete.");
			e.printStackTrace();
		}
		return result;
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

	// accept new connections request and add to the list
	public static class Listener extends Thread {
		Integer count;
		Integer c;

		public Listener() {
			count = 0;
			c = 0;
		}

		public void run() {

			new Listener();

			// if many clients join the server at once inform with a single line
			Runnable counter = new Runnable() {
				public void run() {
					while (true) {
						synchronized (count) {
							try {
								while (count > 0) {
									c = count;
									Thread.sleep(300);
									if (count > c)
										continue;
									else
										break;
								}
								if (count > 1)
									System.out.println("[" + (count + 1) + "] Users joined the server @"
											+ new Time(System.currentTimeMillis()).toString());
								else if (count == 1) {
									System.out.println(" A user joined the server @"
											+ new Time(System.currentTimeMillis()).toString());
								} else
									System.out.print("");
								c = 0;
								count = 0;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}
					}
				}
			};
			new Thread(counter).start();
			while (true) {
				try {

					userInfo temp = new userInfo(serverSocket.accept());
					synchronized (count) {
						count++;

						synchronized (connectionsList) {
							connectionsList.notify();
							connectionsList.add(temp);

							connectionsList.get(connectionsList.size()
									- 1).ipAddress = connectionsList.get(connectionsList.size() - 1).socket
											.getInetAddress().toString();

							connectionsList.get(connectionsList.size()
									- 1).localAddress = connectionsList.get(connectionsList.size() - 1).socket
											.getLocalAddress().toString();

							connectionsList.get(connectionsList.size() - 1).socket.getInputStream();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

}

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

	protected int serverPort = 1234;
	protected static Map<String, Socket> clients = new HashMap<>(); // hashmap of client usernames and sockets

	public static void main(String[] args) throws Exception {
		new ChatServer();
	}

	public ChatServer() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(this.serverPort); // create the ServerSocket
			System.out.println("[system] listening ...");

			while (true) {
				Socket newClientSocket = serverSocket.accept(); // wait for a new client connection
				ChatServerConnector conn = new ChatServerConnector(this, newClientSocket); // create a new thread
				conn.start(); // run the new thread
			}
		} catch (Exception e) {
			System.err.println("[error] Accept failed.");
			e.printStackTrace(System.err);
			System.exit(1);
		} finally {
			System.out.println("[system] closing server socket ...");
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
	}

	// send a message to all clients connected to the server
	public void sendToAllClients(String message) throws Exception {
		for (Socket socket : clients.values()) { // iterate through the client list
			// get the socket for communicating with this client
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(message); // send message to the client
			} catch (Exception e) {
				System.err.println("[system] could not send message to a client");
				e.printStackTrace(System.err);
			}
		}
	}

	public void removeClient(Socket socket) {
		synchronized (this) {
			Iterator<Map.Entry<String, Socket>> iterator = clients.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Socket> entry = iterator.next();
				if (entry.getValue().equals(socket)) {
					System.out.println("[system] Removing client: " + entry.getKey());
					iterator.remove();
					break;
				}
			}
		}
	}
}

class ChatServerConnector extends Thread {
	private ChatServer server;
	private Socket socket;

	public ChatServerConnector(ChatServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		System.out
				.println("[system] connected with " + this.socket.getInetAddress().getHostName() + ":" + this.socket.getPort());

		DataInputStream in;
		DataOutputStream out;
		try {
			in = new DataInputStream(this.socket.getInputStream()); // create input stream for listening for incoming messages
		} catch (IOException e) {
			System.err.println("[system] could not open input stream!");
			e.printStackTrace(System.err);
			this.server.removeClient(socket);
			return;
		}

		try {
			out = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("[system] could not open output stream!");
			e.printStackTrace(System.err);
			this.server.removeClient(socket);
			return;
		}

		boolean isUsernameSet = false;
		String userName = null;

		while (true) { // infinite loop in which this thread waits for incoming messages and processes
										// them
			String msg_received;

			try {
				msg_received = in.readUTF(); // read the message from the client
			} catch (SocketException e) {
				System.err.println("[system] Connection reset by client on port " + this.socket.getPort());
				break; // Break the loop to handle the disconnection
			} catch (EOFException eof) {
				System.out.println("[system] Client disconnected on port " + this.socket.getPort());
				this.server.removeClient(this.socket);
				break;
			} catch (Exception e) {
				System.err.println("[system] there was a problem while reading message client on port " + this.socket.getPort()
						+ ", removing client");
				e.printStackTrace(System.err);
				this.server.removeClient(this.socket);
				return;
			}

			// first message is the username
			if (!isUsernameSet) {
				while (!isUsernameSet) {
					if (msg_received == null) {
						try {
							out.writeUTF("Invalid username.\nPlease enter a non-empty username:");
							msg_received = in.readUTF().trim();
						} catch (Exception e) {
							System.err.println("[system] there was a problem while reading message client on port "
									+ this.socket.getPort() + ", removing client");
							e.printStackTrace(System.err);
							this.server.removeClient(this.socket);
							return;
						}
					} else if (msg_received != null) {
						boolean userExists = ChatServer.clients.containsKey(msg_received);
						if (userExists) {
							Random random = new Random();
							int randomNumber = random.nextInt(1000);
							userName = "User" + randomNumber;
						} else if (!userExists) {
							userName = msg_received;
						}
						synchronized (server) {
							ChatServer.clients.put(userName.trim(), this.socket);
							if (userExists) {
								try {
									DataOutputStream usernameOut = new DataOutputStream(this.socket.getOutputStream());
									usernameOut.writeUTF("This user already exists, your new username is: " + userName);
								} catch (IOException e) {
									System.err.println("[system] Error sending new username to client on port " + this.socket.getPort());
									e.printStackTrace();
								}
							}
						}
						isUsernameSet = true;
						try {
							server.sendToAllClients("[system] " + userName + " has joined the chat.");
						} catch (Exception e) {
							System.err.println("[system] there was a problem while sending the message to all clients");
							e.printStackTrace();
						}
					}
				}
			} else if (isUsernameSet) {
				MessagePacket message = MessagePacket.jsonToJava(msg_received);

				String time = message.getTime();
				String type = message.getType();
				String sender = message.getSender();
				String receiver = message.getReceiver();
				String content = message.getContent();

				boolean sendToSender = false;
				if (sender.equals(receiver)) {
					sendToSender = true;
					Socket senderSocket = ChatServer.clients.get(sender);
					try {
						out = new DataOutputStream(senderSocket.getOutputStream()); // Assume 'senderSocket' is the Socket of the
																																				// sender
						out.writeUTF("Cannot send message to yourself aka. " + "[" + receiver + "]");
					} catch (IOException e) {
						System.err.println("[system] Error sending failure notification to " + receiver);
						e.printStackTrace();
					}
				}

				if (type.equals("1")) {
					receiver = "all";
				}
				String msg_send = time + " " + "[" + sender + "] " + "===>" + "[" + receiver + "]";

				// private message
				if (type.equals("2") && !sendToSender) {

					boolean userFound = false;

					for (Map.Entry<String, Socket> client : ChatServer.clients.entrySet()) {
						if (client.getKey().equals(receiver)) {
							try {
								out = new DataOutputStream(client.getValue().getOutputStream()); // create output stream for sending
																																									// messages to the client
								String msg = time + " " + "[" + sender + "]" + " : " + content;
								System.out.println(msg_send);
								out.writeUTF(msg);
							} catch (Exception e) {
								System.err.println("[system] could not send message to " + receiver);
								e.printStackTrace(System.err);
							}
							userFound = true;
						}
					}
					if (!userFound) {
						Socket senderSocket = ChatServer.clients.get(sender);
						try {
							out = new DataOutputStream(senderSocket.getOutputStream()); // Assume 'senderSocket' is the Socket of the
																																					// sender
							out.writeUTF("Message could not be sent. User '" + receiver + "' does not exist.");
						} catch (IOException e) {
							System.err.println("[system] Error sending failure notification to " + receiver);
							e.printStackTrace();
						}
					}

					// public message
				} else if (type.equals("1")) {
					try {
						String msg_all = time + " " + "[" + sender + "]" + " : " + content;
						this.server.sendToAllClients(msg_all); // send message to all clients
						System.out.println(msg_send);
					} catch (Exception e) {
						System.err.println("[system] there was a problem while sending the message to all clients");
						e.printStackTrace(System.err);
						continue;
					}
				}
			}
		}
	}
}

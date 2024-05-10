import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ChatClient extends Thread {
	protected int serverPort = 1234;
	private String userName;

	public static void main(String[] args) throws Exception {
		new ChatClient();
	}

	public ChatClient() throws Exception {
		Socket socket = null;
		DataInputStream in = null;
		DataOutputStream out = null;

		// connect to the chat server
		try {
			System.out.println("[system] connecting to chat server ...");
			socket = new Socket("localhost", serverPort); // create socket connection
			in = new DataInputStream(socket.getInputStream()); // create input stream for listening for incoming messages
			out = new DataOutputStream(socket.getOutputStream()); // create output stream for sending messages
			System.out.println("[system] connected");

			// Enter username
			BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter your username: ");
			userName = std_in.readLine();
			out.writeUTF(userName);
			out.flush();

			String userNameResponse = in.readUTF();
			if (userNameResponse.startsWith("This user already exists")) {
				userName = userNameResponse.split(": ")[1].trim(); // Update local username with the new one from the server
				System.out.println(userNameResponse);
			}

			ChatClientMessageReceiver message_receiver = new ChatClientMessageReceiver(in); // create a separate thread for
																																											// listening to messages from the
																																											// chat server
			message_receiver.start(); // run the new thread
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// read from STDIN and send messages to the chat server
		BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		while ((userInput = std_in.readLine()) != null) { // read a line from the console
			this.sendMessage(userInput, out); // send the message to the chat server
		}

		// cleanup
		try {
			out.close();
			in.close();
			std_in.close();
			socket.close();
			System.out.println("Client shutdown succesfully.");
		} catch (IOException e) {
			System.out.println("Error closing client.");
		}
	}

	private void sendMessage(String userInput, DataOutputStream out) throws IOException {

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		String time = now.format(formatter);
		System.out.println("Enter 1 for 'public' && 2 for 'private': ");

		BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
		String type = std_in.readLine();

		if (type.equalsIgnoreCase("1")) {
			String reciever = "all";
			try {
				String message = MessageType.PublicMessage(time, type, userName, reciever, userInput);
				out.writeUTF(message); // send the message to the chat server
				out.flush(); // ensure the message has been sent
			} catch (IOException e) {
				System.err.println("[system] could not send message");
				e.printStackTrace(System.err);
			}
		} else if (type.equalsIgnoreCase("2")) {

			System.out.println("Enter recipient username: ");
			String reciever = std_in.readLine();
			try {
				String message = MessageType.PrivateMessage(time, type, userName, reciever, userInput);
				out.writeUTF(message); // send the message to the chat server
				out.flush(); // ensure the message has been sent
			} catch (IOException e) {
				System.err.println("[system] could not send message");
				e.printStackTrace(System.err);
			}
		}
	}
}

// wait for messages from the chat server and print the out
class ChatClientMessageReceiver extends Thread {
	private DataInputStream in;

	public ChatClientMessageReceiver(DataInputStream in) {
		this.in = in;
	}

	public void run() {
		try {
			String message;
			while ((message = this.in.readUTF()) != null) { // read new message
				System.out.println(message); // print the message to the console
			}
		} catch (Exception e) {
			System.err.println("[system] could not read message");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}

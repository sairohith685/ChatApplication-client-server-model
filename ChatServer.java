// File: ChatServer.java

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1234; // Server port number
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);

            while (true) {
                // Accept new client connections
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                // Create a new handler for each client
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);

                // Start the client's thread
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error in ChatServer: " + e.getMessage());
        } finally {
            // Close the server socket when done
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Cannot close server socket: " + e.getMessage());
                }
            }
        }
    }

    // Broadcast message to all clients
    public static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeUser) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // Remove a client from the list
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("A client disconnected.");
    }
}

// Handler class for client threads
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Set up I/O streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Get username
            out.println("Enter your username:");
            userName = in.readLine();
            System.out.println(userName + " has joined.");

            // Notify all clients about the new user
            ChatServer.broadcast(userName + " has joined the chat.", this);

            String message;
            // Read messages from the client and broadcast them
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }
                String formattedMessage = userName + ": " + message;
                System.out.println(formattedMessage);
                ChatServer.broadcast(formattedMessage, this);
            }
        } catch (IOException e) {
            System.err.println("Error in ClientHandler: " + e.getMessage());
        } finally {
            // Clean up resources
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Cannot close socket: " + e.getMessage());
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(userName + " has left the chat.", this);
        }
    }

    // Send message to the client
    public void sendMessage(String message) {
        out.println(message);
    }
}

// File: ChatClient.java

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            // Connect to the server
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the chat server.");

            // Start a new thread for reading messages from the server
            new ReadThread(socket, this).start();
            // Main thread will write messages to the server
            new WriteThread(socket, this).start();

        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }

    // Setters and getters
    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }

    public static void main(String[] args) {
        String hostname = "localhost"; // Server hostname or IP
        int port = 1234; // Server port number

        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
}

// Thread for reading messages from the server
class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            // Initialize the reader
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error getting input stream: " + e.getMessage());
        }
    }

    public void run() {
        while (true) {
            try {
                // Read messages from the server
                String response = reader.readLine();
                System.out.println("\n" + response);

                // Print the prompt
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
                break;
            }
        }
    }
}

// Thread for writing messages to the server
class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private Scanner scanner;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;
        scanner = new Scanner(System.in);

        try {
            // Initialize the writer
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error getting output stream: " + e.getMessage());
        }
    }

    public void run() {
        System.out.print("Enter your username: ");
        String userName = scanner.nextLine();
        client.setUserName(userName);
        writer.println(userName);

        String text;

        do {
            System.out.print("[" + userName + "]: ");
            text = scanner.nextLine();
            writer.println(text);

        } while (!text.equalsIgnoreCase("/exit"));

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error writing to server: " + e.getMessage());
        }
    }
}

package com.pcadproject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String args[]){
        int port = 8818;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("ServerSocket creato! '\n In ascolto...");
             while (true) {
                 System.out.println("In procinto di accettare una connessione da un client...\n");
                 Socket clientSocket = serverSocket.accept();
                 System.out.println("Connessione accettata da "+ clientSocket);
                 ServerWorker worker = new ServerWorker(clientSocket);
                 worker.start();
             }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

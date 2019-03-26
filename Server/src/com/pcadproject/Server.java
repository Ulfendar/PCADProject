package com.pcadproject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverport;

    private ArrayList<ServerWorker> serverWorkersList = new ArrayList<>();

    public Server(int serverport) {
        this.serverport = serverport;
    }

    public List<ServerWorker> getServerWorkersList() {
        return serverWorkersList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverport);
            System.out.println("ServerSocket creato! '\n In ascolto...");
            while (true) {
                System.out.println("In procinto di accettare una connessione da un client...\n");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da "+ clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                serverWorkersList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseWorker(ServerWorker worker){

        serverWorkersList.remove(worker);
    }
}

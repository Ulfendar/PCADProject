package com.pcadproject;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class ServerWorker extends Thread {
    private Socket clientSocket;
    private Server server;//contiene la lista degli altri ServerWorkers
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.outputStream = clientSocket.getOutputStream();
    }

    public String getLogin() {
        return login;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        outputStream.write("Connesso!\n".getBytes());

        String line;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);

                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    
                    if ("quit".equalsIgnoreCase(cmd)) {
                        handleLogOut();
                        break;
                    } else if ("login".equalsIgnoreCase(cmd)) handleLogin(outputStream, tokens);
                    else if ("msg".equalsIgnoreCase(cmd)) handleMessages(outputStream, tokens);
                    else {
                        String msg = "Comando non riconosciuto: " + cmd + " \n";
                        send(msg);
                    }

                }

            }
            clientSocket.close();
        }
    }

    private void handleLogOut() throws IOException {
        send("Arrivederci");
        String onlineNotification = "L'utente " + login +" e' ora offline!\n";
        broadcast(onlineNotification);
        server.releaseWorker(this);
        return;
    }

    private void handleMessages(OutputStream outputStream, String[] tokens) throws IOException {
        String msg = null;
        if (tokens.length == 2)
            msg = "Messaggio: " + tokens[1] + "\n";
        else
            msg = "Errore di sintassi \n      Prova -> [msg CONTENUTO_MSG]";
        outputStream.write(msg.getBytes());
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {

        if (tokens.length == 3) {

            String login = tokens[1];
            String password = tokens[2];
            String msg;
            if(login.equals("guest") && password.equals("guest")
                    || login.equals("jim") && password.equals("jim")
                    || login.equals("jack") && password.equals("jack")) {
                msg = "Login effettuato\n";
                this.login = login;
                System.out.println("login fatto");
                String onlineNotification = "L'utente " + login +" e' ora online!\n";

                getUsersStatus();

                broadcast(onlineNotification);
            }
            else
                msg = "Errore nel login\n";

            outputStream.write(msg.getBytes());
        }
    }

    private void getUsersStatus() throws IOException {

        String onlineNotification = null;
        for(ServerWorker worker: server.getServerWorkersList()) {
            if(worker.getLogin()!= null
                    &&(!login.equals(worker.getLogin()))){
                onlineNotification = "L'utente " + worker.getLogin() +" e' ora online!\n";
                send(onlineNotification);
            }
        }
    }

    private void broadcast(String msg) throws IOException {

        for(ServerWorker worker: server.getServerWorkersList()) {
            worker.send(msg);
        }

    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

}

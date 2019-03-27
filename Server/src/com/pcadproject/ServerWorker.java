package com.pcadproject;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {
    private Socket clientSocket;
    private Server server;//contiene la lista degli altri ServerWorkers
    private String login = null;
    private OutputStream outputStream;
    HashSet<String> topicSet;

    public ServerWorker(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.outputStream = clientSocket.getOutputStream();
        this.topicSet = new HashSet<>();
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
                String[] tokens = StringUtils.split(line,null, 3);

                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    
                    if ("quit".equalsIgnoreCase(cmd)) {
                        handleLogOut();
                        break;
                    }
                    else if ("login".equalsIgnoreCase(cmd)) handleLogin(tokens);
                    else if ("msg".equalsIgnoreCase(cmd)) handleMessages(tokens);
                    else if ("join".equalsIgnoreCase(cmd)) handleJoin(tokens);
                    else if ("leave".equalsIgnoreCase(cmd)) handleLeave(tokens);
                    else {
                        String msg = "Comando non riconosciuto: " + cmd + " \n";
                        send(msg);
                    }

                }

            }
            clientSocket.close();
        }
    }

    private void handleLeave(String[] tokens) throws IOException {
        if (tokens.length >1){
            String topic = tokens[1];
            topicSet.remove(topic);
            send("Uscito da "+topic+"\n");
        }
    }

    private boolean isMemberOfTopic(String topic) {

        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) throws IOException {
        if (tokens.length >1){
            String topic = tokens[1];
            topicSet.add(topic);
            send(topic+" Joinato\n");
        }
    }

    private void handleLogOut() throws IOException {
        send("Arrivederci\n");
        String offlineNotification = "L'utente " + login +" e' ora offline!\n";
        broadcast(offlineNotification);
        server.releaseWorker(this);
        return;
    }

    private void handleMessages(String[] tokens) throws IOException {
        String msg= "";
        if ((tokens.length < 3 || tokens[1].equals(null))){
            msg = "Errore di sintassi \n      Prova -> [msg USERNAME CONTENUTO_MSG]\n";
            send(msg);
            return;
        }
        String sendTo = tokens[1];

        boolean isTopic = (sendTo.charAt(0) == '@');//starting character for a group
        boolean isFound = false;

        if(isTopic) msg += ""+ sendTo+":";

        for(ServerWorker worker: server.getServerWorkersList()) {
            if(isTopic){ //if sendTo is a groupChat...
                if(worker.isMemberOfTopic(sendTo)) {
                    isFound = true;
                    deliverMessage(tokens[2],msg,worker);
                }
            }
            else if(sendTo.equals(worker.getLogin())) {//if user found
                isFound =true;
                deliverMessage(tokens[2], msg, worker);
            }

        }
        if(isFound == false) {
            msg = "Utente o Gruppo non trovato!\n";
            send(msg);
        }

    }

    private void deliverMessage(String token, String msg, ServerWorker worker) throws IOException {
        msg += token;
        worker.send(" " + login + ":\n   " + msg + "\n");
    }

    private void handleLogin(String[] tokens) throws IOException {

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
            if(!login.equals(worker.getLogin())) worker.send(msg);
        }

    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }

}

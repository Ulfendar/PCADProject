package com.pcadproject;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private String login =null;

    public ServerWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
        OutputStream outputStream = clientSocket.getOutputStream();
        outputStream.write("Connesso!\n".getBytes());

        String line,msg;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("quit".equalsIgnoreCase(cmd)) break;
                    else if ("login".equalsIgnoreCase(cmd)) handleLogin(outputStream, tokens);
                    else if ("msg".equalsIgnoreCase(cmd)) handleMessages(outputStream, tokens);
                    else {
                        msg = "Comando non riconosciuto: " + cmd + " \n";
                        outputStream.write(msg.getBytes());
                    }

                }

            }


            clientSocket.close();
        }
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
            if(login.equals("guest")&&password.equals("guest")) {
                msg = "Login effettuato\n";
                this.login = login;
                System.out.println("login fatto");
            }
            else
                msg = "Errore nel login\n";

            outputStream.write(msg.getBytes());
        }
    }

}

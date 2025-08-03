package com.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatApp extends Application {

    private static Scene scene;
    private boolean isServer = false;
    private TextArea message = new TextArea();
    private NetworkConnection connection = isServer ? createServer() : createClient();
    private ExecutorService networkExecutor;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(createContent());
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void init() throws Exception {
        networkExecutor = Executors.newSingleThreadExecutor();
        networkExecutor.submit(connection);
        
    }
    @Override
    public void stop() throws Exception {
        connection.shutDown();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdownNow(); // Đảm bảo đóng ExecutorService
        }
    }
    public Parent createContent(){
        TextField input = new TextField();
        message.setPrefHeight(550);
        message.setEditable(false);
        VBox root = new VBox(20, message,input);
        root.setPrefSize(600, 600);
        input.setOnAction(event ->{
            String messageString = isServer ? "Server: " : "Client: ";
            messageString+= input.getText();
            input.clear();
            if(isServer)
            message.appendText(messageString+"\n");
            try{
            connection.send(messageString);
            }catch(Exception e){

            }
        });
        return root;
    }
    public Server createServer(){
        return new Server(data ->{
            Platform.runLater(() ->{
                message.appendText(data.toString()+"\n");
            });
            
        });
    }
    public Client createClient(){
        return new Client(data ->{
            Platform.runLater(() ->{
                message.appendText(data.toString()+"\n");
            });
            
        });
    }
    public static void main(String[] args) {
        launch(args);
    }

}
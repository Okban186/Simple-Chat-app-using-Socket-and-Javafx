package com.chat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class Server implements Runnable, NetworkConnection{
   private ServerSocket server;
   private ExecutorService pools = Executors.newCachedThreadPool(new customeThreadFactory());
   private List<ConnectionHandle> handleConnection;
   private Consumer<Serializable> onReCallBack;
   private boolean isDone = false;
    public Server(Consumer<Serializable> onReCallBack){
        this.onReCallBack = onReCallBack;
    }
   @Override
   public void run(){
    try{
        handleConnection = new ArrayList<>();
        server = new ServerSocket(55555);
        while(!isDone){
        Socket socket = server.accept();
        ConnectionHandle handleSocket = new ConnectionHandle(socket);
        handleConnection.add(handleSocket);
        pools.submit(handleSocket);
        }
    }catch(Exception e){
        shutDown();
    }
   }
   public void shutDown(){
    try{
        if(!isDone){
        server.close();
        pools.shutdown();
        isDone = true;
        Thread.currentThread().interrupt();
        for (ConnectionHandle connectionHandle : handleConnection) {
            connectionHandle.shutDown();
        }
    }
    }catch(Exception e){

    }
   }
   public void send(Serializable data) throws Exception{
        for (ConnectionHandle connectionHandle : handleConnection) {
            if(connectionHandle == null) continue;
            connectionHandle.sendt(data);
        }
   }
   public class customeThreadFactory implements ThreadFactory{
    private final ThreadFactory threadFactory = Executors.defaultThreadFactory();
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = threadFactory.newThread(r);
        thread.setDaemon(true);
        return thread;
    }
    }
    public class ConnectionHandle implements Runnable{
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean done = false;
        public ConnectionHandle(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
          try{
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            while(!done){
                Serializable data = (Serializable) in.readObject();
                onReCallBack.accept(data);
                send(data);
            }
          }catch(Exception e){

          }
        }
        public void sendt(Serializable data) throws Exception{
            out.writeObject(data);
            out.flush();
        }
        public void shutDown(){
            if(!done){
                try{
                    done = true;
                    socket.close();;
                    out.close();
                    in.close();
                }catch(Exception e){

                }
            }
        }

    }
}

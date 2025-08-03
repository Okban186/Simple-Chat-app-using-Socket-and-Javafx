package com.chat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client implements Runnable, NetworkConnection{
    private ObjectInputStream input;
    private ObjectOutputStream out;
    private Socket socket;
    private boolean done = false;
    private Consumer<Serializable> onReCallBack;

    public Client(Consumer<Serializable> onReCallBack){
        this.onReCallBack = onReCallBack;
    }

    @Override
    public void run() {
        try{
            socket = new Socket("localhost", 55555);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // <<< THÊM DÒNG NÀY <<< Rất quan trọng cho ObjectOutputStream
            input = new ObjectInputStream(socket.getInputStream());

            while(!done){
                Serializable data = (Serializable) input.readObject();
                onReCallBack.accept(data);
            }
        }catch(Exception e){
            // Xử lý lỗi kết nối hoặc đọc/ghi
            onReCallBack.accept("Client disconnected: " + e.getMessage()); // Thông báo lỗi ra UI
            shutDown(); // Đóng kết nối khi có lỗi
        }
    }

    public void shutDown(){
        if(!done){
            try{
                done = true; // Đặt cờ done trước khi đóng tài nguyên
                socket.close();
                input.close();
                out.close();
            }catch(Exception e){
                // Xử lý lỗi khi đóng
            }
        }
    }

    @Override // Ghi đè phương thức send từ NetworkConnection
    public void send(Serializable data) throws Exception{
        if(out != null){ // Đảm bảo out stream đã được khởi tạo
            out.writeObject(data);
            out.flush(); // <<< THÊM DÒNG NÀY <<< Quan trọng để đảm bảo dữ liệu được gửi đi ngay
        } else {
            throw new IllegalStateException("Output stream not initialized.");
        }
    }
}
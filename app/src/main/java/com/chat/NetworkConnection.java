package com.chat;

import java.io.Serializable;

public interface NetworkConnection extends Runnable {
    public void shutDown();
    public void send(Serializable data) throws Exception;
}

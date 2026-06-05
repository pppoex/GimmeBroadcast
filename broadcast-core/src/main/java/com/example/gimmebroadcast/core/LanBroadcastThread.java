package com.example.gimmebroadcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanBroadcastThread extends Thread {

    public static final String MULTICAST_ADDRESS = "224.0.2.60";
    public static final int    MULTICAST_PORT    = 4445;
    private static final long BROADCAST_INTERVAL_MS = 1500L;

    private final byte[]        pingData;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket     socket;
    private InetAddress        group;

    public LanBroadcastThread(String motd, int port) {
        super("LAN-Broadcast-Thread");
        setDaemon(true);
        this.pingData = ("[MOTD]" + motd + "[/MOTD][AD]" + port + "[/AD]")
                .getBytes(StandardCharsets.UTF_8);
    }

    public synchronized void startBroadcast() {
        if (running.get()) return;
        running.set(true);
        start();
    }

    public void stopBroadcast() {
        running.set(false);
        interrupt();
        if (socket != null && !socket.isClosed()) socket.close();
    }

    public boolean isBroadcasting() { return running.get(); }

    @Override
    public void run() {
        try {
            group  = InetAddress.getByName(MULTICAST_ADDRESS);
            socket = new DatagramSocket();

            while (running.get() && !isInterrupted()) {
                try {
                    socket.send(new DatagramPacket(pingData, pingData.length, group, MULTICAST_PORT));
                } catch (IOException e) {
                    System.err.println("[LanBroadcast] " + e.getMessage());
                }
                Thread.sleep(BROADCAST_INTERVAL_MS);
            }
        } catch (SocketException e) { /* normal shutdown */ }
          catch (InterruptedException e) { interrupt(); }
          catch (IOException e) { System.err.println("[LanBroadcast] " + e.getMessage()); }
        finally {
            running.set(false);
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }
}

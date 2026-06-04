package com.example.gimmebroadcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanBroadcastThread extends Thread {

    public static final String MULTICAST_ADDRESS = "224.0.2.60";
    public static final int    MULTICAST_PORT    = 4445;
    private static final long BROADCAST_INTERVAL_MS = 1500L;

    private final byte[]       pingData;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket     socket;
    private InetAddress        group;

    public LanBroadcastThread(String motd, String host, int port) {
        super("LAN-Broadcast-Thread");
        setDaemon(true);

        String addr = (host == null || host.isEmpty() || "0.0.0.0".equals(host))
                ? resolveLocalAddress() : host;
        this.pingData = ("[MOTD]" + motd + "[/MOTD][AD]" + addr + ":" + port + "[/AD]")
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
                socket.send(new DatagramPacket(pingData, pingData.length, group, MULTICAST_PORT));
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

    private static String resolveLocalAddress() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return "0.0.0.0";
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address) return addr.getHostAddress();
                }
            }
        } catch (SocketException ignored) {}
        return "0.0.0.0";
    }
}

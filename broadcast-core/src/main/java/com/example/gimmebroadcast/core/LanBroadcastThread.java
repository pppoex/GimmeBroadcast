package com.example.gimmebroadcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends a UDP broadcast to each subnet's broadcast address on port 4445.
 * Vanilla Minecraft clients listen on this port for LAN server discovery.
 */
public class LanBroadcastThread extends Thread {

    public static final int BROADCAST_PORT = 4445;
    private static final long BROADCAST_INTERVAL_MS = 1500L;

    private final byte[]        pingData;
    private final List<InetAddress> broadcastAddresses;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket     socket;

    public LanBroadcastThread(String motd, int port) {
        super("LAN-Broadcast-Thread");
        setDaemon(true);
        this.pingData = ("[MOTD]" + motd + "[/MOTD][AD]" + port + "[/AD]")
                .getBytes(StandardCharsets.UTF_8);
        this.broadcastAddresses = resolveBroadcastAddresses();
    }

    public List<String> getBroadcastAddresses() {
        List<String> result = new ArrayList<>();
        for (InetAddress a : broadcastAddresses) result.add(a.getHostAddress());
        return result;
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
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            while (running.get() && !isInterrupted()) {
                for (InetAddress broadcast : broadcastAddresses) {
                    try {
                        socket.send(new DatagramPacket(pingData, pingData.length, broadcast, BROADCAST_PORT));
                    } catch (IOException ignored) {
                        // Address unreachable — skip
                    }
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

    /** Returns the subnet broadcast address for every non-loopback, up, IPv4 interface. */
    private static List<InetAddress> resolveBroadcastAddresses() {
        List<InetAddress> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return result;

            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                for (InterfaceAddress ifAddr : iface.getInterfaceAddresses()) {
                    InetAddress broadcast = ifAddr.getBroadcast();
                    if (broadcast != null && ifAddr.getAddress() instanceof java.net.Inet4Address) {
                        result.add(broadcast);
                    }
                }
            }
        } catch (SocketException ignored) {}
        return result;
    }
}

package com.example.gimmebroadcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pure-Java UDP thread that broadcasts a Minecraft LAN server advertisement.
 * Zero dependency on Forge, NeoForge, or any Minecraft runtime class.
 *
 * <p>Sends one packet per available IP address every cycle, so clients on
 * different subnets (LAN, VPN, etc.) all see the server on their own subnet's
 * address.
 */
public class LanBroadcastThread extends Thread {

    public static final String MULTICAST_ADDRESS = "224.0.2.60";
    public static final int    MULTICAST_PORT    = 4445;
    private static final long BROADCAST_INTERVAL_MS = 1500L;

    private final String       motd;
    private final int          port;
    private final List<String> addresses;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket     socket;
    private InetAddress        group;

    /**
     * @param motd  Server MOTD displayed in the LAN tab.
     * @param hosts Addresses to broadcast, or {@code null} to auto-detect.
     * @param port  Server port (typically 25565).
     */
    public LanBroadcastThread(String motd, List<String> hosts, int port) {
        super("LAN-Broadcast-Thread");
        setDaemon(true);
        this.motd = motd;
        this.port = port;
        this.addresses = (hosts == null || hosts.isEmpty())
                ? resolveAllAddresses()
                : hosts;
    }

    /** Convenience constructor: broadcast on all detected non-loopback IPv4 addresses. */
    public LanBroadcastThread(String motd, int port) {
        this(motd, null, port);
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
    public List<String> getAddresses() { return addresses; }

    @Override
    public void run() {
        try {
            group  = InetAddress.getByName(MULTICAST_ADDRESS);
            socket = new DatagramSocket();

            while (running.get() && !isInterrupted()) {
                for (String addr : addresses) {
                    try {
                        byte[] data = buildPacket(addr);
                        socket.send(new DatagramPacket(data, data.length, group, MULTICAST_PORT));
                    } catch (IOException ignored) {
                        // Address unreachable / no route — skip silently
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

    private byte[] buildPacket(String addr) {
        return ("[MOTD]" + motd + "[/MOTD][AD]" + addr + ":" + port + "[/AD]")
                .getBytes(StandardCharsets.UTF_8);
    }

    /** Returns all non-loopback, up, IPv4 addresses on this machine. */
    public static List<String> resolveAllAddresses() {
        List<String> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return result;

            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        result.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException ignored) {}
        return result;
    }
}

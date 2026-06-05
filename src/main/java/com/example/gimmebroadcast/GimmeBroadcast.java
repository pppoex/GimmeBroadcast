/* GimmeBroadcast — Forge edition (MC 1.21.x).
 *
 * Restores the UDP multicast LAN advertisement (224.0.2.60:4445) that
 * Forge disables by default.
 *
 * Dedicated server: starts broadcasting immediately on ServerStartedEvent.
 * Singleplayer "Open to LAN": starts when isPublished() becomes true,
 * detected via ServerTickEvent.
 */

package com.example.gimmebroadcast;

import com.example.gimmebroadcast.core.LanBroadcastThread;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GimmeBroadcast.MOD_ID)
public class GimmeBroadcast {

    public static final String MOD_ID = "gimmebroadcast";
    private static final Logger LOG = LogUtils.getLogger();

    private volatile MinecraftServer server;
    private volatile LanBroadcastThread broadcastThread;

    public GimmeBroadcast() {
        MinecraftForge.EVENT_BUS.register(this);
        LOG.info("{} registered", MOD_ID);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        this.server = event.getServer();
        LOG.info("Server started – dedicated={}, published={}, port={}",
                server.isDedicatedServer(), server.isPublished(), server.getPort());
        if (server.isDedicatedServer() || server.isPublished()) startBroadcast(server);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (server == null) return;
        if (server.isPublished() && broadcastThread == null) startBroadcast(server);
        if (broadcastThread != null && !server.isPublished() && !server.isDedicatedServer()) stopBroadcast();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        stopBroadcast();
        this.server = null;
    }

    private void startBroadcast(MinecraftServer srv) {
        if (broadcastThread != null) { broadcastThread.stopBroadcast(); broadcastThread = null; }
        broadcastThread = new LanBroadcastThread(srv.getMotd(), srv.getPort());
        broadcastThread.startBroadcast();
        LOG.info("Broadcasting on {}:{}", String.join(", ", broadcastThread.getAddresses()), srv.getPort());
    }

    private void stopBroadcast() {
        if (broadcastThread != null) { broadcastThread.stopBroadcast(); broadcastThread = null; }
    }
}

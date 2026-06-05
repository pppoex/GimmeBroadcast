/* GimmeBroadcast — NeoForge edition (MC 1.21.x).
 *
 * Restores the UDP multicast LAN advertisement (224.0.2.60:4445) that
 * NeoForge disables by default.
 *
 * Dedicated server: starts broadcasting immediately on ServerStartedEvent.
 * Singleplayer "Open to LAN": starts when isPublished() becomes true,
 * detected via ServerTickEvent.
 */

package com.example.gimmebroadcast;

import com.example.gimmebroadcast.core.LanBroadcastThread;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(GimmeBroadcast.MOD_ID)
public class GimmeBroadcast {

    public static final String MOD_ID = "gimmebroadcast";
    private static final Logger LOG = LogUtils.getLogger();

    private volatile MinecraftServer server;
    private volatile LanBroadcastThread broadcastThread;

    public GimmeBroadcast() {
        NeoForge.EVENT_BUS.register(this);
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
    public void onServerTick(ServerTickEvent.Post event) {
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
        LOG.info("Broadcasting on {}", String.join(", ", broadcastThread.getBroadcastAddresses()));
    }

    private void stopBroadcast() {
        if (broadcastThread != null) { broadcastThread.stopBroadcast(); broadcastThread = null; }
    }
}

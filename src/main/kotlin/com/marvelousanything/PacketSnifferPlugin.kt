package com.marvelousanything

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.MessageToMessageDecoder
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Gauge
import io.prometheus.metrics.exporter.httpserver.HTTPServer
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class PacketSnifferPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("PacketSnifferEnabled")
        JvmMetrics.builder().register()
        packetCounter = Counter.builder().name("minecraft_packets").help("minecraft packets").labelNames("packets").register()
        onlinePlayerGauge = Gauge.builder().name("minecraft_players").help("minecraft players").labelNames("player_count").register()

        try {
            httpServer = HTTPServer.builder().port(9940).buildAndStart()
            logger.info("HTTPServer started on port 9940")
        } catch (e: Exception) {
            logger.warning("HTTPServer failed to start ${e.message}")
        }

        server.pluginManager.registerEvents(PlayerListener(), this)
        SnifferDecoder()
    }

    override fun onDisable() {
        httpServer.stop()
    }

    companion object {
        var players: MutableList<Player> = ArrayList()
        lateinit var packetCounter: Counter
        lateinit var onlinePlayerGauge: Gauge
        lateinit var httpServer: HTTPServer

        fun registerPlayer(player: Player) {
            val connection = (player as CraftPlayer).handle.connection
            val channel = (connection.javaClass.getField("h").get(connection) as Connection).channel
            val pipeline = channel.pipeline()
            pipeline.addBefore("packet_handler", player.getName(), SnifferDecoder())
        }
    }
}

class SnifferDecoder : MessageToMessageDecoder<Packet<*>>() {
    override fun decode(ctx: ChannelHandlerContext?, packet: Packet<*>?, out: MutableList<Any>?) {
        if (packet != null) {
            out?.add(packet)
        }
        PacketSnifferPlugin.packetCounter.labelValues("${packet!!::class.simpleName}").inc()
    }
}

class PlayerListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        PacketSnifferPlugin.registerPlayer(event.player)
        PacketSnifferPlugin.onlinePlayerGauge.labelValues("player_count").inc()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        PacketSnifferPlugin.players.remove(event.player)
        PacketSnifferPlugin.onlinePlayerGauge.labelValues("player_count").dec()
    }
}
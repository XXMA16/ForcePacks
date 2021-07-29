package com.xxma.forcepacks;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


@Plugin(
        id = "forcepacks",
        name = "ForcePacks",
        version = "@version@",
        authors = {"XXMA"}
)

public class ForcePacks {

    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private Toml config;

    @Inject
    public ForcePacks(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        if (!loadConfig()) {
            server.getEventManager().unregisterListeners(this);
            logger.error("Config is not loaded. Plugin will be inactive");
        }
    }

    @Subscribe
    public void onPlayerJoin(PlayerResourcePackStatusEvent e) {

//      failed
        if (e.getStatus().toString().equals("FAILED_DOWNLOAD") && config.getTable("console").getBoolean("failed")) {
            logger.warn(e.getPlayer().getUsername() + " failed to download the resourcepack");
        }

//      declined
        if (e.getStatus().toString().equals("DECLINED")) {
            if (e.getPlayer().hasPermission("forcepacks.bypass")) {
                if (config.getTable("console").getBoolean("bypass")) {
                    logger.info(e.getPlayer().getUsername() + " has bypassed the need to download the pack");
                }
                return;
            }
//          kicks
            if (config.getTable("console").getBoolean("kicks")) {
                logger.info(e.getPlayer().getUsername() + " declined downloading the resourcepack and was kicked");
            }
            e.getPlayer().disconnect(
                    MiniMessage.get().parse(config.getTable("messages").getString("kick"))
            );
        }

//      success
        if (e.getStatus().toString().equals("SUCCESSFUL") && config.getTable("console").getBoolean("success")) {
            logger.info(e.getPlayer().getUsername() + " has successfully applied the resourcepack");
        }
    }

    private boolean loadConfig() {
        File config = new File(dataDirectory.toFile(), "config.toml");
        try {
            if (!config.exists()) {
                dataDirectory.toFile().mkdir();
                if (!config.exists()) {
                    try (InputStream in = ForcePacks.class.getClassLoader().getResourceAsStream("config.toml")) {
                        Files.copy(in, config.toPath());
                    }
                }
            }
            this.config = new Toml().read(config);
            if (this.config.getTable("console").getBoolean("kicks")) {
                logger.info("Enabled");
            }

        } catch (IOException ex) {
            logger.error("Could not load or save config", ex);
            return false;
        }
        return true;
    }
}

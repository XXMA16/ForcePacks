package com.xxma.forcepacks;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;


@Plugin(
        id = "forcepacks",
        name = "ForcePacks",
        version = "@version@",
        authors = {"XXMA"}
)

public class ForcePacks {

    private final Logger logger;

    @Inject
    public ForcePacks(Logger logger) {
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }

    @Subscribe
    public void onPlayerJoin(PlayerResourcePackStatusEvent e) {
        if (e.getStatus().toString().equals("DECLINED")) {
            if (e.getPlayer().hasPermission("bypass")) {
                logger.info(e.getPlayer().getUsername() + " has bypassed the need to download the pack");
                return;
            }
            e.getPlayer().disconnect(Component.text("You need to accept the server resourcepacks in order to play")
                    .color(NamedTextColor.DARK_RED));
        }
    }
}

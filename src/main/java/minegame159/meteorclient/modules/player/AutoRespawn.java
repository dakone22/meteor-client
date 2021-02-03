/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.game.OpenScreenEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.resource.language.I18n;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super(Category.Player, "auto-respawn", I18n.translate("Module.AutoRespawn.description"));
    }

    @EventHandler
    private final Listener<OpenScreenEvent> onOpenScreenEvent = new Listener<>(event -> {
        if (!(event.screen instanceof DeathScreen)) return;

        mc.player.requestRespawn();
        event.cancel();
    });
}

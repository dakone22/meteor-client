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
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;

public class NoInteract extends Module {

    public NoInteract() {
        super(Category.Player, "no-interact", I18n.translate("Modules.NoInteract.description"));
    }

    @EventHandler
    private final Listener<OpenScreenEvent> onScreenOpen = new Listener<>(event -> {
        if (event.screen == null) return;
        if (!event.screen.isPauseScreen() && !(event.screen instanceof AbstractInventoryScreen) && (event.screen instanceof HandledScreen)) event.setCancelled(true);
    });
}

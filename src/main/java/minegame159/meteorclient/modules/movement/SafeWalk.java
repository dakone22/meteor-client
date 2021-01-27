/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.player.ClipAtLedgeEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class SafeWalk extends Module {
    public SafeWalk() {
        super(Category.Movement, "safe-walk", I18n.translate("Modules.SafeWalk.description"));
    }

    @EventHandler
    private final Listener<ClipAtLedgeEvent> onClipAtLedge = new Listener<>(event -> {
        event.setClip(true);
    });
}

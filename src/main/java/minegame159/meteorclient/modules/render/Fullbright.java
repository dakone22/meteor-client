/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class Fullbright extends Module {

    public Fullbright() {
        super(Category.Render, "fullbright", I18n.translate("Modules.Fullbright.description"));
    }

    @Override
    public void onActivate() {
        mc.options.gamma = 16;
    }

    @Override
    public void onDeactivate() {
        mc.options.gamma = 1;
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        mc.options.gamma = 16;
    });
}

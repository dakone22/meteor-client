/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render.hud.modules;

import minegame159.meteorclient.mixininterface.IClientPlayerInteractionManager;
import minegame159.meteorclient.modules.render.hud.HUD;

public class BreakingBlockHud extends DoubleTextHudModule {
    public BreakingBlockHud(HUD hud) {
        super(hud, "breaking-block", "Displays percentage of the block you are breaking.", "Breaking Block: ");
    }

    @Override
    protected String getRight() {
        if (mc.interactionManager == null) return "0%";
        return String.format("%.0f%%", ((IClientPlayerInteractionManager) mc.interactionManager).getBreakingProgress() * 100);
    }
}

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class LiquidInteract extends Module {
    public LiquidInteract() {
        super(Category.Player, "liquid-interact", I18n.translate("Module.LiquidInteract.description"));
    }
}

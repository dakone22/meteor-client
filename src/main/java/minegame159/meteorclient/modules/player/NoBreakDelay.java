/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class NoBreakDelay extends Module {
    public NoBreakDelay() {
        super(Category.Player, "no-break-delay", I18n.translate("Modules.NoBreakDelay.description"));
    }
}
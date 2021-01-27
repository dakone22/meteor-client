/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class AntiPacketKick extends Module {
    public AntiPacketKick() {
        super(Category.Misc, "anti-packet-kick", I18n.translate("Modules.AntiPacketKick.description"));
    }
}

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

//Created by squidoodly 27/05/2020

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class BypassDeathScreen extends Module {

    public boolean shouldBypass = false;

    public BypassDeathScreen(){
        super(Category.Misc, "bypass-death-screen", I18n.translate("Module.BypassDeathScreen.description"));
    }

    @Override
    public void onDeactivate() {
        shouldBypass = false;
        super.onDeactivate();
    }
}

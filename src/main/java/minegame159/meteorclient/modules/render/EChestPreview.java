/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */
package minegame159.meteorclient.modules.render;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;

public class EChestPreview extends Module {
    public EChestPreview() {
        super(Category.Render, "EChest-preview", I18n.translate("Modules.EChestPreview.description"));
    }
}
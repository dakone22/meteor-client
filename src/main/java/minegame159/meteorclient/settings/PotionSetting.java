/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.settings;

import minegame159.meteorclient.gui.screens.settings.PotionSettingScreen;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WItemWithLabel;
import minegame159.meteorclient.utils.misc.MyPotion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

import java.util.function.Consumer;

public class PotionSetting extends EnumSetting<MyPotion> {
    public PotionSetting(String name, String title, String description, MyPotion defaultValue, Consumer<MyPotion> onChanged, Consumer<Setting<MyPotion>> onModuleActivated) {
        super(name, title, description, null, defaultValue, onChanged, onModuleActivated);

        widget = new WItemWithLabel(get().potion);
        widget.add(new WButton(I18n.translate("WButton.select"))).getWidget().action = () -> MinecraftClient.getInstance().openScreen(new PotionSettingScreen(this));
    }

    @Override
    public void resetWidget() {
        ((WItemWithLabel) widget).set(get().potion);
    }

    public static class Builder extends EnumSetting.Builder<MyPotion> {
        @Override
        public EnumSetting<MyPotion> build() {
            return new PotionSetting(name, title, description, defaultValue, onChanged, onModuleActivated);
        }
    }
}

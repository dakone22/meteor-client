/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.ItemListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.InvUtils;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class AutoDrop extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
            .name("items")
            .displayName(I18n.translate("Modules.AutoDrop.setting.items.displayName"))
            .description(I18n.translate("Modules.AutoDrop.setting.items.description"))
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    private final Setting<Boolean> excludeHotbar = sgGeneral.add(new BoolSetting.Builder()
            .name("exclude-hotbar")
            .displayName(I18n.translate("Modules.AutoDrop.setting.excludeHotbar.displayName"))
            .description(I18n.translate("Modules.AutoDrop.setting.excludeHotbar.description"))
            .defaultValue(false)
            .build()
    );

    public AutoDrop() {
        super(Category.Player, "auto-drop", I18n.translate("Modules.AutoDrop.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (mc.currentScreen instanceof HandledScreen<?>) return;

        for (int i = excludeHotbar.get() ? 9 : 0; i < mc.player.inventory.size(); i++) {
            if (items.get().contains(mc.player.inventory.getStack(i).getItem())) {
                InvUtils.clickSlot(InvUtils.invIndexToSlotId(i), 1, SlotActionType.THROW);
            }
        }
    });
}

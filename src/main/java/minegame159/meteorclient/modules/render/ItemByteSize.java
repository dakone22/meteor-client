/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.game.GetTooltipEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.misc.ByteCountDataOutput;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.IOException;

public class ItemByteSize extends Module {
    public enum Mode {
        Standard,
        True
    }

    private final SettingGroup sgUseKbIfBigEnough = settings.createGroup(I18n.translate("Module.ItemByteSize.group.sgUseKbIfBigEnough"));

    private final Setting<Boolean> useKbIfBigEnoughEnabled = sgUseKbIfBigEnough.add(new BoolSetting.Builder()
            .name("use-kb-if-big-enough-enabled")
            .displayName(I18n.translate("Module.ItemByteSize.setting.useKbIfBigEnoughEnabled.displayName"))
            .description(I18n.translate("Module.ItemByteSize.setting.useKbIfBigEnoughEnabled.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Mode> mode = sgUseKbIfBigEnough.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Module.ItemByteSize.setting.mode.displayName"))
            .description(I18n.translate("Module.ItemByteSize.setting.mode.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.ItemByteSize.enum.Mode.Standard"),
                    I18n.translate("Module.ItemByteSize.enum.Mode.True"),
            })
            .defaultValue(Mode.True)
            .build()
    );

    public ItemByteSize() {
        super(Category.Render, "item-byte-size", I18n.translate("Module.ItemByteSize.description"));
    }

    @EventHandler
    private final Listener<GetTooltipEvent> onGetTooltip = new Listener<>(event -> {
        try {
            event.itemStack.toTag(new CompoundTag()).write(ByteCountDataOutput.INSTANCE);
            int byteCount = ByteCountDataOutput.INSTANCE.getCount();
            ByteCountDataOutput.INSTANCE.reset();

            event.list.add(new LiteralText(Formatting.GRAY + ModuleManager.INSTANCE.get(ItemByteSize.class).bytesToString(byteCount)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private int getKbSize() {
        return mode.get() == Mode.True ? 1024 : 1000;
    }

    public String bytesToString(int count) {
        if (useKbIfBigEnoughEnabled.get() && count >= getKbSize()) return String.format("%.2f kb", count / (float) getKbSize());
        return String.format("%d bytes", count);
    }
}

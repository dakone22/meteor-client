/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IMinecraftClient;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;

public class FastUse extends Module {

    public enum Mode {
        All,
        Some
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Modules.FastUse.setting.mode.displayName"))
            .description(I18n.translate("Modules.FastUse.setting.mode.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.FastUse.enum.Mode.All"),
                    I18n.translate("Modules.FastUse.enum.Mode.Some"),
            })
            .defaultValue(Mode.All)
            .build()
    );

    private final Setting<Boolean> exp = sgGeneral.add(new BoolSetting.Builder()
            .name("xp")
            .displayName(I18n.translate("Modules.FastUse.setting.exp.displayName"))
            .description(I18n.translate("Modules.FastUse.setting.exp.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> blocks = sgGeneral.add(new BoolSetting.Builder()
            .name("blocks")
            .displayName(I18n.translate("Modules.FastUse.setting.blocks.displayName"))
            .description(I18n.translate("Modules.FastUse.setting.blocks.description"))
            .defaultValue(false)
            .build()
    );

    public FastUse() {
        super(Category.Player, "fast-use", I18n.translate("Modules.FastUse.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        switch (mode.get()) {
            case All:
                ((IMinecraftClient) mc).setItemUseCooldown(0);
                break;
            case Some:
                if (exp.get() && (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE)) ((IMinecraftClient) mc).setItemUseCooldown(0);
                if (blocks.get() && mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getOffHandStack().getItem() instanceof BlockItem) ((IMinecraftClient) mc).setItemUseCooldown(0);
        }
    });
}

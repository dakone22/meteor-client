/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.CityUtils;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class CityESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup(I18n.translate("Modules.CityESP.group.sgRender"));

    // General

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range")
            .displayName(I18n.translate("Modules.CityESP.setting.range.displayName"))
            .description(I18n.translate("Modules.CityESP.setting.range.description"))
            .defaultValue(5)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Boolean> checkBelow = sgGeneral.add(new BoolSetting.Builder()
            .name("check-below")
            .displayName(I18n.translate("Modules.CityESP.setting.checkBelow.displayName"))
            .description(I18n.translate("Modules.CityESP.setting.checkBelow.description"))
            .defaultValue(true)
            .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Modules.CityESP.setting.shapeMode.displayName"))
            .description(I18n.translate("Modules.CityESP.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("fill-color")
            .displayName(I18n.translate("Modules.CityESP.setting.sideColor.displayName"))
            .description(I18n.translate("Modules.CityESP.setting.sideColor.description"))
            .defaultValue(new SettingColor(225, 0, 0, 75))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("outline-color")
            .displayName(I18n.translate("Modules.CityESP.setting.lineColor.displayName"))
            .description(I18n.translate("Modules.CityESP.setting.lineColor.description"))
            .defaultValue(new SettingColor(225, 0, 0, 255))
            .build()
    );

    public CityESP() {
        super(Category.Render, "city-esp", I18n.translate("Modules.CityESP.description"));
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        PlayerEntity target = CityUtils.getPlayerTarget();
        BlockPos targetBlock = CityUtils.getTargetBlock(checkBelow.get());

        if (target == null || targetBlock == null || MathHelper.sqrt(mc.player.squaredDistanceTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ())) > range.get()) return;

        Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, targetBlock, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    });
}

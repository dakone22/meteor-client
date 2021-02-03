/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AirPlace extends Module {

    public enum Place {
        OnClick,
        Always
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup(I18n.translate("Module.AirPlace.group.sgRender"));

    // General

    private final Setting<Place> placeWhen = sgGeneral.add(new EnumSetting.Builder<Place>()
            .name("place-when")
            .displayName(I18n.translate("Module.AirPlace.setting.placeWhen.displayName"))
            .description(I18n.translate("Module.AirPlace.setting.placeWhen.description"))
            .defaultValue(Place.OnClick)
            .build()
    );

    // Render
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .displayName(I18n.translate("Module.AirPlace.setting.render.displayName"))
            .description(I18n.translate("Module.AirPlace.setting.render.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Module.AirPlace.setting.shapeMode.displayName"))
            .description(I18n.translate("Module.AirPlace.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .displayName(I18n.translate("Module.AirPlace.setting.sideColor.displayName"))
            .description(I18n.translate("Module.AirPlace.setting.sideColor.description"))
            .defaultValue(new SettingColor(204, 0, 0, 10))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .displayName(I18n.translate("Module.AirPlace.setting.lineColor.displayName"))
            .description(I18n.translate("Module.AirPlace.setting.lineColor.description"))
            .defaultValue(new SettingColor(204, 0, 0, 255))
            .build()
    );

    private BlockPos target;

    public AirPlace() {
        super(Category.Player, "air-place", I18n.translate("Module.AirPlace.description"));
    }

    @Override
    public void onActivate() {
        target = mc.player.getBlockPos().add(4, 2,0); //lol funni
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {

        if (!(mc.crosshairTarget instanceof BlockHitResult) || !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        target = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

        if (!mc.world.getBlockState(target).isAir()) return;

        if (placeWhen.get() == Place.Always
                || placeWhen.get() == Place.OnClick && (mc.options.keyUse.wasPressed() || mc.options.keyUse.isPressed())) {
            PlayerUtils.placeBlock(target, Hand.MAIN_HAND);
        }

    });

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        if (!(mc.crosshairTarget instanceof BlockHitResult)
                || !mc.world.getBlockState(target).isAir()
                || !(mc.player.getMainHandStack().getItem() instanceof BlockItem)
                || !render.get()) return;

        Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, target, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    });
}

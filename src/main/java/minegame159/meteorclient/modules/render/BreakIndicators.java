/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.mixininterface.IClientPlayerInteractionManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class BreakIndicators extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup(I18n.translate("Modules.BreakIndicators.group.sgRender"));

    // General

    public final Setting<Boolean> multiple = sgGeneral.add(new BoolSetting.Builder()
            .name("multiple")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.multiple.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.multiple.description"))
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> hideVanillaIndicators = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-vanilla-indicators")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.hideVanillaIndicators.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.hideVanillaIndicators.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> smoothAnim = sgGeneral.add(new BoolSetting.Builder()
            .name("smooth-animation")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.smoothAnim.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.smoothAnim.description"))
            .defaultValue(true)
            .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.shapeMode.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> gradientColor1Sides = sgRender.add(new ColorSetting.Builder()
            .name("gradient-color-1-sides")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.gradientColor1Sides.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.gradientColor1Sides.description"))
            .defaultValue(new SettingColor(25, 252, 25, 25))
            .build()
    );

    private final Setting<SettingColor> gradientColor1Lines = sgRender.add(new ColorSetting.Builder()
            .name("gradient-color-1-lines")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.gradientColor1Lines.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.gradientColor1Lines.description"))
            .defaultValue(new SettingColor(25, 252, 25, 100))
            .build()
    );

    private final Setting<SettingColor> gradientColor2Sides = sgRender.add(new ColorSetting.Builder()
            .name("gradient-color-2-sides")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.gradientColor2Sides.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.gradientColor2Sides.description"))
            .defaultValue(new SettingColor(255, 25, 25, 100))
            .build()
    );

    private final Setting<SettingColor> gradientColor2Lines = sgRender.add(new ColorSetting.Builder()
            .name("gradient-color-2-lines")
            .displayName(I18n.translate("Modules.BreakIndicators.setting.gradientColor2Lines.displayName"))
            .description(I18n.translate("Modules.BreakIndicators.setting.gradientColor2Lines.description"))
            .defaultValue(new SettingColor(255, 25, 25, 100))
            .build()
    );

    public final Map<Integer, BlockBreakingInfo> blocks = new HashMap<>();

    private final Color cSides = new Color();
    private final Color cLines = new Color();

    public BreakIndicators() {
        super(Category.Render, "break-indicators", I18n.translate("Modules.BreakIndicators.description"));
    }

    @Override
    public void onDeactivate() {
        blocks.clear();
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        IClientPlayerInteractionManager iam;
        boolean smooth;

        if (smoothAnim.get()) {
            iam = (IClientPlayerInteractionManager) mc.interactionManager;
            BlockPos pos = iam.getCurrentBreakingBlockPos();
            smooth = pos != null && iam.getBreakingProgress() > 0;

            if (smooth && blocks.values().stream().noneMatch(info -> info.getPos().equals(pos))) {
                blocks.put(mc.player.getEntityId(), new BlockBreakingInfo(mc.player.getEntityId(), pos));
            }
        } else {
            iam = null;
            smooth = false;
        }

        blocks.values().forEach(info -> {
            BlockPos pos = info.getPos();
            int stage = info.getStage();

            BlockState state = mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(mc.world, pos);
            if (shape.isEmpty()) return;
            Box orig = shape.getBoundingBox();
            Box box = orig;

            double shrinkFactor;
            if (smooth && iam.getCurrentBreakingBlockPos().equals(pos)) {
                shrinkFactor = 1d - iam.getBreakingProgress();
            } else {
                shrinkFactor = (9 - (stage + 1)) / 9d;
            }
            double progress = 1d - shrinkFactor;

            box = box.shrink(
                    box.getXLength() * shrinkFactor,
                    box.getYLength() * shrinkFactor,
                    box.getZLength() * shrinkFactor
            );

            double xShrink = (orig.getXLength() * shrinkFactor) / 2;
            double yShrink = (orig.getYLength() * shrinkFactor) / 2;
            double zShrink = (orig.getZLength() * shrinkFactor) / 2;

            double x1 = pos.getX() + box.minX + xShrink;
            double y1 = pos.getY() + box.minY + yShrink;
            double z1 = pos.getZ() + box.minZ + zShrink;
            double x2 = pos.getX() + box.maxX + xShrink;
            double y2 = pos.getY() + box.maxY + yShrink;
            double z2 = pos.getZ() + box.maxZ + zShrink;

            // Gradient
            Color c1Sides = gradientColor1Sides.get();
            Color c2Sides = gradientColor2Sides.get();

            cSides.set(
                    (int) Math.round(c1Sides.r + (c2Sides.r - c1Sides.r) * progress),
                    (int) Math.round(c1Sides.g + (c2Sides.g - c1Sides.g) * progress),
                    (int) Math.round(c1Sides.b + (c2Sides.b - c1Sides.b) * progress),
                    (int) Math.round(c1Sides.a + (c2Sides.a - c1Sides.a) * progress)
            );

            Color c1Lines = gradientColor1Lines.get();
            Color c2Lines = gradientColor2Lines.get();

            cLines.set(
                    (int) Math.round(c1Lines.r + (c2Lines.r - c1Lines.r) * progress),
                    (int) Math.round(c1Lines.g + (c2Lines.g - c1Lines.g) * progress),
                    (int) Math.round(c1Lines.b + (c2Lines.b - c1Lines.b) * progress),
                    (int) Math.round(c1Lines.a + (c2Lines.a - c1Lines.a) * progress)
            );

            Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x1, y1, z1, x2, y2, z2, cSides, cLines, shapeMode.get(), 0);
        });
    });
}

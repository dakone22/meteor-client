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
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class BlockSelection extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> advanced = sgGeneral.add(new BoolSetting.Builder()
            .name("advanced")
            .displayName(I18n.translate("Modules.BlockSelection.setting.advanced.displayName"))
            .description(I18n.translate("Modules.BlockSelection.setting.advanced.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Modules.BlockSelection.setting.shapeMode.displayName"))
            .description(I18n.translate("Modules.BlockSelection.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Lines)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("side-color")
            .displayName(I18n.translate("Modules.BlockSelection.setting.sideColor.displayName"))
            .description(I18n.translate("Modules.BlockSelection.setting.sideColor.description"))
            .defaultValue(new SettingColor(255, 255, 255, 50))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("line-color")
            .displayName(I18n.translate("Modules.BlockSelection.setting.lineColor.displayName"))
            .description(I18n.translate("Modules.BlockSelection.setting.lineColor.description"))
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    public BlockSelection() {
        super(Category.Render, "block-selection", I18n.translate("Modules.BlockSelection.description"));
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult)) return;

        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape(mc.world, pos);

        if (shape.isEmpty()) return;
        Box box = shape.getBoundingBox();

        if (advanced.get()) {
            for (Box b : shape.getBoundingBoxes()) {
                render(pos, b);
            }
        } else {
            render(pos, box);
        }
    });

    private void render(BlockPos pos, Box box) {
        Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, pos.getX() + box.minX, pos.getY() + box.minY, pos.getZ() + box.minZ, pos.getX() + box.maxX, pos.getY() + box.maxY, pos.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}

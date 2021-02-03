/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.render.color.SettingColor;
import minegame159.meteorclient.utils.world.Dimension;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VoidESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup(I18n.translate("Module.VoidESP.group.sgRender"));

    // General

    private final Setting<Boolean> airOnly = sgGeneral.add(new BoolSetting.Builder()
            .name("air-only")
            .displayName(I18n.translate("Module.VoidESP.setting.airOnly.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.airOnly.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-radius")
            .displayName(I18n.translate("Module.VoidESP.setting.horizontalRadius.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.horizontalRadius.description"))
            .defaultValue(64)
            .min(0)
            .sliderMax(256)
            .build()
    );

    private final Setting<Integer> holeHeight = sgGeneral.add(new IntSetting.Builder()
            .name("hole-height")
            .displayName(I18n.translate("Module.VoidESP.setting.holeHeight.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.holeHeight.description"))
            .defaultValue(1)  // If we already have one hole in the bedrock layer, there is already something interesting.
            .min(1)
            .sliderMax(5)     // There is no sense to check more than 5.
            .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Module.VoidESP.setting.shapeMode.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("fill-color")
            .displayName(I18n.translate("Module.VoidESP.setting.sideColor.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.sideColor.description"))
            .defaultValue(new SettingColor(225, 25, 25))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .displayName(I18n.translate("Module.VoidESP.setting.lineColor.displayName"))
            .description(I18n.translate("Module.VoidESP.setting.lineColor.description"))
            .defaultValue(new SettingColor(225, 25, 255))
            .build()
    );

    public VoidESP() {
        super(Category.Render, "void-esp", I18n.translate("Module.VoidESP.description"));
    }

    private final List<BlockPos> voidHoles = new ArrayList<>();

    private void getHoles(int searchRange, int holeHeight) {
        voidHoles.clear();
        if (Utils.getDimension() == Dimension.End) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int playerY = playerPos.getY();

        for (int x = -searchRange; x < searchRange; ++x) {
            for (int z = -searchRange; z < searchRange; ++z) {
                BlockPos bottomBlockPos = playerPos.add(x, -playerY, z);

                int blocksFromBottom = 0;
                for (int i = 0; i < holeHeight; ++i)
                    if (isBlockMatching(mc.world.getBlockState(bottomBlockPos.add(0, i, 0)).getBlock()))
                        ++blocksFromBottom;

                if (blocksFromBottom >= holeHeight) voidHoles.add(bottomBlockPos);

                // checking nether roof
                if (Utils.getDimension() == Dimension.Nether) {
                    BlockPos topBlockPos = playerPos.add(x, 127 - playerY, z);

                    int blocksFromTop = 0;
                    for (int i = 0; i < holeHeight; ++i)
                        if (isBlockMatching(mc.world.getBlockState(bottomBlockPos.add(0, 127 - i, 0)).getBlock()))
                            ++blocksFromTop;

                    if (blocksFromTop >= holeHeight) voidHoles.add(topBlockPos);
                }
            }
        }
    }

    private boolean isBlockMatching(Block block) {
        if (airOnly.get())
            return block == Blocks.AIR;
        return block != Blocks.BEDROCK;
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        getHoles(horizontalRadius.get(), holeHeight.get());
    });

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        for (BlockPos voidHole : voidHoles) {
            Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, voidHole, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    });
}

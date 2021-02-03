/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixin.AbstractBlockAccessor;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.DrawMode;
import minegame159.meteorclient.rendering.MeshBuilder;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.misc.Pool;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import minegame159.meteorclient.utils.world.BlockIterator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module {
    public enum Mode {
        Flat,
        Box,
        BoxBelow,
        Glow,
        ReverseGlow
    }

    private static final MeshBuilder MB;
    private static final MeshBuilder _MB;

    static {
        MB = new MeshBuilder(1024);
        _MB = new MeshBuilder(1024);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup(I18n.translate("Modules.HoleESP.group.sgColors"));

    // General

    private final Setting<Mode> renderMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("render-mode")
            .displayName(I18n.translate("Modules.HoleESP.setting.renderMode.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.renderMode.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.HoleESP.enum.Mode.Flat"),
                    I18n.translate("Modules.HoleESP.enum.Mode.Box"),
                    I18n.translate("Modules.HoleESP.enum.Mode.BoxBelow"),
                    I18n.translate("Modules.HoleESP.enum.Mode.Glow"),
                    I18n.translate("Modules.HoleESP.enum.Mode.ReverseGlow")})
            .defaultValue(Mode.Glow)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Modules.HoleESP.setting.shapeMode.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Lines)
            .build()
    );

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-radius")
            .displayName(I18n.translate("Modules.HoleESP.setting.horizontalRadius.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.horizontalRadius.description"))
            .defaultValue(10)
            .min(0)
            .sliderMax(32)
            .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("vertical-radius")
            .displayName(I18n.translate("Modules.HoleESP.setting.verticalRadius.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.verticalRadius.description"))
            .defaultValue(10)
            .min(0)
            .sliderMax(32)
            .build()
    );

    private final Setting<Integer> holeHeight = sgGeneral.add(new IntSetting.Builder()
            .name("hole-height")
            .displayName(I18n.translate("Modules.HoleESP.setting.holeHeight.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.holeHeight.description"))
            .defaultValue(3)
            .min(1)
            .build()
    );

    private final Setting<Double> glowHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("glow-height")
            .displayName(I18n.translate("Modules.HoleESP.setting.glowHeight.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.glowHeight.description"))
            .defaultValue(1)
            .min(0)
            .build()
    );

    // Colors

    private final Setting<Boolean> depthTest = sgColors.add(new BoolSetting.Builder()
            .name("glow-depth-test")
            .displayName(I18n.translate("Modules.HoleESP.setting.depthTest.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.depthTest.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreOwn = sgColors.add(new BoolSetting.Builder()
            .name("ignore-own")
            .displayName(I18n.translate("Modules.HoleESP.setting.ignoreOwn.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.ignoreOwn.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> allBedrock = sgColors.add(new ColorSetting.Builder()
            .name("all-bedrock")
            .displayName(I18n.translate("Modules.HoleESP.setting.allBedrock.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.allBedrock.description"))
            .defaultValue(new SettingColor(25, 225, 25))
            .build()
    );

    private final Setting<SettingColor> someObsidian = sgColors.add(new ColorSetting.Builder()
            .name("some-obsidian")
            .displayName(I18n.translate("Modules.HoleESP.setting.someObsidian.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.someObsidian.description"))
            .defaultValue(new SettingColor(225, 145, 25))
            .build()
    );

    private final Setting<SettingColor> allObsidian = sgColors.add(new ColorSetting.Builder()
            .name("all-obsidian")
            .displayName(I18n.translate("Modules.HoleESP.setting.allObsidian.displayName"))
            .description(I18n.translate("Modules.HoleESP.setting.allObsidian.description"))
            .defaultValue(new SettingColor(225, 25, 25))
            .build()
    );

    private final Pool<Hole> holePool = new Pool<>(Hole::new);
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final List<Hole> holes = new ArrayList<>();
    private final Color transparent = new Color(0, 0, 0, 0);

    public HoleESP() {
        super(Category.Render, "hole-esp", I18n.translate("Modules.HoleESP.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onTick = new Listener<>(event -> {
        for (Hole hole : holes) holePool.free(hole);
        holes.clear();

        BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos1, blockState) -> {
            blockPos.set(blockPos1);

            if (!checkHeight()) return;
            if (ignoreOwn.get() && (mc.player.getBlockPos().equals(blockPos))) return;

            Block bottom = mc.world.getBlockState(add(0, -1, 0)).getBlock();
            if (bottom != Blocks.BEDROCK && bottom != Blocks.OBSIDIAN) return;
            Block forward = mc.world.getBlockState(add(0, 1, 1)).getBlock();
            if (forward != Blocks.BEDROCK && forward != Blocks.OBSIDIAN) return;
            Block back = mc.world.getBlockState(add(0, 0, -2)).getBlock();
            if (back != Blocks.BEDROCK && back != Blocks.OBSIDIAN) return;
            Block right = mc.world.getBlockState(add(1, 0, 1)).getBlock();
            if (right != Blocks.BEDROCK && right != Blocks.OBSIDIAN) return;
            Block left = mc.world.getBlockState(add(-2, 0, 0)).getBlock();
            if (left != Blocks.BEDROCK && left != Blocks.OBSIDIAN) return;
            add(1, 0, 0);

            if (bottom == Blocks.BEDROCK && forward == Blocks.BEDROCK && back == Blocks.BEDROCK && right == Blocks.BEDROCK && left == Blocks.BEDROCK) {
                holes.add(holePool.get().set(blockPos, allBedrock.get()));
            } else {
                int obsidian = 0;

                if (bottom == Blocks.OBSIDIAN) obsidian++;
                if (forward == Blocks.OBSIDIAN) obsidian++;
                if (back == Blocks.OBSIDIAN) obsidian++;
                if (right == Blocks.OBSIDIAN) obsidian++;
                if (left == Blocks.OBSIDIAN) obsidian++;

                if (obsidian == 5) holes.add(holePool.get().set(blockPos, allObsidian.get()));
                else holes.add(holePool.get().set(blockPos, someObsidian.get()));
            }
        });
    });

    private boolean checkHeight() {
        if (((AbstractBlockAccessor) mc.world.getBlockState(blockPos).getBlock()).isCollidable()) return false;

        for (int i = 0; i < holeHeight.get() - 1; i++) {
            if (((AbstractBlockAccessor) mc.world.getBlockState(add(0, 1, 0)).getBlock()).isCollidable()) return false;
        }

        add(0, -holeHeight.get() + 1, 0);
        return true;
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        if (renderMode.get() == Mode.Glow || renderMode.get() == Mode.ReverseGlow) {
            MB.depthTest = depthTest.get();
            _MB.depthTest = depthTest.get();
            MB.begin(event, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
            _MB.begin(event, DrawMode.Lines, VertexFormats.POSITION_COLOR);
        }

        for (Hole hole : holes) {
            int x = hole.blockPos.getX();
            int y = hole.blockPos.getY();
            int z = hole.blockPos.getZ();

            switch (renderMode.get()) {
                case Flat:
                    Renderer.quadWithLinesHorizontal(Renderer.NORMAL, Renderer.LINES, x, y, z, 1, hole.colorSides, hole.colorLines, shapeMode.get());
                    break;
                case Box:
                    Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, blockPos, hole.colorSides, hole.colorLines, shapeMode.get(), 0);
                    break;
                case BoxBelow:
                    Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x, y - 1, z, 1, hole.colorSides, hole.colorLines, shapeMode.get(), 0);
                    break;
                case Glow:
                    Renderer.quadWithLinesHorizontal(Renderer.NORMAL, Renderer.LINES, x, y, z, 1, hole.colorSides, hole.colorLines, shapeMode.get());
                    MB.gradientBoxSides(x, y, z, x + 1, y + glowHeight.get(), z + 1, hole.colorSides, transparent);
                    gradientBoxVertical(x, y, z, glowHeight.get(), hole.colorLines, transparent, false);
                    break;
                case ReverseGlow:
                    Renderer.quadWithLinesHorizontal(Renderer.NORMAL, Renderer.LINES, x, y + glowHeight.get(), z, 1, hole.colorSides, hole.colorLines, shapeMode.get());
                    MB.gradientBoxSides(x, y, z, x + 1, y + glowHeight.get(), z + 1, transparent, hole.colorSides);
                    gradientBoxVertical(x, y, z, glowHeight.get(), hole.colorLines, transparent, true);
                    break;
            }
        }

        if (renderMode.get() == Mode.ReverseGlow || renderMode.get() == Mode.Glow) {
            MB.end();
            _MB.end();
        }
    });

    private BlockPos.Mutable add(int x, int y, int z) {
        blockPos.setX(blockPos.getX() + x);
        blockPos.setY(blockPos.getY() + y);
        blockPos.setZ(blockPos.getZ() + z);

        return blockPos;
    }

    private static class Hole {
        public BlockPos.Mutable blockPos = new BlockPos.Mutable();
        public Color colorSides = new Color();
        public Color colorLines = new Color();

        public Hole set(BlockPos blockPos, Color color) {
            this.blockPos.set(blockPos);

            colorLines.set(color);
            colorSides.set(color);
            colorSides.a -= 175;
            colorSides.validate();

            return this;
        }
    }

    private void gradientBoxVertical(double x, double y, double z, double height, Color startColor, Color endColor, boolean reverse) {
        if (!reverse) {
            _MB.gradientLine(x, y, z, x, y + height, z, startColor, endColor);
            _MB.gradientLine(x + 1, y, z, x + 1, y + height, z, startColor, endColor);
            _MB.gradientLine(x, y, z + 1, x, y + height, z + 1, startColor, endColor);
            _MB.gradientLine(x + 1, y, z + 1, x + 1, y + height, z + 1, startColor, endColor);
        }
        else {
            _MB.gradientLine(x, y + height, z, x, y, z, startColor, endColor);
            _MB.gradientLine(x + 1, y + height, z, x + 1, y, z, startColor, endColor);
            _MB.gradientLine(x, y + height, z + 1, x, y, z + 1, startColor, endColor);
            _MB.gradientLine(x + 1, y + height, z + 1, x + 1, y, z + 1, startColor, endColor);
        }
    }
}
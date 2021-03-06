/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.entity.EntityUtils;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

public class Chams extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    // General
    
    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Select entities to show through walls.")
            .defaultValue(new Object2BooleanOpenHashMap<>(0))
            .build()
    );

    public final Setting<Boolean> throughWalls = sgGeneral.add(new BoolSetting.Builder()
            .name("through-walls")
            .description("Renders entities through walls.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> colored = sgGeneral.add(new BoolSetting.Builder()
            .name("colored")
            .description("Renders entity models with a custom color.")
            .defaultValue(false)
            .build()
    );

    // Colors

    public final Setting<Boolean> useNameColor = sgColors.add(new BoolSetting.Builder()
            .name("use-name-color")
            .description("Uses players displayname color for the chams color (good for minigames).")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> playersColor = sgColors.add(new ColorSetting.Builder()
            .name("players-color")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> animalsColor = sgColors.add(new ColorSetting.Builder()
            .name("animals-color")
            .description("The animal's color.")
            .defaultValue(new SettingColor(25, 255, 25, 255))
            .build()
    );

    private final Setting<SettingColor> waterAnimalsColor = sgColors.add(new ColorSetting.Builder()
            .name("water-animals-color")
            .description("The water animal's color.")
            .defaultValue(new SettingColor(25, 25, 255, 255))
            .build()
    );

    private final Setting<SettingColor> monstersColor = sgColors.add(new ColorSetting.Builder()
            .name("monsters-color")
            .description("The monster's color.")
            .defaultValue(new SettingColor(255, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> ambientColor = sgColors.add(new ColorSetting.Builder()
            .name("ambient-color")
            .description("The ambient's color.")
            .defaultValue(new SettingColor(25, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> miscColor = sgColors.add(new ColorSetting.Builder()
            .name("misc-color")
            .description("The misc color.")
            .defaultValue(new SettingColor(175, 175, 175, 255))
            .build()
    );

    public Chams() {
        super(Category.Render, "chams", "Renders entities through walls.");
    }

    public boolean ignoreRender(Entity entity) {
        return !isActive() || !entities.get().getBoolean(entity.getType());
    }

    public boolean renderChams(EntityModel<LivingEntity> model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, LivingEntity entity) {
        if (ignoreRender(entity) || !colored.get()) return false;
        Color color = EntityUtils.getEntityColor(entity, playersColor.get(), animalsColor.get(), waterAnimalsColor.get(), monstersColor.get(), ambientColor.get(), miscColor.get(), useNameColor.get());
        model.render(matrices, vertices, light, overlay, (float)color.r/255f, (float)color.g/255f, (float)color.b/255f, (float)color.a/255f);
        return true;
    }

    // TODO: 30/12/2020 Fix crystal chams
    // also fix cape rendering in chams

//    public boolean renderChamsCrystal(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
//        if (!isActive() || !entities.get().contains(EntityType.END_CRYSTAL) || !colored.get()) return false;
//        Color color = miscColor.get();
//        modelPart.render(matrices, vertices, light, overlay, (float)color.r/255f, (float)color.g/255f, (float)color.b/255f, (float)color.a/255f);
//        return true;
//    }
}

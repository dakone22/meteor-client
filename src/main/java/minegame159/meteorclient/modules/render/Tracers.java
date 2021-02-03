/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.entity.EntityUtils;
import minegame159.meteorclient.utils.entity.Target;
import minegame159.meteorclient.utils.render.RenderUtils;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class Tracers extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAppearance = settings.createGroup(I18n.translate("Module.Tracers.group.sgAppearance"));
    private final SettingGroup sgColors = settings.createGroup(I18n.translate("Module.Tracers.group.sgColors"));

    // General

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entites")
            .displayName(I18n.translate("Module.Tracers.setting.entities.displayName"))
            .description(I18n.translate("Module.Tracers.setting.entities.description"))
            .defaultValue(Utils.asObject2BooleanOpenHashMap(EntityType.PLAYER))
            .build()
    );

    private final Setting<Boolean> storage = sgGeneral.add(new BoolSetting.Builder()
            .name("storage")
            .displayName(I18n.translate("Module.Tracers.setting.storage.displayName"))
            .description(I18n.translate("Module.Tracers.setting.storage.description"))
            .defaultValue(false)
            .build()
    );

    // Appearance

    private final Setting<Target> target = sgAppearance.add(new EnumSetting.Builder<Target>()
            .name("target")
            .displayName(I18n.translate("Module.Tracers.setting.target.displayName"))
            .description(I18n.translate("Module.Tracers.setting.target.description"))
            .defaultValue(Target.Body)
            .build()
    );

    private final Setting<Boolean> stem = sgAppearance.add(new BoolSetting.Builder()
            .name("stem")
            .displayName(I18n.translate("Module.Tracers.setting.stem.displayName"))
            .description(I18n.translate("Module.Tracers.setting.stem.description"))
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> showInvis = sgGeneral.add(new BoolSetting.Builder()
            .name("show-invisible")
            .displayName(I18n.translate("Module.Tracers.setting.showInvis.displayName"))
            .description(I18n.translate("Module.Tracers.setting.showInvis.description"))
            .defaultValue(true)
            .build()
    );

    // Colors

    public final Setting<Boolean> useNameColor = sgColors.add(new BoolSetting.Builder()
            .name("use-name-color")
            .displayName(I18n.translate("Module.Tracers.setting.useNameColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.useNameColor.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> playersColor = sgColors.add(new ColorSetting.Builder()
            .name("players-colors")
            .displayName(I18n.translate("Module.Tracers.setting.playersColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.playersColor.description"))
            .defaultValue(new SettingColor(205, 205, 205, 127))
            .build()
    );

    private final Setting<SettingColor> animalsColor = sgColors.add(new ColorSetting.Builder()
            .name("animals-color")
            .displayName(I18n.translate("Module.Tracers.setting.animalsColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.animalsColor.description"))
            .defaultValue(new SettingColor(145, 255, 145, 127))
            .build()
    );

    private final Setting<SettingColor> waterAnimalsColor = sgColors.add(new ColorSetting.Builder()
            .name("water-animals-color")
            .displayName(I18n.translate("Module.Tracers.setting.waterAnimalsColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.waterAnimalsColor.description"))
            .defaultValue(new SettingColor(145, 145, 255, 127))
            .build()
    );

    private final Setting<SettingColor> monstersColor = sgColors.add(new ColorSetting.Builder()
            .name("monsters-color")
            .displayName(I18n.translate("Module.Tracers.setting.monstersColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.monstersColor.description"))
            .defaultValue(new SettingColor(255, 145, 145, 127))
            .build()
    );

    private final Setting<SettingColor> ambientColor = sgColors.add(new ColorSetting.Builder()
            .name("ambient-color")
            .displayName(I18n.translate("Module.Tracers.setting.ambientColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.ambientColor.description"))
            .defaultValue(new SettingColor(75, 75, 75, 127))
            .build()
    );

    private final Setting<SettingColor> miscColor = sgColors.add(new ColorSetting.Builder()
            .name("misc-color")
            .displayName(I18n.translate("Module.Tracers.setting.miscColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.miscColor.description"))
            .defaultValue(new SettingColor(145, 145, 145, 127))
            .build()
    );

    private final Setting<SettingColor> storageColor = sgColors.add(new ColorSetting.Builder()
            .name("storage-color")
            .displayName(I18n.translate("Module.Tracers.setting.storageColor.displayName"))
            .description(I18n.translate("Module.Tracers.setting.storageColor.description"))
            .defaultValue(new SettingColor(255, 160, 0, 127))
            .build()
    );

    private int count;

    public Tracers() {
        super(Category.Render, "tracers", I18n.translate("Module.Tracers.description"));
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        count = 0;


        for (Entity entity : mc.world.getEntities()) {
            if ((!ModuleManager.INSTANCE.isActive(Freecam.class) && entity == mc.player) || !entities.get().getBoolean(entity.getType()) || (!showInvis.get() && entity.isInvisible())) continue;
            Color color = EntityUtils.getEntityColor(entity, playersColor.get(), animalsColor.get(), waterAnimalsColor.get(), monstersColor.get(), ambientColor.get(), miscColor.get(), useNameColor.get());
            RenderUtils.drawTracerToEntity(event, entity, color, target.get(), stem.get()); count++;
        }

        if (storage.get()) {
            for (BlockEntity blockEntity : mc.world.blockEntities) {
                if (blockEntity.isRemoved()) continue;
                if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity) {
                    RenderUtils.drawTracerToBlockEntity(blockEntity, storageColor.get(), event);
                    count++;
                }
            }
        }
    });

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}

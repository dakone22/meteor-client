/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.EntityTypeListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class Hitboxes extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .displayName(I18n.translate("Module.Hitboxes.setting.entities.displayName"))
            .description(I18n.translate("Module.Hitboxes.setting.entities.description"))
            .defaultValue(Utils.asObject2BooleanOpenHashMap(EntityType.PLAYER))
            .build()
    );

    private final Setting<Double> value = sgGeneral.add(new DoubleSetting.Builder()
            .name("expand")
            .displayName(I18n.translate("Module.Hitboxes.setting.value.displayName"))
            .description(I18n.translate("Module.Hitboxes.setting.value.description"))
            .defaultValue(0.5)
            .build()
    );

    public Hitboxes() {
        super(Category.Combat, "hitboxes", I18n.translate("Module.Hitboxes.description"));
    }

    public double getEntityValue(Entity entity) {
        if (!isActive()) return 0;
        if (entities.get().getBoolean(entity.getType())) return value.get();
        return 0;
    }
}

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.EntityAddedEvent;
import minegame159.meteorclient.events.entity.EntityRemovedEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.StringSetting;
import minegame159.meteorclient.utils.entity.FakePlayerEntity;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;

public class VisualRange extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .displayName(I18n.translate("Module.VisualRange.setting.ignoreFriends.displayName"))
            .description(I18n.translate("Module.VisualRange.setting.ignoreFriends.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreFakes = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-fakeplayers")
            .displayName(I18n.translate("Module.VisualRange.setting.ignoreFakes.displayName"))
            .description(I18n.translate("Module.VisualRange.setting.ignoreFakes.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<String> enterMessage = sgGeneral.add(new StringSetting.Builder()
            .name("enter-message")
            .displayName(I18n.translate("Module.VisualRange.setting.enterMessage.displayName"))
            .description(I18n.translate("Module.VisualRange.setting.enterMessage.description"))
            .defaultValue(I18n.translate("Module.VisualRange.setting.enterMessage.defaultValue"))
            .build()
    );

    private final Setting<String> leaveMessage = sgGeneral.add(new StringSetting.Builder()
            .name("leave-message")
            .displayName(I18n.translate("Module.VisualRange.setting.leaveMessage.displayName"))
            .description(I18n.translate("Module.VisualRange.setting.leaveMessage.description"))
            .defaultValue(I18n.translate("Module.VisualRange.setting.leaveMessage.defaultValue"))
            .build()
    );


    public VisualRange() {
        super(Category.Misc, "visual-range", I18n.translate("Module.VisualRange.description"));
    }

    @EventHandler
    private final Listener<EntityAddedEvent> onEntityAdded = new Listener<>(event -> {
        if (event.entity.equals(mc.player) || !(event.entity instanceof PlayerEntity) || !FriendManager.INSTANCE.attack((PlayerEntity) event.entity) && ignoreFriends.get() || (event.entity instanceof FakePlayerEntity && ignoreFakes.get())) return;

        String enter = enterMessage.get().replace("{player}", ((PlayerEntity) event.entity).getGameProfile().getName());
        ChatUtils.moduleInfo(this, enter);
    });

    @EventHandler
    private final Listener<EntityRemovedEvent> onEntityRemoved = new Listener<>(event -> {
        if (event.entity.equals(mc.player) || !(event.entity instanceof PlayerEntity) || !FriendManager.INSTANCE.attack((PlayerEntity) event.entity) && ignoreFriends.get() || (event.entity instanceof FakePlayerEntity && ignoreFakes.get())) return;

        String leave = leaveMessage.get().replace("{player}", ((PlayerEntity) event.entity).getGameProfile().getName());
        ChatUtils.moduleInfo(this, leave);
    });
}

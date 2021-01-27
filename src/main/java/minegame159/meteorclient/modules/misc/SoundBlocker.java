/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.PlaySoundEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.SoundEventListSetting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.sound.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class SoundBlocker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
            .name("sounds")
            .displayName(I18n.translate("Modules.SoundBlocker.setting.sounds.displayName"))
            .description(I18n.translate("Modules.SoundBlocker.setting.sounds.description"))
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    public SoundBlocker() {
        super(Category.Misc, "sound-blocker", I18n.translate("Modules.SoundBlocker.description"));
    }

    @EventHandler
    private final Listener<PlaySoundEvent> onPlaySound = new Listener<>(event -> {
        for (SoundEvent sound : sounds.get()) {
            if (sound.getId().equals(event.sound.getId())) {
                event.cancel();
                break;
            }
        }
    });
}

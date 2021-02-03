/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.meteor.KeyEvent;
import minegame159.meteorclient.events.world.PlaySoundEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;

public class AutoFish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSplashRangeDetection = settings.createGroup(I18n.translate("Module.AutoFish.group.sgSplashRangeDetection"));

    // General
    private final Setting<Boolean> autoCast = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-cast")
            .displayName(I18n.translate("Module.AutoFish.setting.autoCast.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.autoCast.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> ticksAutoCast = sgGeneral.add(new IntSetting.Builder()
            .name("ticks-auto-cast")
            .displayName(I18n.translate("Module.AutoFish.setting.ticksAutoCast.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.ticksAutoCast.description"))
            .defaultValue(10)
            .min(0)
            .sliderMax(60)
            .build()
    );

    private final Setting<Integer> ticksCatch = sgGeneral.add(new IntSetting.Builder()
            .name("ticks-catch")
            .displayName(I18n.translate("Module.AutoFish.setting.ticksCatch.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.ticksCatch.description"))
            .defaultValue(6)
            .min(0)
            .sliderMax(60)
            .build()
    );

    private final Setting<Integer> ticksThrow = sgGeneral.add(new IntSetting.Builder()
            .name("ticks-throw")
            .displayName(I18n.translate("Module.AutoFish.setting.ticksThrow.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.ticksThrow.description"))
            .defaultValue(14)
            .min(0)
            .sliderMax(60)
            .build()
    );

    // Splash range detection
    private final Setting<Boolean> splashDetectionRangeEnabled = sgSplashRangeDetection.add(new BoolSetting.Builder()
            .name("splash-detection-range-enabled")
            .displayName(I18n.translate("Module.AutoFish.setting.splashDetectionRangeEnabled.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.splashDetectionRangeEnabled.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> splashDetectionRange = sgSplashRangeDetection.add(new DoubleSetting.Builder()
            .name("splash-detection-range")
            .displayName(I18n.translate("Module.AutoFish.setting.splashDetectionRange.displayName"))
            .description(I18n.translate("Module.AutoFish.setting.splashDetectionRange.description"))
            .defaultValue(10)
            .min(0)
            .build()
    );

    private boolean ticksEnabled;
    private int ticksToRightClick;
    private int ticksData;

    private int autoCastTimer;
    private boolean autoCastEnabled;

    private int autoCastCheckTimer;

    public AutoFish() {
        super(Category.Player, "auto-fish", I18n.translate("Module.AutoFish.description"));
    }

    @Override
    public void onActivate() {
        ticksEnabled = false;
        autoCastEnabled = false;
        autoCastCheckTimer = 0;
    }

    @EventHandler
    private final Listener<PlaySoundEvent> onPlaySound = new Listener<>(event -> {
        SoundInstance p = event.sound;
        FishingBobberEntity b = mc.player.fishHook;

        if (p.getId().getPath().equals("entity.fishing_bobber.splash")) {
            if (!splashDetectionRangeEnabled.get() || Utils.distance(b.getX(), b.getY(), b.getZ(), p.getX(), p.getY(), p.getZ()) <= splashDetectionRange.get()) {
                ticksEnabled = true;
                ticksToRightClick = ticksCatch.get();
                ticksData = 0;
            }
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        // Auto cast
        if (autoCastCheckTimer <= 0) {
            autoCastCheckTimer = 30;

            if (autoCast.get() && !ticksEnabled && !autoCastEnabled && mc.player.fishHook == null && mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
                autoCastTimer = 0;
                autoCastEnabled = true;
            }
        } else {
            autoCastCheckTimer--;
        }

        // Check for auto cast timer
        if (autoCastEnabled) {
            autoCastTimer++;

            if (autoCastTimer > ticksAutoCast.get()) {
                autoCastEnabled = false;
                Utils.rightClick();
            }
        }

        // Handle logic
        if (ticksEnabled && ticksToRightClick <= 0) {
            if (ticksData == 0) {
                Utils.rightClick();
                ticksToRightClick = ticksThrow.get();
                ticksData = 1;
            }
            else if (ticksData == 1) {
                Utils.rightClick();
                ticksEnabled = false;
            }
        }

        ticksToRightClick--;
    });

    @EventHandler
    private final Listener<KeyEvent> onKey = new Listener<>(event -> {
        if (mc.options.keyUse.isPressed()) ticksEnabled = false;
    });
}

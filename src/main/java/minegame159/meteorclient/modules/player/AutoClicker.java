/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IKeyBinding;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.resource.language.I18n;

public class AutoClicker extends Module {

    public enum Mode {
        Hold,
        Press
    }

    public enum Button {
        Right,
        Left
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Modules.AutoClicker.setting.mode.displayName"))
            .description(I18n.translate("Modules.AutoClicker.setting.mode.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.AutoClicker.enum.Mode.Hold"),
                    I18n.translate("Modules.AutoClicker.enum.Mode.Press"),
            })
            .defaultValue(Mode.Press)
            .build() 
    );

    private final Setting<Button> button = sgGeneral.add(new EnumSetting.Builder<Button>()
            .name("button")
            .displayName(I18n.translate("Modules.AutoClicker.setting.button.displayName"))
            .description(I18n.translate("Modules.AutoClicker.setting.button.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.AutoClicker.enum.Button.Right"),
                    I18n.translate("Modules.AutoClicker.enum.Button.Left"),
            })
            .defaultValue(Button.Right)
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .displayName(I18n.translate("Modules.AutoClicker.setting.delay.displayName"))
            .description(I18n.translate("Modules.AutoClicker.setting.delay.description"))
            .defaultValue(2)
            .min(0)
            .sliderMax(60)
            .build()
    );

    private int timer;

    public AutoClicker() {
        super(Category.Player, "auto-clicker", I18n.translate("Modules.AutoClicker.description"));
    }

    @Override
    public void onActivate() {
        timer = 0;
        ((IKeyBinding)mc.options.keyAttack).setPressed(false);
        ((IKeyBinding)mc.options.keyUse).setPressed(false);
    }

    @Override
    public void onDeactivate() {
        ((IKeyBinding)mc.options.keyAttack).setPressed(false);
        ((IKeyBinding)mc.options.keyUse).setPressed(false);
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        switch (mode.get()) {
            case Hold:
                switch (button.get()) {
                    case Left:
                        ((IKeyBinding)mc.options.keyAttack).setPressed(true);
                        break;
                    case Right:
                        ((IKeyBinding)mc.options.keyUse).setPressed(true);
                        break;
                }
                break;
            case Press:
                timer++;
                if (!(delay.get() > timer)) {
                    switch (button.get()) {
                        case Left:
                            Utils.leftClick();
                            break;
                        case Right:
                            Utils.rightClick();
                            break;
                    }
                    timer = 0;
                }
        }
    });
}

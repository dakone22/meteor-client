/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.gui.widgets.*;
import minegame159.meteorclient.mixininterface.IKeyBinding;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AntiAFK extends Module {

    public enum SpinMode {
        Server,
        Client
    }

    public AntiAFK() {
        super(Category.Player, "anti-afk", I18n.translate("Module.AntiAFK.description"));
    }

    private final SettingGroup sgActions = settings.createGroup(I18n.translate("Module.AntiAFK.group.sgActions"));
    private final SettingGroup sgMessages = settings.createGroup(I18n.translate("Module.AntiAFK.group.sgMessages"));

    // Actions

    private final Setting<Boolean> spin = sgActions.add(new BoolSetting.Builder()
            .name("spin")
            .displayName(I18n.translate("Module.AntiAFK.setting.spin.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.spin.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<SpinMode> spinMode = sgActions.add(new EnumSetting.Builder<SpinMode>()
            .name("spin-mode")
            .displayName(I18n.translate("Module.AntiAFK.setting.spinMode.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.spinMode.description"))
            .defaultValue(SpinMode.Server)
            .build()
    );

    private final Setting<Integer> spinSpeed = sgActions.add(new IntSetting.Builder()
            .name("spin-speed")
            .displayName(I18n.translate("Module.AntiAFK.setting.spinSpeed.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.spinSpeed.description"))
            .defaultValue(7)
            .build()
    );

    private final Setting<Double> pitch = sgActions.add(new DoubleSetting.Builder()
            .name("pitch")
            .displayName(I18n.translate("Module.AntiAFK.setting.pitch.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.pitch.description"))
            .defaultValue(-90)
            .min(-90)
            .max(90)
            .sliderMin(-90)
            .sliderMax(90)
            .build()
    );

    private final Setting<Boolean> jump = sgActions.add(new BoolSetting.Builder()
            .name("jump")
            .displayName(I18n.translate("Module.AntiAFK.setting.jump.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.jump.description"))
            .defaultValue(true)
            .build());

    private final Setting<Boolean> click = sgActions.add(new BoolSetting.Builder()
            .name("click")
            .displayName(I18n.translate("Module.AntiAFK.setting.click.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.click.description"))
            .defaultValue(false)
            .build());

    private final Setting<Boolean> disco = sgActions.add(new BoolSetting.Builder()
            .name("disco")
            .displayName(I18n.translate("Module.AntiAFK.setting.disco.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.disco.description"))
            .defaultValue(false)
            .build());

    private final Setting<Boolean> strafe = sgActions.add(new BoolSetting.Builder()
            .name("strafe")
            .displayName(I18n.translate("Module.AntiAFK.setting.strafe.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.strafe.description"))
            .defaultValue(false)
            .onChanged(aBoolean -> {
                strafeTimer = 0;
                direction = false;
            })
            .build());

    // Messages

    private final Setting<Boolean> sendMessages = sgMessages.add(new BoolSetting.Builder()
            .name("send-messages")
            .displayName(I18n.translate("Module.AntiAFK.setting.sendMessages.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.sendMessages.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> delay = sgMessages.add(new IntSetting.Builder()
            .name("delay")
            .displayName(I18n.translate("Module.AntiAFK.setting.delay.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.delay.description"))
            .defaultValue(20)
            .min(0)
            .sliderMax(500)
            .build()
    );

    private final Setting<Boolean> randomMessage = sgMessages.add(new BoolSetting.Builder()
            .name("random")
            .displayName(I18n.translate("Module.AntiAFK.setting.randomMessage.displayName"))
            .description(I18n.translate("Module.AntiAFK.setting.randomMessage.description"))
            .defaultValue(false)
            .build()
    );

    private final List<String> messages = new ArrayList<>();
    private int timer;
    private int messageI;
    private int strafeTimer = 0;
    private boolean direction = false;

    private final Random random = new Random();

    private float prevYaw;

    @Override
    public void onActivate() {
        prevYaw = mc.player.yaw;
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (Utils.canUpdate()) {

            //Spin
            if (spin.get()) {
                prevYaw += spinSpeed.get();
                switch (spinMode.get()) {
                    case Client:
                        mc.player.yaw = prevYaw;
                        break;
                    case Server:
                        RotationUtils.packetRotate(prevYaw, pitch.get().floatValue());
                        break;
                }
            }

            //Jump
            if (jump.get() && mc.options.keyJump.isPressed()) ((IKeyBinding) mc.options.keyJump).setPressed(false);
            if (jump.get() && mc.options.keySneak.isPressed()) ((IKeyBinding) mc.options.keySneak).setPressed(false);
            else if (jump.get() && random.nextInt(99) + 1 == 50) ((IKeyBinding) mc.options.keyJump).setPressed(true);

            //Click
            if (click.get() && random.nextInt(99) + 1 == 45) {
                mc.options.keyAttack.setPressed(true);
                Utils.leftClick();
                mc.options.keyAttack.setPressed(false);
            }

            //Disco
            if (disco.get() && random.nextInt(24) + 1 == 15) ((IKeyBinding) mc.options.keySneak).setPressed(true);

            //Spam
            if (sendMessages.get() && !messages.isEmpty())
                if (timer <= 0) {
                    int i;
                    if (randomMessage.get()) {
                        i = Utils.random(0, messages.size());
                    } else {
                        if (messageI >= messages.size()) messageI = 0;
                        i = messageI++;
                    }

                    mc.player.sendChatMessage(messages.get(i));

                    timer = delay.get();
                } else {
                    timer--;
                }

            //Strafe
            if (strafe.get() && strafeTimer == 20) {
                ((IKeyBinding) mc.options.keyLeft).setPressed(!direction);
                ((IKeyBinding) mc.options.keyRight).setPressed(direction);
                direction = !direction;
                strafeTimer = 0;
            } else
                strafeTimer++;

        }
    });

    @Override
    public WWidget getWidget() {
        messages.removeIf(String::isEmpty);

        WTable table = new WTable();
        fillTable(table);
        return table;
    }

    private void fillTable(WTable table) {
        table.add(new WHorizontalSeparator(I18n.translate("Module.AntiAFK.MessageList.title")));

        // Messages
        for (int i = 0; i < messages.size(); i++) {
            int msgI = i;
            String message = messages.get(i);

            WTextBox textBox = table.add(new WTextBox(message, 100)).fillX().expandX().getWidget();
            textBox.action = () -> messages.set(msgI, textBox.getText());

            WMinus minus = table.add(new WMinus()).getWidget();
            minus.action = () -> {
                messages.remove(msgI);

                table.clear();
                fillTable(table);
            };

            table.row();
        }

        // New Message
        WPlus plus = table.add(new WPlus()).fillX().right().getWidget();
        plus.action = () -> {
            messages.add("");

            table.clear();
            fillTable(table);
        };
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        messages.removeIf(String::isEmpty);
        ListTag messagesTag = new ListTag();

        for (String message : messages) messagesTag.add(StringTag.of(message));
        tag.put("messages", messagesTag);

        return tag;
    }

    @Override
    public Module fromTag(CompoundTag tag) {
        messages.clear();

        if (tag.contains("messages")) {
            ListTag messagesTag = tag.getList("messages", 8);
            for (Tag messageTag : messagesTag) messages.add(messageTag.asString());
        } else {
            messages.add("This is an AntiAFK message. Meteor on Crack!");
        }

        return super.fromTag(tag);
    }
}
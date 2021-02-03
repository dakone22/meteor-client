/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.misc.input.Input;
import net.minecraft.client.options.Perspective;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.glfw.GLFW;

public class FreeRotate extends Module {

    public enum Mode {
        Player,
        Camera
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgArrows = settings.createGroup(I18n.translate("Modules.FreeRotate.group.sgArrows"));

    // General

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Modules.FreeRotate.setting.mode.displayName"))
            .description(I18n.translate("Modules.FreeRotate.setting.mode.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.FreeRotate.enum.Mode.Player"),
                    I18n.translate("Modules.FreeRotate.enum.Mode.Camera")})
            .defaultValue(Mode.Player)
            .build()
    );

    public final Setting<Boolean> togglePerpective = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-perspective")
            .displayName(I18n.translate("Modules.FreeRotate.setting.togglePerpective.displayName"))
            .description(I18n.translate("Modules.FreeRotate.setting.togglePerpective.description"))
            .defaultValue(true)
            .build()
    );

    public final Setting<Double> sensitivity = sgGeneral.add(new DoubleSetting.Builder()
            .name("camera-sensitivity")
            .displayName(I18n.translate("Modules.FreeRotate.setting.sensitivity.displayName"))
            .description(I18n.translate("Modules.FreeRotate.setting.sensitivity.description"))
            .defaultValue(8)
            .min(0)
            .sliderMax(10)
            .build()
    );

    // Arrows

    public final Setting<Boolean> arrows = sgArrows.add(new BoolSetting.Builder()
            .name("arrows-control-opposite")
            .displayName(I18n.translate("Modules.FreeRotate.setting.arrows.displayName"))
            .description(I18n.translate("Modules.FreeRotate.setting.arrows.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> arrowSpeed = sgArrows.add(new DoubleSetting.Builder()
            .name("arrow-speed")
            .displayName(I18n.translate("Modules.FreeRotate.setting.arrowSpeed.displayName"))
            .description(I18n.translate("Modules.FreeRotate.setting.arrowSpeed.description"))
            .defaultValue(4)
            .min(0)
            .build()
    );

    public FreeRotate() {
        super(Category.Render, "free-rotate", I18n.translate("Modules.FreeRotate.description"));
    }

    public float cameraYaw;
    public float cameraPitch;

    private Perspective prePers;

    @Override
    public void onActivate() {
        cameraYaw = mc.player.yaw;
        cameraPitch = mc.player.pitch;
        prePers = mc.options.getPerspective();

        if (prePers != Perspective.THIRD_PERSON_BACK &&  togglePerpective.get()) mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    public void onDeactivate() {
        if (mc.options.getPerspective() != prePers && togglePerpective.get()) mc.options.setPerspective(prePers);
    }

    public boolean playerMode() {
        return isActive() && mc.options.getPerspective() == Perspective.THIRD_PERSON_BACK && mode.get() == Mode.Player;
    }

    public boolean cameraMode() {
        return isActive() && mc.options.getPerspective() == Perspective.THIRD_PERSON_BACK && mode.get() == Mode.Camera;
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (arrows.get()) {
            for (int i = 0; i < (arrowSpeed.get() * 2); i++) {
                switch (mode.get()) {
                    case Player:
                        if (Input.isPressed(GLFW.GLFW_KEY_LEFT)) cameraYaw -= 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_RIGHT)) cameraYaw += 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_UP)) cameraPitch -= 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_DOWN)) cameraPitch += 0.5;
                        break;
                    case Camera:
                        if (Input.isPressed(GLFW.GLFW_KEY_LEFT)) mc.player.yaw -= 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_RIGHT)) mc.player.yaw += 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_UP)) mc.player.pitch -= 0.5;
                        if (Input.isPressed(GLFW.GLFW_KEY_DOWN)) mc.player.pitch += 0.5;
                        break;
                }
            }
        }

        mc.player.pitch = Utils.clamp(mc.player.pitch, -90, 90);
        cameraPitch = Utils.clamp(cameraPitch, -90, 90);
    });
}
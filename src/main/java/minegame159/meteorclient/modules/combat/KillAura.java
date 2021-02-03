/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import baritone.api.BaritoneAPI;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.entity.Target;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class KillAura extends Module {
    public enum Priority {
        LowestDistance,
        HighestDistance,
        LowestHealth,
        HighestHealth
    }

    public enum OnlyWith {
        Sword,
        Axe,
        SwordOrAxe,
        Any
    }

    public enum RotationDirection {
        Eyes,
        Chest,
        Feet
    }

    public enum RotationMode {
        Always,
        OnHit,
        None
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelay = settings.createGroup(I18n.translate("Module.KillAura.group.sgDelay"));

    // General

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .displayName(I18n.translate("Module.KillAura.setting.range.displayName"))
            .description(I18n.translate("Module.KillAura.setting.range.description"))
            .defaultValue(4)
            .min(0)
            .max(6)
            .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .displayName(I18n.translate("Module.KillAura.setting.entities.displayName"))
            .description(I18n.translate("Module.KillAura.setting.entities.description"))
            .defaultValue(new Object2BooleanOpenHashMap<>(0))
            .onlyAttackable()
            .build()
    );

    private final Setting<Priority> priority = sgGeneral.add(new EnumSetting.Builder<Priority>()
            .name("priority")
            .displayName(I18n.translate("Module.KillAura.setting.priority.displayName"))
            .description(I18n.translate("Module.KillAura.setting.priority.description"))
            .defaultValue(Priority.LowestHealth)
            .build()
    );

    private final Setting<OnlyWith> onlyWith = sgGeneral.add(new EnumSetting.Builder<OnlyWith>()
            .name("only-with")
            .displayName(I18n.translate("Module.KillAura.setting.onlyWith.displayName"))
            .description(I18n.translate("Module.KillAura.setting.onlyWith.description"))
            .defaultValue(OnlyWith.Any)
            .build()
    );

    private final Setting<Boolean> ignoreWalls = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-walls")
            .displayName(I18n.translate("Module.KillAura.setting.ignoreWalls.displayName"))
            .description(I18n.translate("Module.KillAura.setting.ignoreWalls.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder()
            .name("friends")
            .displayName(I18n.translate("Module.KillAura.setting.friends.displayName"))
            .description(I18n.translate("Module.KillAura.setting.friends.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> babies = sgGeneral.add(new BoolSetting.Builder()
            .name("babies")
            .displayName(I18n.translate("Module.KillAura.setting.babies.displayName"))
            .description(I18n.translate("Module.KillAura.setting.babies.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> nametagged = sgGeneral.add(new BoolSetting.Builder()
            .name("nametagged")
            .displayName(I18n.translate("Module.KillAura.setting.nametagged.displayName"))
            .description(I18n.translate("Module.KillAura.setting.nametagged.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> hitChance = sgGeneral.add(new DoubleSetting.Builder()
            .name("hit-chance")
            .displayName(I18n.translate("Module.KillAura.setting.hitChance.displayName"))
            .description(I18n.translate("Module.KillAura.setting.hitChance.description"))
            .defaultValue(100)
            .min(0)
            .max(100)
            .sliderMax(100)
            .build()
    );

    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-on-combat")
            .displayName(I18n.translate("Module.KillAura.setting.pauseOnCombat.displayName"))
            .description(I18n.translate("Module.KillAura.setting.pauseOnCombat.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<RotationMode> rotationMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
            .name("rotation-mode")
            .displayName(I18n.translate("Module.KillAura.setting.rotationMode.displayName"))
            .description(I18n.translate("Module.KillAura.setting.rotationMode.description"))
            .defaultValue(RotationMode.OnHit)
            .build()
    );

    private final Setting<RotationDirection> rotationDirection = sgGeneral.add(new EnumSetting.Builder<RotationDirection>()
            .name("rotation-direction")
            .displayName(I18n.translate("Module.KillAura.setting.rotationDirection.displayName"))
            .description(I18n.translate("Module.KillAura.setting.rotationDirection.description"))
            .defaultValue(RotationDirection.Eyes)
            .build()
    );

    // Delay

    private final Setting<Boolean> smartDelay = sgDelay.add(new BoolSetting.Builder()
            .name("smart-delay")
            .displayName(I18n.translate("Module.KillAura.setting.smartDelay.displayName"))
            .description(I18n.translate("Module.KillAura.setting.smartDelay.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> hitDelay = sgDelay.add(new IntSetting.Builder()
            .name("hit-delay")
            .displayName(I18n.translate("Module.KillAura.setting.hitDelay.displayName"))
            .description(I18n.translate("Module.KillAura.setting.hitDelay.description"))
            .defaultValue(0)
            .min(0)
            .sliderMax(60)
            .build()
    );

    private final Setting<Boolean> randomDelayEnabled = sgDelay.add(new BoolSetting.Builder()
            .name("random-delay-enabled")
            .displayName(I18n.translate("Module.KillAura.setting.randomDelayEnabled.displayName"))
            .description(I18n.translate("Module.KillAura.setting.randomDelayEnabled.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> randomDelayMax = sgDelay.add(new IntSetting.Builder()
            .name("random-delay-max")
            .displayName(I18n.translate("Module.KillAura.setting.randomDelayMax.displayName"))
            .description(I18n.translate("Module.KillAura.setting.randomDelayMax.description"))
            .defaultValue(4)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private int hitDelayTimer;
    private int randomDelayTimer;
    private Entity target;
    private boolean wasPathing;

    private final List<Entity> entityList = new ArrayList<>();

    public KillAura() {
        super(Category.Combat, "kill-aura", I18n.translate("Module.KillAura.description"));
    }

    @Override
    public void onDeactivate() {
        hitDelayTimer = 0;
        randomDelayTimer = 0;
        target = null;
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onPreTick = new Listener<>(event -> target = null);

    @EventHandler
    private final Listener<TickEvent.Post> onPostTick = new Listener<>(event -> {
        findEntity();
        if (target == null) return;

        if (rotationMode.get() == RotationMode.Always) packetRotate(target);
        attack(target);
    });

    public void packetRotate(Entity entity) {
        switch (rotationDirection.get()) {
            case Eyes:  RotationUtils.packetRotate(entity, Target.Head); break;
            case Chest: RotationUtils.packetRotate(entity); break;
            case Feet:  RotationUtils.packetRotate(entity, Target.Feet); break;
        }
    }

    private void attack(Entity entity) {
        if (entity == null) return;

        // Entities without health can be hit instantly
        if (entity instanceof LivingEntity) {
            if (smartDelay.get()) {
                if (mc.player.getAttackCooldownProgress(0.5f) < 1) return;
            }
            else {
                if (hitDelayTimer >= 0) {
                    hitDelayTimer--;
                    return;
                } else hitDelayTimer = hitDelay.get();
            }
        }

        if (randomDelayEnabled.get()) {
            if (randomDelayTimer > 0) {
                randomDelayTimer--;
                return;
            } else {
                randomDelayTimer = (int) Math.round(Math.random() * randomDelayMax.get());
            }
        }

        if (Math.random() > hitChance.get() / 100) return;

        if (rotationMode.get() == RotationMode.OnHit) packetRotate(entity);

        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void findEntity() {
        if (mc.player.isDead() || !mc.player.isAlive()) return;
        if (!itemInHand()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity == mc.cameraEntity) continue;
            if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) continue;
            if (entity.distanceTo(mc.player) > range.get()) continue;
            if (!entities.get().getBoolean(entity.getType())) continue;
            if (!nametagged.get() && entity.hasCustomName()) continue;
            if (!ignoreWalls.get() && PlayerUtils.canSeeEntity(entity)) continue;

            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isCreative()) continue;
                if (!friends.get() && !FriendManager.INSTANCE.attack((PlayerEntity) entity)) continue;
            }

            if (entity instanceof AnimalEntity && !babies.get() && ((AnimalEntity) entity).isBaby()) continue;

            entityList.add(entity);
        }

        if (entityList.size() > 0) {
            entityList.sort(this::sort);
            target = entityList.get(0);
            entityList.clear();

            if (pauseOnCombat.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && !wasPathing) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
                wasPathing = true;
            }
        } else {
            if (wasPathing){
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
                wasPathing = false;
            }
        }
    }

    private boolean itemInHand() {
        switch(onlyWith.get()){
            case Axe:        return mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Sword:      return mc.player.getMainHandStack().getItem() instanceof SwordItem;
            case SwordOrAxe: return mc.player.getMainHandStack().getItem() instanceof AxeItem || mc.player.getMainHandStack().getItem() instanceof SwordItem;
            default:         return true;
        }
    }

    private int sort(Entity e1, Entity e2) {
        switch (priority.get()) {
            case LowestDistance:  return Double.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player));
            case HighestDistance: return invertSort(Double.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player)));
            case LowestHealth:    return sortHealth(e1, e2);
            case HighestHealth:   return invertSort(sortHealth(e1, e2));
            default:              return 0;
        }
    }

    private int sortHealth(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l && e2l) return -1;

        return Float.compare(((LivingEntity) e1).getHealth(), ((LivingEntity) e2).getHealth());
    }

    private int invertSort(int sort) {
        if (sort == 0) return 0;
        return sort > 0 ? -1 : 1;
    }

    @Override
    public String getInfoString() {
        if (target != null && target instanceof PlayerEntity) return target.getEntityName();
        if (target != null) return target.getType().getName().getString();
        return null;
    }

        /*@EventHandler
    private final Listener<PacketEvent.Send> onSendPacket = new Listener<>(event -> {
        if (movePacket != null) return;

        if (event.packet instanceof PlayerMoveC2SPacket.PositionOnly) {
            event.cancel();

            PlayerMoveC2SPacket.PositionOnly p = (PlayerMoveC2SPacket.PositionOnly) event.packet;

            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Both(
                    p.getX(mc.player.getX()),
                    p.getY(mc.player.getY()),
                    p.getZ(mc.player.getZ()),
                    mc.player.yaw,
                    mc.player.pitch,
                    p.isOnGround()
            ));
        } else if (event.packet instanceof PlayerMoveC2SPacket) {
            movePacket = (PlayerMoveC2SPacket) event.packet;

            rotatePacket();
        }
    });*/

    /*@EventHandler
    private final Listener<PacketSentEvent> onPacketSent = new Listener<>(event -> {
        if (event.packet == movePacket) attack();
    });

    @EventHandler
    private final Listener<TickEvent.Post> onPostTick = new Listener<>(event -> {
        if (movePacket == null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookOnly(
                    mc.player.yaw,
                    mc.player.pitch,
                    mc.player.isOnGround()
            ));
        }
    });*/
}

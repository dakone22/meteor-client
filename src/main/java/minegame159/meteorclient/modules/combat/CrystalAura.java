/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.zero.alpine.event.EventPriority;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.EntityRemovedEvent;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.misc.Pool;
import minegame159.meteorclient.utils.misc.SafetyMode;
import minegame159.meteorclient.utils.player.DamageCalcUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.*;

public class CrystalAura extends Module {
    public enum TargetMode {
        MostDamage,
        HighestXDamages
    }

    public enum RotationMode {
        None,
        FaceCrystal,
        Return
    }

    public enum SwitchMode {
        Auto,
        Spoof,
        None
    }

    private final SettingGroup sgPlace = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgPlace"));
    private final SettingGroup sgBreak = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgBreak"));
    private final SettingGroup sgTarget = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgTarget"));
    private final SettingGroup sgPause = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgPause"));
    private final SettingGroup sgMisc = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgMisc"));
    private final SettingGroup sgRender = settings.createGroup(I18n.translate("Module.CrystalAura.group.sgRender"));

    // Place

    private final Setting<Integer> placeDelay = sgPlace.add(new IntSetting.Builder()
            .name("place-delay")
            .displayName(I18n.translate("Module.CrystalAura.setting.placeDelay.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.placeDelay.description"))
            .defaultValue(2)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<SafetyMode> placeMode = sgPlace.add(new EnumSetting.Builder<SafetyMode>()
            .name("place-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.placeMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.placeMode.description"))
            .defaultValue(SafetyMode.Safe)
            .build()
    );

    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
            .name("place-range")
            .displayName(I18n.translate("Module.CrystalAura.setting.placeRange.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.placeRange.description"))
            .defaultValue(4.5)
            .min(0)
            .sliderMax(7)
            .build()
    );

    private final Setting<Boolean> place = sgPlace.add(new BoolSetting.Builder()
            .name("place")
            .displayName(I18n.translate("Module.CrystalAura.setting.place.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.place.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> minDamage = sgPlace.add(new DoubleSetting.Builder()
            .name("min-damage")
            .displayName(I18n.translate("Module.CrystalAura.setting.minDamage.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.minDamage.description"))
            .defaultValue(5.5)
            .build()
    );

    private final Setting<Double> minHealth = sgPlace.add(new DoubleSetting.Builder()
            .name("min-health")
            .displayName(I18n.translate("Module.CrystalAura.setting.minHealth.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.minHealth.description"))
            .defaultValue(15)
            .build()
    );

    private final Setting<Boolean> surroundBreak = sgPlace.add(new BoolSetting.Builder()
            .name("surround-break")
            .displayName(I18n.translate("Module.CrystalAura.setting.surroundBreak.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.surroundBreak.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> surroundHold = sgPlace.add(new BoolSetting.Builder()
            .name("surround-hold")
            .displayName(I18n.translate("Module.CrystalAura.setting.surroundHold.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.surroundHold.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> strict = sgPlace.add(new BoolSetting.Builder()
            .name("strict")
            .displayName(I18n.translate("Module.CrystalAura.setting.strict.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.strict.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreWalls = sgPlace.add(new BoolSetting.Builder()
            .name("ignore-walls")
            .displayName(I18n.translate("Module.CrystalAura.setting.ignoreWalls.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.ignoreWalls.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> facePlace = sgPlace.add(new BoolSetting.Builder()
            .name("face-place")
            .displayName(I18n.translate("Module.CrystalAura.setting.facePlace.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.facePlace.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> spamFacePlace = sgPlace.add(new BoolSetting.Builder()
            .name("spam-face-place")
            .displayName(I18n.translate("Module.CrystalAura.setting.spamFacePlace.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.spamFacePlace.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> facePlaceHealth = sgPlace.add(new DoubleSetting.Builder()
            .name("face-place-health")
            .displayName(I18n.translate("Module.CrystalAura.setting.facePlaceHealth.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.facePlaceHealth.description"))
            .defaultValue(8)
            .min(1)
            .max(36)
            .build()
    );

    private final Setting<Double> facePlaceDurability = sgPlace.add(new DoubleSetting.Builder()
            .name("face-place-durability")
            .displayName(I18n.translate("Module.CrystalAura.setting.facePlaceDurability.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.facePlaceDurability.description"))
            .defaultValue(2)
            .min(1)
            .max(100)
            .sliderMax(100)
            .build()
    );

    private final Setting<Boolean> support = sgPlace.add(new BoolSetting.Builder()
            .name("support")
            .displayName(I18n.translate("Module.CrystalAura.setting.support.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.support.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> supportDelay = sgPlace.add(new IntSetting.Builder()
            .name("support-delay")
            .displayName(I18n.translate("Module.CrystalAura.setting.supportDelay.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.supportDelay.description"))
            .defaultValue(5)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> supportBackup = sgPlace.add(new BoolSetting.Builder()
            .name("support-backup")
            .displayName(I18n.translate("Module.CrystalAura.setting.supportBackup.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.supportBackup.description"))
            .defaultValue(true)
            .build()
    );

    // Break

    private final Setting<Integer> breakDelay = sgBreak.add(new IntSetting.Builder()
            .name("break-delay")
            .displayName(I18n.translate("Module.CrystalAura.setting.breakDelay.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.breakDelay.description"))
            .defaultValue(1)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<SafetyMode> breakMode = sgBreak.add(new EnumSetting.Builder<SafetyMode>()
            .name("break-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.breakMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.breakMode.description"))
            .defaultValue(SafetyMode.Safe)
            .build()
    );

    private final Setting<Double> breakRange = sgBreak.add(new DoubleSetting.Builder()
            .name("break-range")
            .displayName(I18n.translate("Module.CrystalAura.setting.breakRange.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.breakRange.description"))
            .defaultValue(5)
            .min(0)
            .sliderMax(7)
            .build()
    );

    // Target

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgTarget.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .displayName(I18n.translate("Module.CrystalAura.setting.entities.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.entities.description"))
            .defaultValue(Utils.asObject2BooleanOpenHashMap(EntityType.PLAYER))
            .onlyAttackable()
            .build()
    );

    private final Setting<Double> targetRange = sgTarget.add(new DoubleSetting.Builder()
            .name("target-range")
            .displayName(I18n.translate("Module.CrystalAura.setting.targetRange.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.targetRange.description"))
            .defaultValue(7)
            .min(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<TargetMode> targetMode = sgTarget.add(new EnumSetting.Builder<TargetMode>()
            .name("target-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.targetMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.targetMode.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.CrystalAura.enum.TargetMode.MostDamage"),
                    I18n.translate("Module.CrystalAura.enum.TargetMode.HighestXDamages"),
            })
            .defaultValue(TargetMode.HighestXDamages)
            .build()
    );

    private final Setting<Integer> numberOfDamages = sgTarget.add(new IntSetting.Builder()
            .name("number-of-damages")
            .displayName(I18n.translate("Module.CrystalAura.setting.numberOfDamages.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.numberOfDamages.description"))
            .defaultValue(3)
            .min(2)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> multiTarget = sgTarget.add(new BoolSetting.Builder()
            .name("multi-targeting")
            .displayName(I18n.translate("Module.CrystalAura.setting.multiTarget.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.multiTarget.description"))
            .defaultValue(false)
            .build()
    );

    // Pause

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-eat")
            .displayName(I18n.translate("Module.CrystalAura.setting.pauseOnEat.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.pauseOnEat.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-drink")
            .displayName(I18n.translate("Module.CrystalAura.setting.pauseOnDrink.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.pauseOnDrink.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder()
            .name("pause-on-mine")
            .displayName(I18n.translate("Module.CrystalAura.setting.pauseOnMine.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.pauseOnMine.description"))
            .defaultValue(false)
            .build()
    );

    // Misc

    private final Setting<Double> maxDamage = sgMisc.add(new DoubleSetting.Builder()
            .name("max-damage")
            .displayName(I18n.translate("Module.CrystalAura.setting.maxDamage.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.maxDamage.description"))
            .defaultValue(3)
            .build()
    );

    private final Setting<RotationMode> rotationMode = sgMisc.add(new EnumSetting.Builder<RotationMode>()
            .name("rotation-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.rotationMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.rotationMode.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.CrystalAura.enum.RotationMode.None"),
                    I18n.translate("Module.CrystalAura.enum.RotationMode.FaceCrystal"),
                    I18n.translate("Module.CrystalAura.enum.RotationMode.Return"),
            })
            .defaultValue(RotationMode.FaceCrystal)
            .build()
    );

    private final Setting<SwitchMode> switchMode = sgMisc.add(new EnumSetting.Builder<SwitchMode>()
            .name("switch-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.switchMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.switchMode.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.CrystalAura.enum.SwitchMode.Auto"),
                    I18n.translate("Module.CrystalAura.enum.SwitchMode.Spoof"),
                    I18n.translate("Module.CrystalAura.enum.SwitchMode.None"),
            })
            .defaultValue(SwitchMode.Auto)
            .build()
    );

    private final Setting<Boolean> smartDelay = sgMisc.add(new BoolSetting.Builder()
            .name("smart-delay")
            .displayName(I18n.translate("Module.CrystalAura.setting.smartDelay.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.smartDelay.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> healthDifference = sgMisc.add(new DoubleSetting.Builder()
            .name("damage-increase")
            .displayName(I18n.translate("Module.CrystalAura.setting.healthDifference.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.healthDifference.description"))
            .defaultValue(5)
            .min(0)
            .max(20)
            .build()
    );

    private final Setting<Boolean> antiWeakness = sgMisc.add(new BoolSetting.Builder()
            .name("anti-weakness")
            .displayName(I18n.translate("Module.CrystalAura.setting.antiWeakness.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.antiWeakness.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noSwing = sgMisc.add(new BoolSetting.Builder()
            .name("no-swing")
            .displayName(I18n.translate("Module.CrystalAura.setting.noSwing.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.noSwing.description"))
            .defaultValue(true)
            .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .displayName(I18n.translate("Module.CrystalAura.setting.render.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.render.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Module.CrystalAura.setting.shapeMode.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Lines)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .displayName(I18n.translate("Module.CrystalAura.setting.sideColor.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.sideColor.description"))
            .defaultValue(new SettingColor(255, 255, 255, 75))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .displayName(I18n.translate("Module.CrystalAura.setting.lineColor.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.lineColor.description"))
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    private final Setting<Integer> renderTimer = sgRender.add(new IntSetting.Builder()
            .name("timer")
            .displayName(I18n.translate("Module.CrystalAura.setting.renderTimer.displayName"))
            .description(I18n.translate("Module.CrystalAura.setting.renderTimer.description"))
            .defaultValue(0)
            .min(0)
            .sliderMax(10)
            .build()
    );

    public CrystalAura() {
        super(Category.Combat, "crystal-aura", I18n.translate("Module.CrystalAura.description"));
    }

    private int preSlot;
    private int placeDelayLeft = placeDelay.get();
    private int breakDelayLeft = breakDelay.get();
    private Vec3d bestBlock;
    private double bestDamage = 0;
    private double lastDamage = 0;
    private EndCrystalEntity heldCrystal = null;
    private LivingEntity target;
    private boolean locked = false;
    private boolean canSupport;
    private int supportSlot = 0;
    private int supportDelayLeft = supportDelay.get();
    private final Map<EndCrystalEntity, List<Double>> crystalMap = new HashMap<>();
    private final List<Double> crystalList = new ArrayList<>();
    private EndCrystalEntity bestBreak = null;

    private final Pool<RenderBlock> renderBlockPool = new Pool<>(RenderBlock::new);
    private final List<RenderBlock> renderBlocks = new ArrayList<>();

    @Override
    public void onActivate() {
        preSlot = -1;
        placeDelayLeft = 0;
        breakDelayLeft = 0;
        heldCrystal = null;
        locked = false;
    }

    @Override
    public void onDeactivate() {
        assert mc.player != null;
        if (preSlot != -1) mc.player.inventory.selectedSlot = preSlot;
        for (RenderBlock renderBlock : renderBlocks) {
            renderBlockPool.free(renderBlock);
        }
        renderBlocks.clear();
    }

    @EventHandler
    private final Listener<EntityRemovedEvent> onEntityRemoved = new Listener<>(event -> {
        if (heldCrystal == null) return;
        if (event.entity.getBlockPos().equals(heldCrystal.getBlockPos())) {
            heldCrystal = null;
            locked = false;
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        assert mc.player != null;
        assert mc.world != null;
        for (Iterator<RenderBlock> it = renderBlocks.iterator(); it.hasNext();) {
            RenderBlock renderBlock = it.next();

            if (renderBlock.shouldRemove()) {
                it.remove();
                renderBlockPool.free(renderBlock);
            }
        }

        placeDelayLeft --;
        breakDelayLeft --;
        supportDelayLeft --;
        if (target == null) {
            heldCrystal = null;
            locked = false;
        }

        if ((mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem().isFood() || mc.player.getOffHandStack().getItem().isFood()) && pauseOnEat.get())
                || (mc.interactionManager.isBreakingBlock() && pauseOnMine.get())
                || (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() instanceof PotionItem || mc.player.getOffHandStack().getItem() instanceof PotionItem) && pauseOnDrink.get())) {
            return;
        }

        if (locked && heldCrystal != null && ((!surroundBreak.get()
                && target.getBlockPos().getSquaredDistance(new Vec3i(heldCrystal.getX(), heldCrystal.getY(), heldCrystal.getZ())) == 4d) || (!surroundHold.get()
                && target.getBlockPos().getSquaredDistance(new Vec3i(heldCrystal.getX(), heldCrystal.getY(), heldCrystal.getZ())) == 2d))){
            heldCrystal = null;
            locked = false;
        }
        if (heldCrystal != null && mc.player.distanceTo(heldCrystal) > breakRange.get()) {
            heldCrystal = null;
            locked = false;
        }
        boolean isThere = false;
        if (heldCrystal != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity)) continue;
                if (heldCrystal != null && entity.getBlockPos().equals(heldCrystal.getBlockPos())) {
                    isThere = true;
                    break;
                }
            }
            if (!isThere){
                heldCrystal = null;
                locked = false;
            }
        }
        boolean shouldFacePlace = false;
        if (getTotalHealth(mc.player) <= minHealth.get() && placeMode.get() != SafetyMode.Suicide) return;
        if (target != null && heldCrystal != null && placeDelayLeft <= 0 && mc.world.raycast(new RaycastContext(target.getPos(), heldCrystal.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, target)).getType()
                == HitResult.Type.MISS) locked = false;
        if (heldCrystal == null) locked = false;
        if (locked && !facePlace.get()) return;

        if (!multiTarget.get()) {
            findTarget();
            if (target == null) return;
            if (breakDelayLeft <= 0) {
                singleBreak();
            }
        } else if (breakDelayLeft <= 0){
            multiBreak();
        }

        if (!smartDelay.get() && placeDelayLeft > 0 && ((!surroundHold.get() && (target != null && (!surroundBreak.get() || !isSurrounded(target)))) || heldCrystal != null) && (!spamFacePlace.get())) return;
        if (switchMode.get() == SwitchMode.None && mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) return;
        if (place.get()) {
            if (target == null) return;
            if (surroundHold.get() && heldCrystal == null){
                int slot = InvUtils.findItemWithCount(Items.END_CRYSTAL).slot;
                if ((slot != -1 && slot < 9) || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
                    bestBlock = findOpen(target);
                    if (bestBlock != null) {
                        doHeldCrystal();
                        return;
                    }
                }
            }
            if (surroundBreak.get() && heldCrystal == null && isSurrounded(target)){
                int slot = InvUtils.findItemWithCount(Items.END_CRYSTAL).slot;
                if ((slot != -1 && slot < 9) || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
                    bestBlock = findOpenSurround(target);
                    if (bestBlock != null) {
                        doHeldCrystal();
                        return;
                    }
                }
            }
            int slot = InvUtils.findItemWithCount(Items.END_CRYSTAL).slot;
            if ((slot == -1 || slot > 9) && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                return;
            }
            findValidBlocks(target);
            if (bestBlock == null) {
                findFacePlace(target);
            }
            if (bestBlock == null) return;
            if (facePlace.get() && Math.sqrt(target.squaredDistanceTo(bestBlock)) <= 2) {
                if (target.getHealth() + target.getAbsorptionAmount() < facePlaceHealth.get()) {
                    shouldFacePlace = true;
                } else {
                    Iterable<ItemStack> armourItems = target.getArmorItems();
                    for (ItemStack itemStack : armourItems){
                        if (itemStack == null) continue;
                        if (!itemStack.isEmpty() && (((double)(itemStack.getMaxDamage() - itemStack.getDamage()) / itemStack.getMaxDamage()) * 100) <= facePlaceDurability.get()){
                            shouldFacePlace = true;
                        }
                    }
                }
            }
            if (bestBlock != null && ((bestDamage >= minDamage.get() && !locked) || shouldFacePlace)) {
                if (switchMode.get() != SwitchMode.None) doSwitch();
                if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) return;
                if (!smartDelay.get()) {
                    placeDelayLeft = placeDelay.get();
                    placeBlock(bestBlock, getHand());
                }else if (smartDelay.get() && (placeDelayLeft <= 0 || bestDamage - lastDamage > healthDifference.get()
                        || (spamFacePlace.get() && shouldFacePlace))) {
                    lastDamage = bestDamage;
                    placeBlock(bestBlock, getHand());
                    if (placeDelayLeft <= 0) placeDelayLeft = 10;
                }
            }
            if (switchMode.get() == SwitchMode.Spoof && preSlot != mc.player.inventory.selectedSlot && preSlot != -1)
                mc.player.inventory.selectedSlot = preSlot;
        }
    }, EventPriority.HIGH);

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        if (render.get()) {
            for (RenderBlock renderBlock : renderBlocks) {
                renderBlock.render();
            }
        }
    });

    private void singleBreak(){
        assert mc.player != null;
        assert mc.world != null;
        Streams.stream(mc.world.getEntities())
                .filter(entity -> entity instanceof EndCrystalEntity)
                .filter(entity -> entity.distanceTo(mc.player) <= breakRange.get())
                .filter(Entity::isAlive)
                .filter(entity -> shouldBreak((EndCrystalEntity) entity))
                .filter(entity -> ignoreWalls.get() || mc.player.canSee(entity))
                .filter(entity -> isSafe(entity.getPos()))
                .max(Comparator.comparingDouble(o -> DamageCalcUtils.crystalDamage(target, o.getPos())))
                .ifPresent(entity -> hitCrystal((EndCrystalEntity) entity));
    }

    private void multiBreak(){
        assert mc.world != null;
        assert mc.player != null;
        crystalMap.clear();
        crystalList.clear();
        Streams.stream(mc.world.getEntities())
                .filter(entity -> entity instanceof EndCrystalEntity)
                .filter(entity -> entity.distanceTo(mc.player) <= breakRange.get())
                .filter(Entity::isAlive)
                .filter(entity -> shouldBreak((EndCrystalEntity) entity))
                .filter(entity -> ignoreWalls.get() || mc.player.canSee(entity))
                .filter(entity -> !isSafe(entity.getPos()))
                .forEach(entity -> {
                    for (Entity target : mc.world.getEntities()){
                        if (target != mc.player && entities.get().getBoolean(target.getType()) && mc.player.distanceTo(target) <= targetRange.get()
                                && target.isAlive() && target instanceof LivingEntity
                                && (!(target instanceof PlayerEntity) || FriendManager.INSTANCE.attack((PlayerEntity) target))){
                            crystalList.add(DamageCalcUtils.crystalDamage((LivingEntity) target, entity.getPos()));
                        }
                    }
                    if (!crystalList.isEmpty()) {
                        crystalList.sort(Comparator.comparingDouble(Double::doubleValue));
                        crystalMap.put((EndCrystalEntity) entity, new ArrayList<>(crystalList));
                        crystalList.clear();
                    }
                });
        EndCrystalEntity crystal = findBestCrystal(crystalMap);
        if (crystal != null) {
            hitCrystal(crystal);
        }
    }

    private EndCrystalEntity findBestCrystal(Map<EndCrystalEntity, List<Double>> map){
        bestDamage = 0;
        double currentDamage = 0;
        if (targetMode.get() == TargetMode.HighestXDamages){
            for (Map.Entry<EndCrystalEntity, List<Double>> entry : map.entrySet()){
                for (int i = 0; i < entry.getValue().size() && i < numberOfDamages.get(); i++){
                    currentDamage += entry.getValue().get(i);
                }
                if (bestDamage < currentDamage) {
                    bestDamage = currentDamage;
                    bestBreak = entry.getKey();
                }
                currentDamage = 0;
            }
        } else if (targetMode.get() == TargetMode.MostDamage){
            for (Map.Entry<EndCrystalEntity, List<Double>> entry : map.entrySet()){
                for (int i = 0; i < entry.getValue().size(); i++){
                    currentDamage += entry.getValue().get(i);
                }
                if (bestDamage < currentDamage) {
                    bestDamage = currentDamage;
                    bestBreak = entry.getKey();
                }
                currentDamage = 0;
            }
        }
        return bestBreak;
    }

    private void hitCrystal(EndCrystalEntity entity){
        assert mc.player != null;
        assert mc.world != null;
        assert mc.interactionManager != null;
        int preSlot = mc.player.inventory.selectedSlot;
        if (mc.player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS) && antiWeakness.get()) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.inventory.getStack(i).getItem() instanceof SwordItem || mc.player.inventory.getStack(i).getItem() instanceof AxeItem) {
                    mc.player.inventory.selectedSlot = i;
                    break;
                }
            }
        }

        if (rotationMode.get() == RotationMode.FaceCrystal || rotationMode.get() == RotationMode.Return) RotationUtils.packetRotate(entity);
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.world.removeEntity(entity.getEntityId());
        if (!noSwing.get()) mc.player.swingHand(getHand());
        mc.player.inventory.selectedSlot = preSlot;
        if (heldCrystal != null && entity.getBlockPos().equals(heldCrystal.getBlockPos())) {
            heldCrystal = null;
            locked = false;
        }
        breakDelayLeft = breakDelay.get();
    }

    private void findTarget(){
        assert  mc.world != null;
        Optional<LivingEntity> livingEntity = Streams.stream(mc.world.getEntities())
                .filter(Entity::isAlive)
                .filter(entity -> entity != mc.player)
                .filter(entity -> !(entity instanceof PlayerEntity) || FriendManager.INSTANCE.attack((PlayerEntity) entity))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entities.get().getBoolean(entity.getType()))
                .filter(entity -> entity.distanceTo(mc.player) <= targetRange.get() * 2)
                .min(Comparator.comparingDouble(o -> o.distanceTo(mc.player)))
                .map(entity -> (LivingEntity) entity);
        if (!livingEntity.isPresent()) {
            target = null;
            return;
        }
        target = livingEntity.get();
    }

    private void doSwitch(){
        assert mc.player != null;
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            int slot = InvUtils.findItemWithCount(Items.END_CRYSTAL).slot;
            if (slot != -1 && slot < 9) {
                preSlot = mc.player.inventory.selectedSlot;
                mc.player.inventory.selectedSlot = slot;
            }
        }
    }

    private void doHeldCrystal(){
        assert mc.player != null;
        if (switchMode.get() != SwitchMode.None) doSwitch();
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) return;
        bestDamage = DamageCalcUtils.crystalDamage(target, bestBlock.add(0, 1, 0));
        heldCrystal = new EndCrystalEntity(mc.world, bestBlock.x, bestBlock.y + 1, bestBlock.z);
        locked = true;
        if (!smartDelay.get()) {
            placeDelayLeft = placeDelay.get();
        } else {
            lastDamage = bestDamage;
            if (placeDelayLeft <= 0) placeDelayLeft = 10;
        }
        placeBlock(bestBlock, getHand());
    }

    private void placeBlock(Vec3d block, Hand hand){
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        if (mc.world.isAir(new BlockPos(block))) {
            PlayerUtils.placeBlock(new BlockPos(block), supportSlot, Hand.MAIN_HAND);
            supportDelayLeft = supportDelay.get();
        }
        float yaw = mc.player.yaw;
        float pitch = mc.player.pitch;
        if (rotationMode.get() == RotationMode.FaceCrystal || rotationMode.get() == RotationMode.Return) RotationUtils.packetRotate(block.add(0.5, 0.5, 0.5));
        mc.interactionManager.interactBlock(mc.player, mc.world, hand, new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(block), false));
        if (!noSwing.get()) mc.player.swingHand(hand);
        if (rotationMode.get() == RotationMode.Return)RotationUtils.packetRotate(yaw, pitch);

        if (render.get()) {
            RenderBlock renderBlock = renderBlockPool.get();
            renderBlock.reset(block);
            renderBlocks.add(renderBlock);
        }
    }

    private void findValidBlocks(LivingEntity target){
        assert mc.player != null;
        assert mc.world != null;
        bestBlock = new Vec3d(0, 0, 0);
        bestDamage = 0;
        Vec3d bestSupportBlock = new Vec3d(0, 0, 0);
        double bestSupportDamage = 0;
        BlockPos playerPos = mc.player.getBlockPos();
        canSupport = false;
        crystalMap.clear();
        crystalList.clear();
        if (support.get()){
            for (int i = 0; i < 9; i++){
                if (mc.player.inventory.getStack(i).getItem() == Items.OBSIDIAN){
                    canSupport = true;
                    supportSlot = i;
                    break;
                }
            }
        }
        for(double i = playerPos.getX() - placeRange.get(); i < playerPos.getX() + placeRange.get(); i++){
            for(double j = playerPos.getZ() - placeRange.get(); j < playerPos.getZ() + placeRange.get(); j++){
                for(double k = playerPos.getY() - 3; k < playerPos.getY() + 3; k++){
                    Vec3d pos = new Vec3d(Math.floor(i), Math.floor(k), Math.floor(j));
                    if(isValid(new BlockPos(pos)) && getDamagePlace(new BlockPos(pos))){
                        if (!strict.get() || isEmpty(new BlockPos(pos.add(0, 2, 0)))) {
                            if (!multiTarget.get()) {
                                if (isEmpty(new BlockPos(pos)) && bestSupportDamage < DamageCalcUtils.crystalDamage(target, pos.add(0.5, 1, 0.5))){
                                    bestSupportBlock = pos;
                                    bestSupportDamage = DamageCalcUtils.crystalDamage(target, pos.add(0.5, 1, 0.5));
                                }else if (!isEmpty(new BlockPos(pos)) && bestDamage < DamageCalcUtils.crystalDamage(target, pos.add(0.5, 1, 0.5))) {
                                    bestBlock = pos;
                                    bestDamage = DamageCalcUtils.crystalDamage(target, bestBlock.add(0.5, 1, 0.5));
                                }
                            } else {
                                for (Entity entity : mc.world.getEntities()){
                                    if (entity != mc.player && entities.get().getBoolean(entity.getType()) && mc.player.distanceTo(entity) <= targetRange.get()
                                            && entity.isAlive() && entity instanceof LivingEntity
                                            && (!(entity instanceof PlayerEntity) || FriendManager.INSTANCE.attack((PlayerEntity) entity))){
                                        crystalList.add(DamageCalcUtils.crystalDamage((LivingEntity) entity, pos.add(0.5, 1, 0.5)));
                                    }
                                }
                                if (!crystalList.isEmpty()) {
                                    crystalList.sort(Comparator.comparingDouble(Double::doubleValue));
                                    crystalMap.put(new EndCrystalEntity(mc.world, pos.x, pos.y, pos.z), new ArrayList<>(crystalList));
                                    crystalList.clear();
                                }
                            }
                        }
                    }
                }
            }
        }
        if (multiTarget.get()){
            EndCrystalEntity entity = findBestCrystal(crystalMap);
            if (entity != null && bestDamage > minDamage.get()){
                bestBlock = entity.getPos();
            } else {
                bestBlock = null;
            }
        } else {
            if (bestDamage < minDamage.get()) bestBlock = null;
        }
        if (support.get() && (bestBlock == null || (bestDamage < bestSupportDamage && !supportBackup.get()))){
            bestBlock = bestSupportBlock;
        }
    }

    private void findFacePlace(LivingEntity target){
        assert mc.world != null;
        assert mc.player != null;
        BlockPos targetBlockPos = target.getBlockPos();
        if (mc.world.getBlockState(targetBlockPos.add(1, 1, 0)).isAir() && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(targetBlockPos.add(1, 1, 0))) <= placeRange.get()
                && getDamagePlace(targetBlockPos.add(1, 1, 0))) {
            bestBlock = target.getPos().add(1, 0, 0);
        } else if (mc.world.getBlockState(targetBlockPos.add(-1, 1, 0)).isAir() && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(targetBlockPos.add(-1, 1, 0))) <= placeRange.get()
                && getDamagePlace(targetBlockPos.add(-1, 1, 0))) {
            bestBlock = target.getPos().add(-1, 0, 0);
        } else if (mc.world.getBlockState(targetBlockPos.add(0, 1, 1)).isAir() && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(targetBlockPos.add(0, 1, 1))) <= placeRange.get()
                && getDamagePlace(targetBlockPos.add(0, 1, 1))) {
            bestBlock = target.getPos().add(0, 0, 1);
        } else if (mc.world.getBlockState(targetBlockPos.add(0, 1, -1)).isAir() && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(targetBlockPos.add(0, 1, -1))) <= placeRange.get()
                && getDamagePlace(targetBlockPos.add(0, 1, -1))) {
            bestBlock = target.getPos().add(0, 0, -1);
        }
    }

    private boolean getDamagePlace(BlockPos pos){
        assert mc.player != null;
        return placeMode.get() == SafetyMode.Suicide || DamageCalcUtils.crystalDamage(mc.player, new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) <= maxDamage.get();
    }

    private Vec3d findOpen(LivingEntity target){
        assert mc.player != null;
        int x = 0;
        int z = 0;
        if (isValid(target.getBlockPos().add(1, -1, 0))
                && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(new Vec3i(target.getBlockPos().getX() + 1, target.getBlockPos().getY() - 1, target.getBlockPos().getZ()))) < placeRange.get()){
            x = 1;
        } else if (isValid(target.getBlockPos().add(-1, -1, 0))
                && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(new Vec3i(target.getBlockPos().getX() -1, target.getBlockPos().getY() - 1, target.getBlockPos().getZ()))) < placeRange.get()){
            x = -1;
        } else if (isValid(target.getBlockPos().add(0, -1, 1))
                && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(new Vec3i(target.getBlockPos().getX(), target.getBlockPos().getY() - 1, target.getBlockPos().getZ() + 1))) < placeRange.get()){
            z = 1;
        } else if (isValid(target.getBlockPos().add(0, -1, -1))
                && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(new Vec3i(target.getBlockPos().getX(), target.getBlockPos().getY() - 1, target.getBlockPos().getZ() - 1))) < placeRange.get()){
            z = -1;
        }
        if (x != 0 || z != 0) {
            return new Vec3d(target.getBlockPos().getX() + 0.5 + x, target.getBlockPos().getY() - 1, target.getBlockPos().getZ() + 0.5 + z);
        }
        return null;
    }

    private Vec3d findOpenSurround(LivingEntity target){
        assert mc.player != null;
        assert mc.world != null;

        int x = 0;
        int z = 0;
        if (validSurroundBreak(target, 2, 0)){
            x = 2;
        } else if (validSurroundBreak(target, -2, 0)){
            x = -2;
        } else if (validSurroundBreak(target, 0, 2)){
            z = 2;
        } else if (validSurroundBreak(target, 0, -2)){
            z = -2;
        }
        if (x != 0 || z != 0) {
            return new Vec3d(target.getBlockPos().getX() + 0.5 + x, target.getBlockPos().getY() - 1, target.getBlockPos().getZ() + 0.5 + z);
        }
        return null;
    }

    private boolean isValid(BlockPos blockPos){
        assert mc.world != null;
        return (((canSupport && isEmpty(blockPos) && blockPos.getY() - target.getBlockPos().getY() == -1 && supportDelayLeft <= 0) || (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN))
                && isEmpty(blockPos.add(0, 1, 0)));
    }

    private boolean validSurroundBreak(LivingEntity target, int x, int z) {
        assert mc.world != null;
        assert mc.player != null;
        Vec3d crystalPos = new Vec3d(target.getBlockPos().getX() + 0.5, target.getBlockPos().getY(), target.getBlockPos().getZ() + 0.5);
        return isValid(target.getBlockPos().add(x, -1, z)) && mc.world.getBlockState(target.getBlockPos().add(x/2, 0, z/2)).getBlock() != Blocks.BEDROCK
                && isSafe(crystalPos.add(x, 0, z))
                && Math.sqrt(mc.player.getBlockPos().getSquaredDistance(new Vec3i(target.getBlockPos().getX() + x, target.getBlockPos().getY() - 1, target.getBlockPos().getZ() + z))) < placeRange.get()
                && mc.world.raycast(new RaycastContext(target.getPos(), target.getPos().add(x, 0, z), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, target)).getType()
                != HitResult.Type.MISS;
    }

    private boolean isSafe(Vec3d crystalPos){
        assert mc.player != null;
        return (!(breakMode.get() == SafetyMode.Safe) || (getTotalHealth(mc.player) - DamageCalcUtils.crystalDamage(mc.player, crystalPos) > minHealth.get()
                && DamageCalcUtils.crystalDamage(mc.player, crystalPos) < maxDamage.get()));
    }

    private float getTotalHealth(PlayerEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    private boolean isEmpty(BlockPos pos) {
        assert mc.world != null;
        return mc.world.getBlockState(pos).isAir() && mc.world.getOtherEntities(null, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D)).isEmpty();
    }

    private class RenderBlock {
        private int x, y, z;
        private int timer;

        public void reset(Vec3d pos) {
            x = MathHelper.floor(pos.getX());
            y = MathHelper.floor(pos.getY());
            z = MathHelper.floor(pos.getZ());
            timer = renderTimer.get();
        }

        public boolean shouldRemove() {
            if (timer <= 0) return true;
            timer--;
            return false;
        }

        public void render() {
            Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x, y, z, 1, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private boolean shouldBreak(EndCrystalEntity entity){
        assert mc.world != null;
        return (heldCrystal == null || (!surroundHold.get() && !surroundBreak.get())) || (placeDelayLeft <= 0 && (!heldCrystal.getBlockPos().equals(entity.getBlockPos()) || mc.world.raycast(new RaycastContext(target.getPos(), heldCrystal.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, target)).getType()
                == HitResult.Type.MISS || (target.distanceTo(heldCrystal) > 1.5 && !isSurrounded(target))));
    }

    private boolean isSurrounded(LivingEntity target){
        assert mc.world != null;
        return !mc.world.getBlockState(target.getBlockPos().add(1, 0, 0)).isAir()
                && !mc.world.getBlockState(target.getBlockPos().add(-1, 0, 0)).isAir()
                && !mc.world.getBlockState(target.getBlockPos().add(0, 0, 1)).isAir() &&
                !mc.world.getBlockState(target.getBlockPos().add(0, 0, -1)).isAir();
    }

    public Hand getHand() {
        assert mc.player != null;
        Hand hand = Hand.MAIN_HAND;
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            hand = Hand.OFF_HAND;
        }
        return hand;
    }

    @Override
    public String getInfoString() {
        if (target != null && target instanceof PlayerEntity) return target.getEntityName();
        if (target != null) return target.getType().getName().getString();
        return null;
    }
}

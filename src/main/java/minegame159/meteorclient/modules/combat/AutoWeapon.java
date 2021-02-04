/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

//Updated by squidoodly 15/06/2020

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.player.AttackEntityEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;

public class AutoWeapon extends Module {
    public enum Weapon{
        Sword,
        Axe
    }

    public AutoWeapon(){
        super(Category.Combat, "auto-weapon", I18n.translate("Module.AutoWeapon.description"));
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Weapon> weapon = sgGeneral.add(new EnumSetting.Builder<Weapon>()
            .name("weapon")
            .displayName(I18n.translate("Module.AutoWeapon.setting.weapon.displayName"))
            .description(I18n.translate("Module.AutoWeapon.setting.weapon.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.AutoWeapon.enum.Weapon.Sword"),
                    I18n.translate("Module.AutoWeapon.enum.Weapon.Axe"),
            })
            .defaultValue(Weapon.Sword)
            .build()
    );

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
            .name("threshold")
            .displayName(I18n.translate("Module.AutoWeapon.setting.threshold.displayName"))
            .description(I18n.translate("Module.AutoWeapon.setting.threshold.description"))
            .defaultValue(4)
            .build()
    );

    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-break")
            .displayName(I18n.translate("Module.AutoWeapon.setting.antiBreak.displayName"))
            .description(I18n.translate("Module.AutoWeapon.setting.antiBreak.description"))
            .defaultValue(false)
            .build()
    );

    @EventHandler
    private final Listener<AttackEntityEvent> onAttack = new Listener<>(event -> mc.player.inventory.selectedSlot = getBestWeapon());

    private int getBestWeapon(){
        int slotS = mc.player.inventory.selectedSlot;
        int slotA = mc.player.inventory.selectedSlot;
        int slot = mc.player.inventory.selectedSlot;
        double damageS = 0;
        double damageA = 0;
        double currentDamageS;
        double currentDamageA;
        for(int i = 0; i < 9; i++){
            if(mc.player.inventory.getStack(i).getItem() instanceof SwordItem
                    && (!antiBreak.get() || (mc.player.inventory.getStack(i).getMaxDamage() - mc.player.inventory.getStack(i).getDamage()) > 10)){
                currentDamageS = ((SwordItem) mc.player.inventory.getStack(i).getItem()).getMaterial().getAttackDamage() + EnchantmentHelper.getAttackDamage(mc.player.inventory.getStack(i), EntityGroup.DEFAULT) + 2;
                if(currentDamageS > damageS){
                    damageS = currentDamageS;
                    slotS = i;
                }
            }
        }
        for(int i = 0; i < 9; i++){
            if(mc.player.inventory.getStack(i).getItem() instanceof AxeItem
                    && (!antiBreak.get() || (mc.player.inventory.getStack(i).getMaxDamage() - mc.player.inventory.getStack(i).getDamage()) > 10)){
                currentDamageA = ((AxeItem) mc.player.inventory.getStack(i).getItem()).getMaterial().getAttackDamage() + EnchantmentHelper.getAttackDamage(mc.player.inventory.getStack(i), EntityGroup.DEFAULT) + 2;
                if(currentDamageA > damageA){
                    damageA = currentDamageA;
                    slotA = i;
                }
            }
        }
        if(weapon.get() == Weapon.Sword && threshold.get() > damageA - damageS){
            slot = slotS;
        }else if(weapon.get() == Weapon.Axe && threshold.get() > damageS - damageA){
            slot = slotA;
        }else if(weapon.get() == Weapon.Sword && threshold.get() < damageA - damageS){
            slot = slotA;
        }else if(weapon.get() == Weapon.Axe && threshold.get() < damageS - damageA){
            slot = slotS;
        }
        return slot;
    }
}

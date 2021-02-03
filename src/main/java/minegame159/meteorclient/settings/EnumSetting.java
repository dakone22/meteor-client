/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.settings;

import minegame159.meteorclient.gui.widgets.WDropbox;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    private T[] values;

    public EnumSetting(String name, String title, String description, String[] displayNames, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated) {
        super(name, title, description, defaultValue, onChanged, onModuleActivated);

        String[] names = {};
        try {
            values = (T[]) defaultValue.getClass().getMethod("values").invoke(null);

            names = new String[values.length];
            for (int i = 0; i < values.length; ++i) {
                if (displayNames != null && i < displayNames.length && displayNames[i] != null && !displayNames[i].isEmpty()) {
                    names[i] = displayNames[i];
                } else {
                    String key = String.format("enum.%s.%s", defaultValue.getClass().getSimpleName(), values[i].toString());
                    names[i] = (I18n.hasTranslation(key) ? I18n.translate(key) : values[i].toString());
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        widget = new WDropbox<>(get(), names);
        ((WDropbox<T>) widget).action = () -> set(((WDropbox<T>) widget).getValue());
    }

    @Override
    protected T parseImpl(String str) {
        for (T possibleValue : values) {
            if (str.equalsIgnoreCase(possibleValue.toString())) return possibleValue;
        }

        return null;
    }

    @Override
    public void resetWidget() {
        ((WDropbox<T>) widget).setValue(get());
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    protected String generateUsage() {
        String usage = "";

        for (int i = 0; i < values.length; i++) {
            if (i > 0) usage += " (default)or ";
            usage += "(highlight)" + values[i];
        }

        return usage;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = saveGeneral();
        tag.putString("value", get().toString());
        return tag;
    }

    @Override
    public T fromTag(CompoundTag tag) {
        parse(tag.getString("value"));

        return get();
    }

    public static class Builder<T extends Enum<?>> {
        protected String name = "undefined", title = "", description = "";
        protected String[] displayNames = {};
        protected T defaultValue;
        protected Consumer<T> onChanged;
        protected Consumer<Setting<T>> onModuleActivated;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> displayName(String title) {
            this.title = title;
            return this;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> displayValues(String[] displayNames) {
            this.displayNames = displayNames;
            return this;
        }

        public Builder<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T> onChanged(Consumer<T> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public Builder<T> onModuleActivated(Consumer<Setting<T>> onModuleActivated) {
            this.onModuleActivated = onModuleActivated;
            return this;
        }

        public EnumSetting<T> build() {
            return new EnumSetting<>(name, title, description, displayNames, defaultValue, onChanged, onModuleActivated);
        }
    }
}

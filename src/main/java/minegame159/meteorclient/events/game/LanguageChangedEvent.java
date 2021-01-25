package minegame159.meteorclient.events.game;

import net.minecraft.util.Language;

public class LanguageChangedEvent {
    private static final LanguageChangedEvent INSTANCE = new LanguageChangedEvent();

    public Language languageInstance;

    public static LanguageChangedEvent get(Language instance) {
        INSTANCE.languageInstance = instance;
        return INSTANCE;
    }
}
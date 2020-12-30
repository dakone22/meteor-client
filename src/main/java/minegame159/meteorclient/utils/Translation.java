package minegame159.meteorclient.utils;

import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.game.LanguageChangedEvent;
import net.minecraft.util.Language;

import javax.annotation.Nullable;

public class Translation {
    public static Translation INSTANCE;
    @Nullable
    private Language previousLanguage;

    public Translation() {
        MeteorClient.EVENT_BUS.subscribe(onLanguageChanged);
    }

    public String getTranslationByKey(String key) {
        return this.previousLanguage.get(key);
    }

    public String get(String string) {
        assert this.previousLanguage != null;
        if (this.previousLanguage.hasTranslation(string))
            return this.previousLanguage.get(string);
        return string;
    }

    private void updateTranslations() {
        Language language = Language.getInstance();
        if (this.previousLanguage != language) {
            this.previousLanguage = language;
        }
    }

    private final Listener<LanguageChangedEvent> onLanguageChanged = new Listener<>(event -> updateTranslations());
}
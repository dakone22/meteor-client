package minegame159.meteorclient.mixin;

import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.game.LanguageChangedEvent;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public class LanguageManagerMixin {
    @Inject(method = "apply(Lnet/minecraft/resource/ResourceManager;)V", at = @At("TAIL"))
    private void onLanguageChange(ResourceManager manager, CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(LanguageChangedEvent.get(Language.getInstance()));
    }
}
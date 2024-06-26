package cc.badideas.cosmatica.mixin;

import cc.badideas.cosmatica.Cosmatica;
import finalforeach.cosmicreach.gamestates.InGame;
import nanobass.qol.ChatProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin {
    @Inject(method = "create", at = @At("TAIL"))
    private void sendCosmaticaChatMessage(CallbackInfo ci) {
        Cosmatica.reset();
        ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Cosmatica " + Cosmatica.MOD_VERSION + " is ready! Use `/cosmatica` to open the menu, or `/cosmatica help` for a full list of commands.");
    }
}
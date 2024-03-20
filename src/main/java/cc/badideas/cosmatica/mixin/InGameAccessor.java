package cc.badideas.cosmatica.mixin;

import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.BlockSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGame.class)
public interface InGameAccessor {
    @Accessor
    BlockSelection getBlockSelection();
}

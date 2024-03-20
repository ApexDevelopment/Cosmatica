package cc.badideas.cosmatica.mixin;

import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.BlockSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockSelection.class)
public interface BlockSelectionAccessor {
    @Accessor("selectedBlockPos")
    BlockPosition getSelectedBlockPos();
}

package cc.badideas.cosmatica.util;

import cc.badideas.cosmatica.mixin.BlockSelectionAccessor;
import cc.badideas.cosmatica.mixin.InGameAccessor;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.BlockSelection;

public class CosmaticaUtils {
    public static IntVector3 worldToChunkLocalPosition(int x, int y, int z) {
        int chunkX = x - 16 * Math.floorDiv(x, 16);
        int chunkY = y - 16 * Math.floorDiv(y, 16);
        int chunkZ = z - 16 * Math.floorDiv(z, 16);

        return new IntVector3(chunkX, chunkY, chunkZ);
    }

    public static IntVector3 getPlayerLookPos() {
        BlockSelection selection = ((InGameAccessor) GameState.IN_GAME).getBlockSelection();
        BlockPosition lookPos = ((BlockSelectionAccessor) selection).getSelectedBlockPos();
        return new IntVector3(lookPos.getGlobalX(), lookPos.getGlobalY(), lookPos.getGlobalZ());
    }
}

package cc.badideas.cosmatica.util;

public class CosmaticaUtils {
    public static IntVector3 worldToChunkLocalPosition(int x, int y, int z) {
        int chunkX = x - 16 * Math.floorDiv(x, 16);
        int chunkY = y - 16 * Math.floorDiv(y, 16);
        int chunkZ = z - 16 * Math.floorDiv(z, 16);

        return new IntVector3(chunkX, chunkY, chunkZ);
    }
}

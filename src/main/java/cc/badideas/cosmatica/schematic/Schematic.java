package cc.badideas.cosmatica.schematic;

import cc.badideas.cosmatica.Cosmatica;
import cc.badideas.cosmatica.block.PalettizedBlock;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import finalforeach.cosmicreach.world.blocks.Block;
import finalforeach.cosmicreach.world.blocks.BlockState;
import nanobass.qol.exception.BlockNotFoundException;
import nanobass.qol.exception.BlockStateNotValidException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Schematic {
    private final String id;
    private final String name;
    private final String author;
    private final String cosmicReachVersion;
    private final String schematicFilePath;
    private long lastRead = 0;

    private final ArrayList<BlockState> blockStates = new ArrayList<>();
    private final ArrayList<PalettizedBlock> blocks = new ArrayList<>();

    public Schematic(String id, String name, String author, String cosmicReachVersion, String schematicFilePath) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.cosmicReachVersion = cosmicReachVersion;
        this.schematicFilePath = schematicFilePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getCosmicReachVersion() {
        return cosmicReachVersion;
    }

    private static Block parseBlock(String blockId, boolean allowAutoBase) throws BlockNotFoundException {
        for (Block block : Block.allBlocks) {
            boolean matches = block.getStringId().equals(blockId);
            if (!matches && allowAutoBase) {
                matches = block.getStringId().equals("base:" + blockId);
            }
            if (matches) {
                return block;
            }
        }

        throw new BlockNotFoundException(blockId);
    }

    private static BlockState parseBlockState(String blockState, boolean allowAutoBase) throws BlockStateNotValidException {
        int blockIdEnd = blockState.indexOf("[");
        if (blockIdEnd == -1) {
            blockIdEnd = blockState.length();
        }

        int stateStringStart = blockState.indexOf("[");
        if (stateStringStart == -1) {
            stateStringStart = blockState.length();
        } else {
            stateStringStart++;
        }

        int stateStringEnd = blockState.indexOf("]");
        if (stateStringEnd == -1) {
            stateStringEnd = blockState.length();
        }

        String blockId = blockState.substring(0, blockIdEnd);
        String stateString = blockState.substring(stateStringStart, stateStringEnd);

        Block block;

        try {
            block = parseBlock(blockId, allowAutoBase);
        } catch (BlockNotFoundException e) {
            throw new BlockStateNotValidException(e, stateString);
        }

        if (stateString.isEmpty()) {
            return block.getDefaultBlockState();
        }
        try {
            return block.getBlockStateFromString(stateString);
        } catch (RuntimeException e) {
            throw new BlockStateNotValidException(block, stateString);
        }
    }

    private void updateCachedSchematicData() throws IOException {
        File schematicFile = new File(schematicFilePath);
        if (lastRead < schematicFile.lastModified()) {
            lastRead = System.currentTimeMillis();
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(schematicFile));
            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                if (entry.getName().equalsIgnoreCase("blockstates.json")) {
                    JsonValue blockStatesJSON = new JsonReader().parse(zipInputStream);

                    try {
                        int size = blockStatesJSON.size;

                        for (int i = 0; i < size; i++) {
                            BlockState state = parseBlockState(blockStatesJSON.getString(i), true);
                            blockStates.add(state);
                        }
                    }
                    catch (Exception exception) {
                        Cosmatica.LOGGER.warning("Corrupt blockstates file!");
                    }
                }
                else if (entry.getName().equalsIgnoreCase("blocks")) {
                    byte[] blockBytes = new byte[16];
                    while (zipInputStream.read(blockBytes) == 16) {
                        ByteBuffer buffer = ByteBuffer.wrap(blockBytes);
                        int relativePosX = buffer.getInt();
                        int relativePosY = buffer.getInt();
                        int relativePosZ = buffer.getInt();
                        int paletteIndex = buffer.getInt();
                        PalettizedBlock block = new PalettizedBlock(relativePosX, relativePosY, relativePosZ, paletteIndex);
                        blocks.add(block);
                    }
                }

                entry = zipInputStream.getNextEntry();
            }

            zipInputStream.close();
        }
    }

    public ArrayList<BlockState> getBlockStates() {
        try {
            updateCachedSchematicData();
        } catch (Exception exception) {
            Cosmatica.LOGGER.warning("Schematic `" + getName() + "` couldn't be read!");
            Cosmatica.LOGGER.warning(exception.toString());
        }
        return this.blockStates;
    }

    public ArrayList<PalettizedBlock> getBlocks() {
        try {
            updateCachedSchematicData();
        } catch (Exception exception) {
            Cosmatica.LOGGER.warning("Schematic `" + getName() + "` couldn't be read!");
            Cosmatica.LOGGER.warning(exception.toString());
        }
        return this.blocks;
    }
}

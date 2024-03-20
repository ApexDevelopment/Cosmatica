package cc.badideas.cosmatica.schematic;

import cc.badideas.cosmatica.Cosmatica;
import cc.badideas.cosmatica.block.PalettizedBlock;
import cc.badideas.cosmatica.block.PositionedBlockState;
import cc.badideas.cosmatica.schematic.Schematic;
import cc.badideas.cosmatica.util.CosmaticaUtils;
import cc.badideas.cosmatica.util.IntVector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.blocks.BlockState;
import finalforeach.cosmicreach.world.chunks.Chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SchematicManager {
    private static final ArrayList<Schematic> schematics = new ArrayList<>();

    private static Schematic unzipSchematic(String fileName) throws IOException {
        File schematicFile = new File(Cosmatica.getSchematicFolderLocation() + "/" + fileName);
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(schematicFile));
        ZipEntry entry = zipInputStream.getNextEntry();

        while (entry != null) {
            if (entry.isDirectory()) {
                continue;
            }

            if (entry.getName().equalsIgnoreCase("schematic.json")) {
                JsonValue manifest = new JsonReader().parse(zipInputStream);

                try {
                    String id = manifest.getString("id");
                    String name = manifest.getString("name");
                    String author = manifest.getString("author");
                    String cosmicReachVersion = manifest.getString("cosmicReachVersion");
                    String manifestVersion = manifest.getString("manifestVersion");

                    if (!manifestVersion.equals(Cosmatica.MANIFEST_VERSION)) {
                        Cosmatica.LOGGER.warning("Couldn't load schematic `" + id + "` due to incompatible manifest version.");
                        zipInputStream.close();
                        return null;
                    }

                    zipInputStream.close();
                    return new Schematic(id, name, author, cosmicReachVersion, schematicFile.getAbsolutePath());
                }
                catch (Exception exception) {
                    Cosmatica.LOGGER.warning("Corrupt manifest file!");
                    zipInputStream.close();
                    return null;
                }
            }

            entry = zipInputStream.getNextEntry();
        }

        zipInputStream.close();
        return null;
    }

    public static void refreshSchematics() {
        schematics.clear();

        File schematicFolder = Cosmatica.getSchematicFolder();
        // Cosmatica will create the dir if it doesn't exist. It can never be null.
        Set<String> schematicFileNames = Stream.of(Objects.requireNonNull(schematicFolder.listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".zip"))
                .map(File::getName)
                .collect(Collectors.toSet());

        for (String schematicName : schematicFileNames) {
            try {
                Schematic schematic = unzipSchematic(schematicName);

                if (schematic == null) {
                    Cosmatica.LOGGER.warning("Malformed schematic file: `" + schematicName + "`");
                    continue;
                }

                Cosmatica.LOGGER.info("Loaded schematic `" + schematic.getId() + "` by `" + schematic.getAuthor() + "`");
                schematics.add(schematic);
            } catch (IOException e) {
                Cosmatica.LOGGER.warning("Unable to load schematic: `" + schematicName + "`");
            }
        }
    }

    public static ArrayList<Schematic> getSchematics() {
        return schematics;
    }

    public static Schematic getSchematicById(String id) {
        for (Schematic schematic : schematics) {
            if (schematic.getId().equals(id)) {
                return schematic;
            }
        }

        return null;
    }

    public static void placeInWorld(World world, Schematic schematic, int offsetX, int offsetY, int offsetZ) {
        ArrayList<BlockState> schematicBlockStates = schematic.getBlockStates();
        ArrayList<PalettizedBlock> blocks = schematic.getBlocks();
        Cosmatica.overwrittenBlocks.clear();

        int numBlocks = blocks.size();
        int numErrors = 0;

        for (PalettizedBlock block : blocks) {
            BlockState blockState = schematicBlockStates.get(block.paletteIndex());
            int finalX = offsetX + block.x();
            int finalY = offsetY + block.y();
            int finalZ = offsetZ + block.z();
            IntVector3 chunkPos = CosmaticaUtils.worldToChunkLocalPosition(finalX, finalY, finalZ);
            Chunk chunk = world.getChunkAtBlock(finalX, finalY, finalZ);

            if (chunk == null) {
                Cosmatica.LOGGER.warning(String.format("Chunk at %d, %d, %d was null!", finalX, finalY, finalZ));
                numErrors++;
                continue;
            }

            BlockPosition blockPosition = new BlockPosition(chunk, chunkPos.x(), chunkPos.y(), chunkPos.z());
            PositionedBlockState oldBlockState = new PositionedBlockState(chunk.getBlockState(chunkPos.x(), chunkPos.y(), chunkPos.z()), finalX, finalY, finalZ);
            Cosmatica.overwrittenBlocks.add(oldBlockState);
            BlockSetter.replaceBlock(world, blockState, blockPosition, new Queue<>());
        }

        Cosmatica.LOGGER.info(String.format("Placed %d blocks, %d errors.", numBlocks, numErrors));
    }

    public static void placeInWorld(World world, String schematicId, int offsetX, int offsetY, int offsetZ) {
        Schematic schematic = getSchematicById(schematicId);

        if (schematic != null) {
            placeInWorld(world, schematic, offsetX, offsetY, offsetZ);
        }
        else {
            Cosmatica.LOGGER.warning("Cannot place schematic, no schematic exists with ID `" + schematicId + "`");
        }
    }
}

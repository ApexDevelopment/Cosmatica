package cc.badideas.cosmatica.schematic;

import cc.badideas.cosmatica.Cosmatica;
import cc.badideas.cosmatica.block.PalettizedBlock;
import cc.badideas.cosmatica.util.IntVector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.blocks.BlockState;
import nanobass.qol.ChatProvider;
import nanobass.qol.command.ArgumentParser;
import nanobass.qol.command.CommandParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    public static void createFileFromUserSelection() {
        IntVector3 startPos = Cosmatica.startPos;
        IntVector3 endPos = Cosmatica.endPos;
        int startX = Math.min(startPos.x(), endPos.x());
        int startY = Math.min(startPos.y(), endPos.y());
        int startZ = Math.min(startPos.z(), endPos.z());
        int endX = Math.max(startPos.x(), endPos.x());
        int endY = Math.max(startPos.y(), endPos.y());
        int endZ = Math.max(startPos.z(), endPos.z());
        ArrayList<String> seenBlockStateStrings = new ArrayList<>();
        ArrayList<PalettizedBlock> palettizedBlocks = new ArrayList<>();

        if (Cosmatica.schematicId == null || Cosmatica.schematicName == null || Cosmatica.schematicAuthor == null) {
            ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Please specify a schematic ID, name, and author! See `/cosmatica help` for more information.");
            return;
        }

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                for (int z = startZ; z < endZ; z++) {
                    int relativeX = x - startX;
                    int relativeY = y - startY;
                    int relativeZ = z - startZ;
                    BlockState blockState = InGame.world.getBlockState(x, y, z);
                    String blockStateString = blockState.toString();
                    int index = seenBlockStateStrings.indexOf(blockStateString);

                    if (index == -1) {
                        seenBlockStateStrings.add(blockStateString);
                        index = seenBlockStateStrings.size() - 1;
                    }

                    palettizedBlocks.add(new PalettizedBlock(relativeX, relativeY, relativeZ, index));
                }
            }
        }

        JsonValue manifest = new JsonValue(JsonValue.ValueType.object);
        manifest.addChild("id", new JsonValue(Cosmatica.schematicId));
        manifest.addChild("name", new JsonValue(Cosmatica.schematicName));
        manifest.addChild("author", new JsonValue(Cosmatica.schematicAuthor));
        manifest.addChild("cosmicReachVersion", new JsonValue(RuntimeInfo.version));
        manifest.addChild("manifestVersion", new JsonValue(Cosmatica.MANIFEST_VERSION));

        JsonValue blockStatesJsonArray = new JsonValue(JsonValue.ValueType.array);
        for (String blockStateString : seenBlockStateStrings) {
            blockStatesJsonArray.addChild(new JsonValue(blockStateString));
        }

        ByteBuffer palettizedBlocksBytes = ByteBuffer.allocate(palettizedBlocks.size() * 16);
        palettizedBlocksBytes.order(ByteOrder.BIG_ENDIAN);
        for (PalettizedBlock block : palettizedBlocks) {
            palettizedBlocksBytes.putInt(block.x());
            palettizedBlocksBytes.putInt(block.y());
            palettizedBlocksBytes.putInt(block.z());
            palettizedBlocksBytes.putInt(block.paletteIndex());
        }

        ZipEntry manifestEntry = new ZipEntry("schematic.json");
        ZipEntry blockStatesEntry = new ZipEntry("blockstates.json");
        ZipEntry blocksEntry = new ZipEntry("blocks");

        File outfile = new File(Cosmatica.getSchematicFolderLocation() + "/" + Cosmatica.schematicId + ".zip");
        outfile.getParentFile().mkdirs();

        try {
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outfile));
            outputStream.putNextEntry(manifestEntry);
            outputStream.write(manifest.toJson(JsonWriter.OutputType.json).getBytes(StandardCharsets.UTF_8));
            outputStream.closeEntry();
            outputStream.putNextEntry(blockStatesEntry);
            outputStream.write(blockStatesJsonArray.toJson(JsonWriter.OutputType.json).getBytes(StandardCharsets.UTF_8));
            outputStream.closeEntry();
            outputStream.putNextEntry(blocksEntry);
            outputStream.write(palettizedBlocksBytes.array());
            outputStream.closeEntry();
            outputStream.close();
        }
        catch (IOException exception) {
            ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR,
                    "Unable to save schematic! Please ensure you have write access to the schematics directory.");
        }
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
                    byte[] buffer = new byte[1024];
                    int len;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    while ((len = zipInputStream.read(buffer)) > -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    outputStream.close();
                    String text = outputStream.toString(StandardCharsets.UTF_8);
                    JsonValue blockStatesJSON = new JsonReader().parse(text);

                    try {
                        int size = blockStatesJSON.size;

                        for (int i = 0; i < size; i++) {
                            BlockState state = ArgumentParser.parseBlockState(blockStatesJSON.getString(i), true);
                            blockStates.add(state);
                        }
                    }
                    catch (Exception exception) {
                        Cosmatica.LOGGER.warning("Corrupt blockstates file!");
                    }
                }
                else if (entry.getName().equalsIgnoreCase("blocks")) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(bytes)) > 0) {
                        outputStream.write(bytes, 0, len);
                    }

                    ByteBuffer buffer = ByteBuffer.wrap(outputStream.toByteArray());
                    buffer.order(ByteOrder.BIG_ENDIAN);

                    while (buffer.hasRemaining()) {
                        int relativePosX = buffer.getInt();
                        int relativePosY = buffer.getInt();
                        int relativePosZ = buffer.getInt();
                        int paletteIndex = buffer.getInt();
                        PalettizedBlock block = new PalettizedBlock(relativePosX, relativePosY, relativePosZ, paletteIndex);
                        blocks.add(block);
                    }

                    Cosmatica.LOGGER.info(String.format("Schematic contains %d blocks", blocks.size()));
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
            exception.printStackTrace();
        }
        return this.blockStates;
    }

    public ArrayList<PalettizedBlock> getBlocks() {
        try {
            updateCachedSchematicData();
        } catch (Exception exception) {
            Cosmatica.LOGGER.warning("Schematic `" + getName() + "` couldn't be read!");
            exception.printStackTrace();
        }
        return this.blocks;
    }
}

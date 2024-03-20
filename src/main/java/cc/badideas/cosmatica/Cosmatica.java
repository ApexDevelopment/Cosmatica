package cc.badideas.cosmatica;

import cc.badideas.cosmatica.block.PositionedBlockState;
import cc.badideas.cosmatica.commands.CosmaticaCommand;
import cc.badideas.cosmatica.schematic.Schematic;
import cc.badideas.cosmatica.util.IntVector3;
import nanobass.qol.ChatAuthor;
import nanobass.qol.command.CommandProvider;
import net.fabricmc.api.ModInitializer;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class Cosmatica implements ModInitializer {
    public static final String MOD_ID = "cosmatica";
    public static final String MOD_VERSION = "0.0.1";
    public static final String MANIFEST_VERSION = "1";
    public static final String SCHEMATIC_DIRECTORY = "schematics";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static final ChatAuthor CHAT_AUTHOR = new ChatAuthor("Cosmatica Mod");

    public static Schematic selectedSchematic = null;
    public static Queue<PositionedBlockState> overwrittenBlocks = new LinkedList<>();
    public static IntVector3 startPos = null;
    public static IntVector3 endPos = null;
    public static IntVector3 placeOrigin = null;

    @Override
    public void onInitialize() {
        LOGGER.info("Cosmatica loading commands...");
        CommandProvider.getInstance().register(new CosmaticaCommand());
    }

    public static String getSchematicFolderLocation() {
        String osName = System.getProperty("os.name").toLowerCase();
        String rootFolder;

        if (osName.contains("windows")) {
            rootFolder = System.getenv("LOCALAPPDATA");
        } else if (osName.contains("mac")) {
            rootFolder = System.getenv("HOME") + "/Library";
        } else {
            rootFolder = System.getenv("XDG_DATA_HOME");
            if (rootFolder == null || rootFolder.isEmpty()) {
                rootFolder = System.getenv("HOME") + "/.local/share";
            }
        }

        return rootFolder + "/cosmic-reach/" + SCHEMATIC_DIRECTORY;
    }

    public static File getSchematicFolder() {
        File dir = new File(getSchematicFolderLocation());
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    public static void reset() {
        selectedSchematic = null;
        startPos = null;
        endPos = null;
        placeOrigin = null;
        overwrittenBlocks.clear();
    }
}
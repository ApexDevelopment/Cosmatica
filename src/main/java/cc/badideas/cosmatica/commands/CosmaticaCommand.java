package cc.badideas.cosmatica.commands;

import cc.badideas.cosmatica.*;
import cc.badideas.cosmatica.block.PositionedBlockState;
import cc.badideas.cosmatica.gamestates.CosmaticaMenu;
import cc.badideas.cosmatica.mixin.BlockSelectionAccessor;
import cc.badideas.cosmatica.mixin.InGameAccessor;
import cc.badideas.cosmatica.schematic.SchematicManager;
import cc.badideas.cosmatica.util.CosmaticaUtils;
import cc.badideas.cosmatica.util.IntVector3;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.BlockSelection;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.blocks.BlockState;
import finalforeach.cosmicreach.world.chunks.Chunk;
import nanobass.qol.ChatProvider;
import nanobass.qol.command.Command;
import nanobass.qol.command.CommandProvider;

import java.util.Map;

public class CosmaticaCommand extends Command {
    public CosmaticaCommand() {
        super("cosmatica", "Opens the Cosmatica menu", new Argument("subcommand", 0, false, ArgumentType.String));
    }

    @Override
    public void execute(CommandProvider commandProvider, Map<String, Parameter> parameters) {
        if (!parameters.containsKey("subcommand")) {
            boolean cursorCaught = Gdx.input.isCursorCatched();
            Gdx.input.setCursorCatched(false);
            GameState.switchToGameState(new CosmaticaMenu(cursorCaught));
            return;
        }

        String subcommand = parameters.get("subcommand").getAsString();

        if (subcommand.equalsIgnoreCase("place")) {
            if (Cosmatica.selectedSchematic != null) {
                IntVector3 origin = Cosmatica.placeOrigin;

                if (origin == null) {
                    Vector3 playerPos = InGame.getLocalPlayer().getEntity().getPosition();
                    origin = new IntVector3((int)playerPos.x, (int)playerPos.y, (int)playerPos.z);
                }

                SchematicManager.placeInWorld(InGame.world, Cosmatica.selectedSchematic, origin.x(), origin.y(), origin.z());
                ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Schematic placed!");
            }
            else {
                ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "No schematic selected! Use `/cosmatica` to open the menu.");
            }
        }
        else if (subcommand.equalsIgnoreCase("undo")) {
            if (!Cosmatica.overwrittenBlocks.isEmpty()) {
                int numChanges = Cosmatica.overwrittenBlocks.size();

                while (!Cosmatica.overwrittenBlocks.isEmpty()) {
                    PositionedBlockState oldBlockStateAndPosition = Cosmatica.overwrittenBlocks.remove();
                    BlockState oldBlockState = oldBlockStateAndPosition.blockState();
                    int worldX = oldBlockStateAndPosition.x();
                    int worldY = oldBlockStateAndPosition.y();
                    int worldZ = oldBlockStateAndPosition.z();
                    IntVector3 chunkPos = CosmaticaUtils.worldToChunkLocalPosition(worldX, worldY, worldZ);
                    Chunk chunk = InGame.world.getChunkAtBlock(worldX, worldY, worldZ);
                    BlockPosition position = new BlockPosition(chunk, chunkPos.x(), chunkPos.y(), chunkPos.z());
                    BlockSetter.replaceBlock(InGame.world, oldBlockState, position, new Queue<>());
                }

                ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, String.format("Undid %d blocks.", numChanges));
            }
            else {
                ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Nothing to undo!");
            }
        }
        else if (subcommand.equalsIgnoreCase("origin")) {
            BlockSelection selection = ((InGameAccessor) GameState.IN_GAME).getBlockSelection();
            BlockPosition lookPos = ((BlockSelectionAccessor) selection).getSelectedBlockPos();
            Cosmatica.placeOrigin = new IntVector3(lookPos.getGlobalX(), lookPos.getGlobalY(), lookPos.getGlobalZ());
            ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, String.format("Set schematic origin to [%d %d %d].",
                    Cosmatica.placeOrigin.x(),
                    Cosmatica.placeOrigin.y(),
                    Cosmatica.placeOrigin.z()));
        }
    }
}

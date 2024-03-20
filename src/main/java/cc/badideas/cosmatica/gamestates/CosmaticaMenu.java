package cc.badideas.cosmatica.gamestates;

import cc.badideas.cosmatica.Cosmatica;
import cc.badideas.cosmatica.schematic.Schematic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.ui.UIElement;
import nanobass.qol.ChatProvider;

import java.io.File;
import java.io.IOException;

public class CosmaticaMenu extends GameState {
    boolean cursorCaught;
    boolean firstFrame;

    public CosmaticaMenu(boolean cursorCaught) {
        this.cursorCaught = cursorCaught;
    }

    public void create() {
        super.create();

        UIElement placeSchematicButton = new UIElement(0.0F, -200.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new SchematicListMenu(CosmaticaMenu.this));
            }
        };
        placeSchematicButton.setText("Place Schematic");
        placeSchematicButton.show();
        this.uiElements.add(placeSchematicButton);

        UIElement createSchematicButton = new UIElement(0.0F, -100.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();
                if (Cosmatica.startPos == null || Cosmatica.endPos == null) {
                    ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, String.join(" ",
                            "Select a start and end position for your schematic.",
                            "You can do this with `/cosmatica start` and `/cosmatica end`."
                    ));
                    switchToGameState(IN_GAME);
                    return;
                }

                Schematic.createFileFromUserSelection();
                ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, String.join(" ",
                        "Schematic was exported to schematics directory!",
                        "Filename:",
                        "`" + Cosmatica.schematicId + ".zip`"
                ));
                switchToGameState(IN_GAME);
            }
        };
        createSchematicButton.setText("Create New Schematic");
        createSchematicButton.show();
        this.uiElements.add(createSchematicButton);

        UIElement openSchematicsButton = new UIElement(0.0F, 0.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();

                try {
                    File schematicFolder = Cosmatica.getSchematicFolder();
                    SaveLocation.OpenFolderWithFileManager(schematicFolder);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        };
        openSchematicsButton.setText("Open Schematics Folder");
        openSchematicsButton.show();
        this.uiElements.add(openSchematicsButton);

        UIElement returnToGameButton = new UIElement(0.0F, 100.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(GameState.IN_GAME);
                Gdx.input.setCursorCatched(CosmaticaMenu.this.cursorCaught);
            }
        };
        returnToGameButton.setText("Return To Game");
        returnToGameButton.show();
        this.uiElements.add(returnToGameButton);
    }

    public void resize(int width, int height) {
        super.resize(width, height);
        IN_GAME.resize(width, height);
    }

    public void render() {
        super.render();
        if (!this.firstFrame && Gdx.input.isKeyJustPressed(111)) {
            switchToGameState(IN_GAME);
        }

        ScreenUtils.clear(0.1F, 0.1F, 0.2F, 1.0F, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        IN_GAME.render();

        this.drawUIElements();
        this.firstFrame = false;
    }
}

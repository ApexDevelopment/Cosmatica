package cc.badideas.cosmatica.gamestates;

import cc.badideas.cosmatica.Cosmatica;
import cc.badideas.cosmatica.schematic.Schematic;
import cc.badideas.cosmatica.schematic.SchematicManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.UIElement;
import nanobass.qol.ChatProvider;

public class SchematicListMenu extends GameState {
    private final GameState previousState;

    public SchematicListMenu(GameState previousState) {
        this.previousState = previousState;
    }

    public void create() {
        super.create();
        SchematicManager.refreshSchematics();

        float yPlacement = -200.0F;

        for (Schematic schematic : SchematicManager.getSchematics()) {
            UIElement schematicButton = new UIElement(0.0F, yPlacement, 400.0F, 50.0F) {
                public void onClick() {
                    super.onClick();

                    if (!schematic.getCosmicReachVersion().equals(RuntimeInfo.version)) {
                        ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Schematic `"
                            + schematic.getName() + "` (ID `" + schematic.getId() + "`) was made for"
                            + " a different version of Cosmic Reach, and may not load properly!");
                    }

                    Cosmatica.selectedSchematic = schematic;
                    ChatProvider.getInstance().sendMessage(Cosmatica.CHAT_AUTHOR, "Schematic selected! "
                        + "Use `/cosmatica place` to place it.");
                    GameState.switchToGameState(previousState);
                }
            };
            schematicButton.setText(schematic.getName() + " by " + schematic.getAuthor());
            schematicButton.show();
            this.uiElements.add(schematicButton);
            yPlacement += 55;
        }

        UIElement cancelButton = new UIElement(0.0F, 200.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(previousState);
            }
        };
        cancelButton.setText("Cancel");
        cancelButton.show();
        this.uiElements.add(cancelButton);
    }

    public void render() {
        super.render();
        if (Gdx.input.isKeyJustPressed(111)) {
            switchToGameState(previousState);
        }

        ScreenUtils.clear(0.1F, 0.1F, 0.2F, 1.0F, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);

        this.drawUIElements();
    }
}

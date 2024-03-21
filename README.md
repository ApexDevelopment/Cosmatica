# Cosmatica

Cosmatica provides a way to share your Cosmic Reach builds, as well as import others' builds into your world!

## Dependencies

Cosmatica depends on `nanobass-qol` **1.6.4**, which itself depends on `nanobass-core`.

## How to use

Simply add the JAR to your mods folder (make sure you also have nanobass's mods).

To import schematics, first make sure you have a schematics folder. It should be inside your cosmic-reach folder, the same place your mods folder is. If it doesn't exist, you can create it. Download the schematic .zip file and place it in your schematics folder. In your Cosmic Reach world, run `/cosmatica` and select "Place Schematic". Choose the schematic you just downloaded. Now, you can either use `/cosmatica origin` to set the origin of the schematic to the block you're looking at, or you can just skip that and run `/cosmatica place`. If you do not specify an origin, it will be placed at your feet. Schematics will always be placed so that they extend away from the origin in the positive direction on all axes.

Mess up the placement? No problem. Run `/cosmatica undo` to restore the world to the way it was. This can only be run once, and if you made any modifications to the area the schematic was placed, they will be reverted too.

To create a schematic, first set the schematic's ID, name, and author using `/cosmatica id [id here]`, `/cosmatica name [name here]`, etc. The ID, name, and author can't have spaces. Now, select the starting and ending positions of your schematic with `/cosmatica start` and `/cosmatica end` (they will use the block you're currently looking at). Open the Cosmatica menu and click "Create New Schematic". Your schematic file should appear in the schematics directory! You can now share this with your friends.
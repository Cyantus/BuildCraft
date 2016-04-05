package buildcraft.core;

import net.minecraft.block.material.Material;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.item.ItemEngine_BC8;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.lib.block.BlockBuildCraftBase_BC8;

public class BCCoreBlocks {
    public static BlockEngine_BC8 engine;
    public static BlockSpring spring;
    public static BlockDecoration decorated;
    public static BlockMarker marker;
    public static BlockPathMarker markerPath;

    public static void preInit() {
        engine = BlockBuildCraftBase_BC8.register(new BlockEngine_BC8(Material.iron, "block.engine.bc"), ItemEngine_BC8.class);
        // engine = registerEngine("engine");

        engine.registerEngine(EnumEngineType.WOOD, TileEngineRedstone_BC8::new);
    }
}
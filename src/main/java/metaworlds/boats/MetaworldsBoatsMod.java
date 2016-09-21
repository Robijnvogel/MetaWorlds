package metaworlds.boats;

import net.minecraft.block.Block;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

//@Mod(modid=MetaworldsBoatsMod.MODID, name="MetaworldsBoatsMod", version=MetaworldsBoatsMod.VERSION, dependencies="required-after:MetaworldsMod")
public class MetaworldsBoatsMod {

    public static final String MODID = "metaworldsboatsmod";
    public static final String VERSION = "0.995";
    
    public static Block floatingWoodBlock;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.floatingWoodBlock = new BlockFloatingWood().setHardness(3.0f).setResistance(15.0f).setStepSound(Block.soundTypeWood).setBlockName("floatingWoodBlock");
        this.floatingWoodBlock.setBlockTextureName("planks_oak");
        
        GameRegistry.registerBlock(this.floatingWoodBlock, "floatingWoodBlock");
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        
    }
}

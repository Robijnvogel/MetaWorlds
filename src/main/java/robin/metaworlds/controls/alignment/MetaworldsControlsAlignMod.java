package robin.metaworlds.controls.alignment;

import robin.metaworlds.api.RecipeConfig;
import robin.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.BlockSand;

@Mod(modid="MetaworldsControlsAlignMod", name="MetaworldsControlsAlignMod", version="0.985", dependencies="required-after:MetaworldsMod")
public class MetaworldsControlsAlignMod {
	public static RecipeConfig subWorldAlignerConfig;
	public static Block subWorldAligner; 

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsControlsAlignMod")
    public static MetaworldsControlsAlignMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();

        this.subWorldAlignerConfig = new RecipeConfig(config, "subWorldAligner", Block.getIdFromBlock(subWorldAligner), false, new String[]{"SSS", "SSS", "SSS"}, new RecipePlaceHolderDef[]{new RecipePlaceHolderDef('S', Block.getIdFromBlock(new BlockSand()))});

        config.save();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event) {
    	this.subWorldAligner = (new BlockSubWorldAligner(Material.wood)).setHardness(0.5F).setStepSound(Block.soundTypeWood).setBlockName("subWorldAligner").setBlockTextureName("planks");
    	
        GameRegistry.registerBlock(subWorldAligner, "subWorldAligner");
    	LanguageRegistry.addName(subWorldAligner, "SubWorld Aligner");
    	subWorldAlignerConfig.addRecipeToGameRegistry();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
}

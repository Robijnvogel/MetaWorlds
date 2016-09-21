package robin.metaworlds.creators.contagious;

import robin.metaworlds.api.RecipeConfig;
import robin.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.BlockDirt;

@Mod(modid="MetaworldsContagiousCreatorMod", name="MetaworldsContagiousCreatorMod", version="0.985", dependencies="required-after:MetaworldsMod")
public class MetaworldsContagiousCreatorMod 
{
	public static RecipeConfig contagiousSubWorldCreatorConfig;
	public static Block contagiousSubWorldCreator; 
	public static Block dummyBlock;

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsContagiousCreatorMod")
    public static MetaworldsContagiousCreatorMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();
        
        MetaworldsContagiousCreatorMod.contagiousSubWorldCreatorConfig = new RecipeConfig(config, "contagiousSubWorldCreator", Block.getIdFromBlock(MetaworldsContagiousCreatorMod.contagiousSubWorldCreator), false, new String[]{"DDD", "DDD", "DDD"}, new RecipePlaceHolderDef[]{new RecipePlaceHolderDef('D', 3)}); //3 is the Block ID for Dirt

        config.save();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event) {
    	this.contagiousSubWorldCreator = (new BlockContagiousSubWorldCreator(Material.rock)).setHardness(3.0F).setResistance(15.0F).setStepSound(Block.soundTypeStone).setBlockName("contagiousSubWorldCreator").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("emerald_block");
    	this.dummyBlock = new BlockDummyReobfTracker(Material.air);
    	((BlockDummyReobfTracker)this.dummyBlock).initialize();
    	
    	GameRegistry.registerBlock(contagiousSubWorldCreator, "contagiousSubWorldCreator");
    	LanguageRegistry.addName(contagiousSubWorldCreator, "Contagious SubWorld Creator");
    	contagiousSubWorldCreatorConfig.addRecipeToGameRegistry();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
}

package robin.metaworlds.creators.blank;

import robin.metaworlds.api.RecipeConfig;
import robin.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.BlockStone;

@Mod(modid="MetaworldsBlankCreatorMod", name="MetaworldsBlankCreatorMod", version="0.985", dependencies="required-after:MetaworldsMod")
public class MetaworldsBlankCreatorMod {
	public static RecipeConfig blankSubWorldCreatorConfig;
	public static Block blankSubWorldCreator; 

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsBlankCreatorMod")
    public static MetaworldsBlankCreatorMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();
        
        this.blankSubWorldCreatorConfig = new RecipeConfig(config, "blankSubWorldCreator", Block.getIdFromBlock(this.blankSubWorldCreator), false, new String[]{"CCC", "CCC", "CCC"}, new RecipePlaceHolderDef[]{new RecipePlaceHolderDef('C', Block.getIdFromBlock(new BlockStone()))});

        config.save();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event) {
    	this.blankSubWorldCreator = (new BlockBlankSubWorldCreator(Material.rock)).setHardness(3.0F).setResistance(15.0F).setStepSound(Block.soundTypeStone).setBlockName("blankSubWorldCreator").setCreativeTab(CreativeTabs.tabBlock).setBlockTextureName("emerald_block");
    	
    	GameRegistry.registerBlock(blankSubWorldCreator, "blankSubWorldCreator");
    	LanguageRegistry.addName(blankSubWorldCreator, "Blank SubWorld Creator");
    	blankSubWorldCreatorConfig.addRecipeToGameRegistry();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
}

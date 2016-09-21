package metaworlds.joints;

import metaworlds.api.RecipeConfig;
import metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
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

//@Mod(modid="MetaworldsJointsMod", name="MetaworldsJointsMod", version="0.994", dependencies="required-after:MetaworldsMod")
public class MetaworldsJointsMod {
	public static RecipeConfig hingeJointBlockConfig;
	public static Block hingeJointBlock;
	
	protected Configuration config;

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsJointsMod")
    public static MetaworldsJointsMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	config = new Configuration(event.getSuggestedConfigurationFile());
    	
    	this.hingeJointBlock = (new BlockHingeJoint()).setHardness(0.5F).setStepSound(Block.soundTypeWood).setBlockName("subWorldHingeJoint");
        this.hingeJointBlock.setBlockTextureName("planks_oak");
        
        config.load();
        
        //this.hingeJointBlockID = config.getBlock("hingeJointBlock", 3588).getInt();
        this.hingeJointBlockConfig = new RecipeConfig(config, "hingeJointBlock", new ItemStack(this.hingeJointBlock, 1, 0), false, new String[]{"I", "", ""}, new RecipePlaceHolderDef[]{new RecipePlaceHolderDef('I', Blocks.iron_block.getUnlocalizedName())});

        config.save();
        
        GameRegistry.registerBlock(hingeJointBlock, "subWorldHingeJoint");
        //LanguageRegistry.addName(hingeJointBlock, "Hinge Joint");
        hingeJointBlockConfig.addRecipeToGameRegistry();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event) {
    	
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
}

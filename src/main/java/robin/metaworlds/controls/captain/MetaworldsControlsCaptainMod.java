package robin.metaworlds.controls.captain;

import org.lwjgl.input.Keyboard;

import robin.metaworlds.api.RecipeConfig;
import robin.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import metaworlds.core.MetaworldsPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWood;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid="MetaworldsControlsCaptainMod", name="MetaworldsControlsCaptainMod", version="0.985", dependencies="required-after:MetaworldsMod")
@NetworkMod(clientSideRequired=true, serverSideRequired=true, channels={"mwcaptain"}, packetHandler = ControllerCaptainPacketHandler.class)
public class MetaworldsControlsCaptainMod {
	public static RecipeConfig subWorldControllerConfig;
	public static Block subWorldController; 

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsControlsCaptainMod")
    public static MetaworldsControlsCaptainMod instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();

        this.subWorldControllerConfig = new RecipeConfig(config, "subWorldController", Block.getIdFromBlock(this.subWorldController), false, new String[]{"PPP", "PPP", "PPP"}, new RecipePlaceHolderDef[]{new RecipePlaceHolderDef('P', Block.getIdFromBlock(new BlockWood()))});

        config.save();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event) {
    	this.subWorldController = (new BlockSubWorldController(Material.ground)).setHardness(0.5F).setStepSound(Block.soundTypeGravel).setBlockName("subWorldController").setBlockTextureName("planks");
    	
        GameRegistry.registerBlock(subWorldController, "subWorldController");
    	LanguageRegistry.addName(subWorldController, "SubWorld Controller");
    	subWorldControllerConfig.addRecipeToGameRegistry();
    	EntityRegistry.registerModEntity(EntitySubWorldController.class, "EntitySubWorldController2", EntityRegistry.findGlobalUniqueEntityId(), this, 80, 3, true);
    	
    	if (event.getSide().isClient())
    	{
    		KeyBinding[] key = {new KeyBinding("LCTRL", Keyboard.KEY_LCONTROL, "MetaWorlds")};
    		boolean[] repeat = {false};
    		ClientRegistry.registerKeyBinding(new SubWorldControllerKeyHandler(key, repeat));
    	}
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
}

package metaworlds.core;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import metaworlds.admin.MwAdminClientActionPacket;
import metaworlds.admin.MwAdminGuiHandler;
import metaworlds.admin.MwAdminGuiInitPacket;
import metaworlds.admin.MwAdminGuiSubWorldInfosPacket;
import metaworlds.admin.SubWorldImportProgressUpdater;
import metaworlds.patcher.CSubWorldProxyPacket;
import metaworlds.patcher.SSubWorldProxyPacket;
import metaworlds.serverlist.ServerListButtonAdder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.Mod.EventHandler; // used in 1.6.2
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.relauncher.Side;

//@Mod(modid="MetaworldsMod", name="MetaworldsMod", version="0.994")
public class MetaworldsMod extends /*DummyModContainer*/ {
    /*public MetaworldsMod()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "MetaworldsMod";
        meta.name = "MetaworldsMod";
        meta.version = "0.994";
        meta.credits = "";
        meta.authorList = Arrays.asList("MMM_MasterM");
        meta.description = "description missing";
        meta.url = "url missing";
        meta.updateUrl = "";
        meta.screenshots = new String[0];
        meta.logoFile = "";
        meta.dependencies = new ArrayList<ArtifactVersion>();
        meta.dependencies.add(new DefaultArtifactVersion("Forge", true));
    }*/
    
    public GeneralPacketPipeline networkHandler;
    
    public static final String CHANNEL = "mwcore";

    // The instance of your mod that Forge uses.
    @Instance("MetaworldsMod")
    public static MetaworldsMod instance;
        
    // Says where the client and server 'proxy' code is loaded.
    //@SidedProxy(clientSide="metaworlds.core.client.ClientProxy", serverSide="metaworlds.core.ServerProxy")
    //public static CommonProxy proxy;
    
    @EventHandler // used in 1.6.2
    public void preInit(FMLPreInitializationEvent event) {
        /*if (event.getSide().isClient())
            clientInstance = this;
        else
            serverInstance = this;*/
        
        if (event.getSide().isClient())
            WorldClient.subWorldFactory = new SubWorldClientFactory();
    	WorldServer.subWorldFactory = new SubWorldServerFactory();
    	MinecraftForge.EVENT_BUS.register(new EventHookContainer());
    }
    
    @EventHandler // used in 1.6.2
    public void load(FMLInitializationEvent event) {
    	FMLCommonHandler.instance().bus().register(new MWCorePlayerTracker());
    	FMLCommonHandler.instance().bus().register(new PlayerTickHandler());
    	if (event.getSide().isClient())
    	{
    	    FMLCommonHandler.instance().bus().register(new SubWorldClientPreTickHandler());
    	    FMLCommonHandler.instance().bus().register(new ServerListButtonAdder());
    	}
    	
    	networkHandler = new GeneralPacketPipeline();
        networkHandler.initialize(CHANNEL);
        networkHandler.addDiscriminator(251, CSubWorldProxyPacket.class);
        networkHandler.addDiscriminator(250, SSubWorldProxyPacket.class);
        networkHandler.addDiscriminator(249, SubWorldUpdatePacket.class);
        networkHandler.addDiscriminator(248, SubWorldCreatePacket.class);
        networkHandler.addDiscriminator(247, SubWorldDestroyPacket.class);
        networkHandler.addDiscriminator(246, MwAdminGuiInitPacket.class);
        networkHandler.addDiscriminator(245, MwAdminClientActionPacket.class);
        networkHandler.addDiscriminator(244, MwAdminGuiSubWorldInfosPacket.class);
        
        NetworkRegistry.INSTANCE.registerGuiHandler("MetaworldsMod", new MwAdminGuiHandler());
        
        FMLCommonHandler.instance().bus().register(new SubWorldImportProgressUpdater());
    }
    
    @EventHandler // used in 1.6.2
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event)
    {
             MinecraftServer server = MinecraftServer.getServer();
             ICommandManager command = server.getCommandManager();
             ServerCommandManager manager = (ServerCommandManager) command;
             manager.registerCommand(new CommandTPWorlds());
             manager.registerCommand(new CommandMWAdmin());
             // Get's the current server instance
    }
}
package metaworlds.patcher;

import java.util.ArrayList;
import java.util.Arrays;

import metaworlds.core.MetaworldsMod;

import org.apache.logging.log4j.Level;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;

public class MetaworldsDummyContainer extends DummyModContainer//MetaworldsMod
{
    public MetaworldsDummyContainer()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "MetaworldsMod";
        meta.name = "Metaworlds_patcher";
        meta.version = "0.995";
        meta.credits = "";
        meta.authorList = Arrays.asList("MMM_MasterM");
        meta.description = "description missing";
        meta.url = "url missing";
        meta.updateUrl = "";
        meta.screenshots = new String[0];
        meta.logoFile = "";
        meta.dependencies = new ArrayList<ArtifactVersion>();
        meta.dependencies.add(new DefaultArtifactVersion("Forge", true));
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }
    
    @Subscribe
    //@Override
    public void preInit(FMLPreInitializationEvent event) {
        FMLLog.log("Metaworlds_patcher", Level.INFO, "Container PreInit");
        
        MetaworldsMod.instance = new MetaworldsMod();
        
        MetaworldsMod.instance.preInit(event);
        
        //if (this.instance == null)
        //    this.instance = this;
        
        //super.preInit(event);
    }
    
    @Subscribe
    //@Override
    public void load(FMLInitializationEvent event) {
        MetaworldsMod.instance.load(event);
        //super.load(event);
    }
    
    @Subscribe
    //@Override
    public void postInit(FMLPostInitializationEvent event) {
        MetaworldsMod.instance.postInit(event);
        //super.postInit(event);
    }
    
    @Subscribe
    //@Override
    public void serverStart(FMLServerStartingEvent event) {
        MetaworldsMod.instance.serverStart(event);
        //super.serverStart(event);
    }
}


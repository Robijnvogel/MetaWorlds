package metaworlds;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(modid = "mwtestmod", name = "MW Test Mod", version = "1.0")
public class TestMod {
    public static TestMod INSTANCE;
    
    public static String MODID = "mwtestmod";
    
    @SidedProxy(clientSide = "metaworlds.ClientProxy", serverSide = "metaworlds.ServerProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void load(FMLPreInitializationEvent event)
    {
        TestMod.INSTANCE = this;
    }
}

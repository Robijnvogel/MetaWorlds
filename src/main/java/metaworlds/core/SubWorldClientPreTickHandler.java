package metaworlds.core;

import java.util.EnumSet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import metaworlds.core.client.SubWorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class SubWorldClientPreTickHandler
{
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase.equals(Phase.START))
        {
            //tickStart
            if (Minecraft.getMinecraft().theWorld != null)
            {
                for (World curSubWorld : Minecraft.getMinecraft().theWorld.getSubWorlds())
                {
                    ((SubWorldClient)curSubWorld).onPreTick();
                }
            }
        }
    }
    
	/*public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.CLIENT);
	}
	
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if (Minecraft.getMinecraft().theWorld != null)
		{
			for (World curSubWorld : Minecraft.getMinecraft().theWorld.getSubWorlds())
			{
				((SubWorldClient)curSubWorld).onPreTick();
			}
		}
	}
	
	public void tickEnd(EnumSet<TickType> type, Object... tickData) { }
	
	public String getLabel()
	{
		return "MetaWorlds SubWorldClientPreTickHandler";
	}*/
}

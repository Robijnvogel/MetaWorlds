package metaworlds.serverlist;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ServerListButtonAdder {
    
    ServerListButton serverListButton;
    Field fieldButtonList;
    
    public ServerListButtonAdder()
    {
        for (Field curField : GuiScreen.class.getDeclaredFields())
        {
            if (curField.getType() == List.class)
            {
                fieldButtonList = curField;
                fieldButtonList.setAccessible(true);
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (true)
            return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.theWorld != null)
            return;
        
        if (mc.currentScreen != null && mc.currentScreen instanceof GuiMultiplayer)
        {
            GuiMultiplayer guiMultiplayer = (GuiMultiplayer)mc.currentScreen;
            
            if (this.serverListButton == null)
                this.serverListButton = new ServerListButton(170, 15, 10, 95, 20, "Online Servers");
            
            List buttonList = null;
            
            try
            {
                buttonList = (List)fieldButtonList.get(guiMultiplayer);
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            
            if (!buttonList.contains(this.serverListButton))
            {
                buttonList.add(this.serverListButton);
            }
        }
    }
}

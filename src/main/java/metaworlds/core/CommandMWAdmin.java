package metaworlds.core;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CommandMWAdmin extends CommandBase {

    @Override
    public String getCommandName()
    {
        return "mwc";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "/mwc Opens a MetaWorlds administration GUI";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] var2)
    {
        if (icommandsender instanceof EntityPlayer)
        {
            EntityPlayer senderPlayer = (EntityPlayer)icommandsender;
            
            FMLNetworkHandler.openGui(senderPlayer, "MetaworldsMod", 0/*GuiMwAdmin*/, null, 0, 0, 0);
        }
    }

}

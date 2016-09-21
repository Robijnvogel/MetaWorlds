package metaworlds.patcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.world.WorldSettings;

public class NetHandlerPlayClientSubWorldProxy extends NetHandlerPlayClient 
{
	public EntityClientPlayerMPSubWorldProxy proxyPlayer;
	
	public NetHandlerPlayClientSubWorldProxy(MinecraftSubWorldProxy minecraftProxy, NetHandlerPlayClient parentNetHandler, WorldClient targetSubWorld)
	{
		super(minecraftProxy, new NetworkManagerSubWorldProxy(parentNetHandler.getNetworkManager(), targetSubWorld.getSubWorldID(), true), targetSubWorld);
		
        this.currentServerMaxPlayers = parentNetHandler.currentServerMaxPlayers;
	}
	
	/*@Override
	public void processReadPackets()
	{
		
	}
	
	@Override
    public EntityPlayer getPlayer()
    {
        return this.proxyPlayer;
    }*/
}

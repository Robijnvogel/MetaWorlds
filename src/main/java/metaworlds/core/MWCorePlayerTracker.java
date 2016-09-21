package metaworlds.core;

import metaworlds.patcher.EntityPlayerMPSubWorldProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class MWCorePlayerTracker {
    
    @SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		NBTTagCompound entityData = event.player.getEntityData();
		
		if (entityData.hasKey("SubWorldInfo"))
		{
			NBTTagCompound subWorldData = entityData.getCompoundTag("SubWorldInfo");
			
			int worldBelowFeetId = subWorldData.getInteger("WorldBelowFeetId");
			World newWorldBelowFeet = event.player.worldObj.getParentWorld().getSubWorld(worldBelowFeetId);
			
			if (worldBelowFeetId != 0 && newWorldBelowFeet != null)
			{
				double posXOnSubWorld = subWorldData.getDouble("posXOnSubWorld");
				double posYOnSubWorld = subWorldData.getDouble("posYOnSubWorld");
				double posZOnSubWorld = subWorldData.getDouble("posZOnSubWorld");
				
				event.player.setWorldBelowFeet(newWorldBelowFeet);
				Vec3 transformedPos = newWorldBelowFeet.transformToGlobal(posXOnSubWorld, posYOnSubWorld, posZOnSubWorld);
				event.player.setPositionAndUpdate(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
			}
		}
		
		MetaworldsMod.instance.networkHandler.sendTo(new SubWorldCreatePacket(event.player.worldObj.getSubWorlds().size(), event.player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])), (EntityPlayerMP)event.player);
		//PacketDispatcher.sendPacketToPlayer(new SubWorldCreatePacket(player.worldObj.getSubWorlds().size(), player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])).makePacket(), (Player)player);
	}

    @SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		NBTTagCompound entityData = event.player.getEntityData();
		
		NBTTagCompound subWorldData = new NBTTagCompound();
		subWorldData.setInteger("WorldBelowFeetId", event.player.getWorldBelowFeet().getSubWorldID());
		Vec3 transformedPos = event.player.getLocalPos(event.player.getWorldBelowFeet());
		subWorldData.setDouble("posXOnSubWorld", transformedPos.xCoord);
		subWorldData.setDouble("posYOnSubWorld", transformedPos.yCoord);
		subWorldData.setDouble("posZOnSubWorld", transformedPos.zCoord);
		
		entityData.setTag("SubWorldInfo", subWorldData);
	}
    
    @SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		//Destroy all old SubWorlds
        MetaworldsMod.instance.networkHandler.sendTo(new SubWorldDestroyPacket(-1, null), (EntityPlayerMP)event.player);
		//PacketDispatcher.sendPacketToPlayer(new SubWorldDestroyPacket(-1, null).makePacket(), (Player)player);
		//Create all new SubWorlds
        MetaworldsMod.instance.networkHandler.sendTo(new SubWorldCreatePacket(event.player.worldObj.getSubWorlds().size(), event.player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])), (EntityPlayerMP)event.player);
		//PacketDispatcher.sendPacketToPlayer(new SubWorldCreatePacket(player.worldObj.getSubWorlds().size(), player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])).makePacket(), (Player)player);
	}

    @SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		//Destroy all old SubWorlds
        MetaworldsMod.instance.networkHandler.sendTo(new SubWorldDestroyPacket(-1, null), (EntityPlayerMP)event.player);
		//PacketDispatcher.sendPacketToPlayer(new SubWorldDestroyPacket(-1, null).makePacket(), (Player)player);
		//Create all new SubWorlds
        MetaworldsMod.instance.networkHandler.sendTo(new SubWorldCreatePacket(event.player.worldObj.getSubWorlds().size(), event.player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])), (EntityPlayerMP)event.player);
		//PacketDispatcher.sendPacketToPlayer(new SubWorldCreatePacket(player.worldObj.getSubWorlds().size(), player.worldObj.getSubWorldsMap().keySet().toArray(new Integer[0])).makePacket(), (Player)player);
	}

}

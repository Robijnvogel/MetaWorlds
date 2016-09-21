package metaworlds.core;

import java.util.Collection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import metaworlds.api.SubWorld;
import metaworlds.patcher.SubWorldInfoHolder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHookContainer 
{
    @SubscribeEvent
	public void worldLoaded(WorldEvent.Load event)
	{
		if (event.world.isRemote)
			return;
		
		if (event.world.isSubWorld())
		{
		    MetaworldsMod.instance.networkHandler.sendToDimension(new SubWorldCreatePacket(1, new Integer[]{event.world.getSubWorldID()}), event.world.provider.dimensionId);
			//PacketDispatcher.sendPacketToAllInDimension(new SubWorldCreatePacket(1, new Integer[]{event.world.getSubWorldID()}).makePacket(), event.world.provider.dimensionId);
			
			return;
		}
    	
		//Server-side: load subworlds of this world
		Collection<Integer> subWorldIDs = DimensionManager.getWorld(0).getWorldInfo().getSubWorldIDs(((WorldServer)event.world).getDimension());
		if (subWorldIDs != null)
		{
			for (Integer curSubWorldID : subWorldIDs)
			{
				event.world.CreateSubWorld(curSubWorldID);
			}
		}
	}
	
    @SubscribeEvent
	public void canUpdateEntity(EntityEvent.CanUpdate event)
	{
		if (event.entity.worldObj.isSubWorld())
		{
			Vec3 transformedPos = event.entity.getGlobalPos();
			int i = MathHelper.floor_double(transformedPos.xCoord);
	        int j = MathHelper.floor_double(transformedPos.zCoord);
	        
	        boolean isForced = event.entity.worldObj.getParentWorld().getPersistentChunks().containsKey(new ChunkCoordIntPair(i >> 4, j >> 4));
	        byte b0 = isForced ? (byte)0 : 32;
	        boolean canUpdate = event.entity.worldObj.getParentWorld().checkChunksExist(i - b0, 0, j - b0, i + b0, 0, j + b0);
	        
	        if (canUpdate)
	        	event.canUpdate = true;
		}
	}
}

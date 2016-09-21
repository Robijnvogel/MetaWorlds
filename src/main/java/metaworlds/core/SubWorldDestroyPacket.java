package metaworlds.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import metaworlds.api.SubWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubWorldDestroyPacket extends MetaWorldsPacket
{
	public int subWorldsCount;
	public Integer[] subWorldIDs;
	
	public SubWorldDestroyPacket() { }
	
	//numSubWorldsToDestroy = -1 means Destroy All
	public SubWorldDestroyPacket(int numSubWorldsToDestroy, Integer[] subWorldIDsArray)
	{
		this.subWorldsCount = numSubWorldsToDestroy;
		this.subWorldIDs = subWorldIDsArray;
	}
	
	@Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
	    this.subWorldsCount = buf.readInt();
        
        if (this.subWorldsCount != -1)
        {
            this.subWorldIDs = new Integer[this.subWorldsCount];
            
            for (int i = 0; i < this.subWorldsCount; ++i)
            {
                this.subWorldIDs[i] = buf.readInt();
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.subWorldsCount);
        
        if (this.subWorldsCount != -1)
            for (Integer curSubWorldID : this.subWorldIDs)
                buf.writeInt(curSubWorldID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx)
    {
    	if (side.isServer())
    		return;
    	
    	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    	if (this.subWorldsCount == -1)
    	{
    		//Destroy all
    		while (!player.worldObj.getSubWorldsMap().isEmpty())
    		{
    			SubWorld curSubWorld = (SubWorld)player.worldObj.getSubWorlds().iterator().next();
    			curSubWorld.removeSubWorld();
    			
    			//Remove proxy player
    			player.playerProxyMap.remove(curSubWorld.getSubWorldID());
    		}
    	}
    	else
    	{
    		for (Integer curSubWorldID : this.subWorldIDs)
    		{
    			SubWorld curSubWorld = (SubWorld)player.worldObj.getSubWorld(curSubWorldID);
    			curSubWorld.removeSubWorld();
    			
    			//Remove proxy player
    			player.playerProxyMap.remove(curSubWorld.getSubWorldID());
    		}
    	}
    }
}

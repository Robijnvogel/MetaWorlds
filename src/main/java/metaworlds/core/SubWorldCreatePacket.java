package metaworlds.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubWorldCreatePacket extends MetaWorldsPacket
{
	public int subWorldsCount;
	public Integer[] subWorldIDs;
	
	public SubWorldCreatePacket() { }
	
	public SubWorldCreatePacket(int numSubWorldsToCreate, Integer[] subWorldIDsArray)
	{
		this.subWorldsCount = numSubWorldsToCreate;
		this.subWorldIDs = subWorldIDsArray;
	}
	
	@Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
        this.subWorldsCount = buf.readInt();
        this.subWorldIDs = new Integer[this.subWorldsCount];
        
        for (int i = 0; i < this.subWorldsCount; ++i)
        {
            this.subWorldIDs[i] = buf.readInt();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.subWorldsCount);
        
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
        for (Integer curSubWorldID : this.subWorldIDs)
        {
            //Client-side: create new subworld
            World newSubWorld = player.worldObj.CreateSubWorld(curSubWorldID);
            
            //The constructor assigns the proxy to the real player
            //MOVED THIS TO WorldClient.CreateSubWorld !
            //EntityClientPlayerMPSubWorldProxy proxyPlayer = new EntityClientPlayerMPSubWorldProxy((EntityClientPlayerMP)player, newSubWorld);
                    //this.realPlayer.playerProxyMap.put(targetSubWorld.getSubWorldID(), this);
        }
    }
}

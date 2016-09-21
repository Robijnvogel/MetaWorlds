package metaworlds.patcher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.collect.BiMap;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import metaworlds.core.MetaWorldsPacket;
import metaworlds.core.MetaworldsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

public class SSubWorldProxyPacket extends MetaWorldsPacket {
	public int subWorldID;
	public Packet actualPacket;
	public NetworkManager networkManager;
	
	public SSubWorldProxyPacket() { }
	public SSubWorldProxyPacket(int targetSubWorld, Packet packetToWrap, NetworkManager networkManagerPar)
	{
		this.subWorldID = targetSubWorld;
		this.actualPacket = packetToWrap;
		
		this.networkManager = networkManagerPar;
	}
    
	@Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
        FMLProxyPacket fmlProxyPacket = ctx.attr(MetaworldsMod.instance.networkHandler.getInboundPacketTrackerAttributeKey()).get().get();
        if (fmlProxyPacket.getTarget().isClient())
            this.networkManager = ((NetHandlerPlayClient)fmlProxyPacket.handler()).getNetworkManager();
        else
            return;//this.networkManager = ((NetHandlerPlayServer)fmlProxyPacket.handler()).netManager;
        
		this.subWorldID = buf.readInt();
		PacketBuffer packetbuffer = new PacketBuffer(buf);
		int packetId = packetbuffer.readVarIntFromBuffer();
		this.actualPacket = Packet.generatePacket((BiMap)this.networkManager.channel().attr(NetworkManager.attrKeyReceivable).get(), packetId);
		try
        {
            this.actualPacket.readPacketData(packetbuffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	@Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.subWorldID);
        
		Integer integer = (Integer)((BiMap)this.networkManager.channel().attr(NetworkManager.attrKeySendable).get()).inverse().get(this.actualPacket.getClass());
		
		PacketBuffer packetbuffer = new PacketBuffer(buf);
        packetbuffer.writeVarIntToBuffer(integer.intValue());
        try
        {
            this.actualPacket.writePacketData(packetbuffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx)
    {
        EntityPlayer player;
        if (side.isClient())
            player = Minecraft.getMinecraft().thePlayer;
        else
            player = ((NetHandlerPlayServer)netHandler).playerEntity;
            
        EntityPlayerProxy playerProxy = player.playerProxyMap.get(this.subWorldID);
		
		if (playerProxy == null)
			return;
		
		INetHandler proxyHandler = playerProxy.getNetHandlerProxy();
		
		this.actualPacket.processPacket(proxyHandler);
	}
}

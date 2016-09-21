package metaworlds.patcher;

import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;

import metaworlds.core.GeneralPacketPipeline;
import metaworlds.core.MetaworldsMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

public class NetworkManagerSubWorldProxy extends NetworkManager {

	private NetworkManager parentNetworkManager;
	private INetHandler netHandlerProxy;//netHandler proxy for this subworld's playerProxy
	private int subWorldID;
	
	protected boolean clientSide;
	
	public NetworkManagerSubWorldProxy(NetworkManager originalNetworkManager, int targetSubWorldID, boolean isClientSide)
	{
	    super(isClientSide);
	    this.clientSide = isClientSide;
		this.parentNetworkManager = originalNetworkManager;
		this.subWorldID = targetSubWorldID;
	}
	
	@Override
	public void setNetHandler(INetHandler nethandler) {
		this.netHandlerProxy = nethandler;
	}
	
	@Override
	public void scheduleOutboundPacket(Packet p_150725_1_, GenericFutureListener ... p_150725_2_)
    {
	    GeneralPacketPipeline pipeline = MetaworldsMod.instance.networkHandler;
	    
	    if (clientSide)
	    {
	        CSubWorldProxyPacket proxyPacket = new CSubWorldProxyPacket(this.subWorldID, p_150725_1_, this.parentNetworkManager);
	        pipeline.sendToServer(proxyPacket);
	    }
	    else
	    {
	        SSubWorldProxyPacket proxyPacket = new SSubWorldProxyPacket(this.subWorldID, p_150725_1_, this.parentNetworkManager);
	        EntityPlayerMP player = ((NetHandlerPlayServer)this.parentNetworkManager.getNetHandler()).playerEntity;
	        pipeline.sendTo(proxyPacket, player);
	    }
	    
	    //this.parentNetworkManager.scheduleOutboundPacket(proxyPacket, p_150725_2_);
    }

	@Override
	public void processReceivedPackets()
    { }

	@Override
	public SocketAddress getSocketAddress() {
		return null;
	}

	@Override
	public Channel channel()
	{
	    return this.parentNetworkManager.channel();
	}
}

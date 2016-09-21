package metaworlds.admin;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import metaworlds.core.MetaWorldsPacket;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.relauncher.Side;

public class MwAdminClientActionPacket extends MetaWorldsPacket {
    private int actionId;
    private int actionParameter;
    
    public MwAdminClientActionPacket()
    { }
    
    public MwAdminClientActionPacket(int parActionId)
    {
        this(parActionId, 0);
    }
    
    public MwAdminClientActionPacket(int parActionId, int parActionParameter)
    {
        this.actionId = parActionId;
        this.actionParameter = parActionParameter;
    }
    
    @Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
        this.actionId = buf.readInt();
        this.actionParameter = buf.readInt();
    }

    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.actionId);
        buf.writeInt(this.actionParameter);
    }

    @Override
    public void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx)
    {
        if (side.isClient())
            return;
        
        EntityPlayerMP player = ((NetHandlerPlayServer)netHandler).playerEntity;
        
        if (player.openContainer == null || !(player.openContainer instanceof MwAdminContainer))
            return;
        
        if (this.actionId == 1)
            ((MwAdminContainer)player.openContainer).sendSubWorldInfos();
        else if (this.actionId == 2)
            ((MwAdminContainer)player.openContainer).loadAndSendSaves();
        else if (this.actionId == 101)
            ((MwAdminContainer)player.openContainer).teleportPlayerToSubWorld(this.actionParameter);
        else if (this.actionId == 102)
            ((MwAdminContainer)player.openContainer).teleportSubWorldToPlayer(this.actionParameter);
        else if (this.actionId == 103)
            ((MwAdminContainer)player.openContainer).spawnSubWorld(this.actionParameter);
        else if (this.actionId == 104)
            ((MwAdminContainer)player.openContainer).despawnSubWorld(this.actionParameter);
        else if (this.actionId == 105)
            ((MwAdminContainer)player.openContainer).stopSubWorldMotion(this.actionParameter);
        else if (this.actionId == 106)
            ((MwAdminContainer)player.openContainer).resetSubWorldScale(this.actionParameter);
        else if (this.actionId == 201)
            ((MwAdminContainer)player.openContainer).importSubWorld(this.actionParameter & 0xFFF, this.actionParameter >> 12);
    }
}

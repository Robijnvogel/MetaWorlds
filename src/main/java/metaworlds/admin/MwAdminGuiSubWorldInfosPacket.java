package metaworlds.admin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import metaworlds.admin.MwAdminContainer.AdminSubWorldInfo;
import metaworlds.core.MetaWorldsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.network.INetHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;

public class MwAdminGuiSubWorldInfosPacket extends MetaWorldsPacket {
    
    protected Collection<AdminSubWorldInfo> adminSubWorldInfos;
    
    public MwAdminGuiSubWorldInfosPacket()
    { }
    
    public MwAdminGuiSubWorldInfosPacket(Collection<AdminSubWorldInfo> parAdminSubWorldInfos)
    {
        this.adminSubWorldInfos = parAdminSubWorldInfos;
    }

    @Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
        this.adminSubWorldInfos = new ArrayList<AdminSubWorldInfo>();
        
        int entriesCount = buf.readInt();
        
        for (int i = 0; i < entriesCount; ++i)
        {
            int curSubWorldId = buf.readInt();
            boolean curIsSpawned = buf.readBoolean();
            int curDimensionId = buf.readInt();
            
            this.adminSubWorldInfos.add(new AdminSubWorldInfo(curSubWorldId, curIsSpawned, curDimensionId));
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.adminSubWorldInfos.size());
        
        for (AdminSubWorldInfo curInfo : this.adminSubWorldInfos)
        {
            buf.writeInt(curInfo.subWorldId);
            buf.writeBoolean(curInfo.isSpawned);
            buf.writeInt(curInfo.dimensionId);
        }
    }

    @Override
    public void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx)
    {
        if (side.isServer())
            return;
        
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMwAdmin)
        {
            ((GuiMwAdmin)Minecraft.getMinecraft().currentScreen).guiSubWorldsList.adminSubWorldInfos = (List)this.adminSubWorldInfos;
        }
    }
}

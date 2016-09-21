package metaworlds.admin;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import metaworlds.admin.MwAdminContainer.SaveGameInfo;
import metaworlds.admin.MwAdminContainer.SaveGameSubWorldInfo;
import metaworlds.core.MetaWorldsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;

public class MwAdminGuiInitPacket extends MetaWorldsPacket {
    
    public List<SaveGameInfo> saveGameInfos;
    
    public MwAdminGuiInitPacket()
    { }
    
    public MwAdminGuiInitPacket(List<SaveGameInfo> parInfosList)
    {
        this.saveGameInfos = parInfosList;
    }

    @Override
    public void read(ChannelHandlerContext ctx, ByteBuf buf)
    {
        this.saveGameInfos = new ArrayList<SaveGameInfo>();
        int entryCount = buf.readInt();
        
        for (int i = 0; i < entryCount; ++i)
        {
            SaveGameInfo curGameInfo = new SaveGameInfo(ByteBufUtils.readUTF8String(buf), null);
            this.saveGameInfos.add(curGameInfo);
            
            int subWorldsCount = buf.readInt();
            
            for (int j = 0; j < subWorldsCount; ++j)
            {
                curGameInfo.subWorldsList.add(new SaveGameSubWorldInfo(ByteBufUtils.readUTF8String(buf), null, 0));
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf buf)
    {
        buf.writeInt(this.saveGameInfos.size());
        
        for (SaveGameInfo curInfo : this.saveGameInfos)
        {
            ByteBufUtils.writeUTF8String(buf, curInfo.worldFileName);
            buf.writeInt(curInfo.subWorldsList.size());
            
            for (SaveGameSubWorldInfo curSubWorldInfo : curInfo.subWorldsList)
            {
                ByteBufUtils.writeUTF8String(buf, curSubWorldInfo.subWorldName);
            }
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
            ((GuiMwAdmin)Minecraft.getMinecraft().currentScreen).guiImportWorldsList.worldsList = this.saveGameInfos;
        }
    }
}

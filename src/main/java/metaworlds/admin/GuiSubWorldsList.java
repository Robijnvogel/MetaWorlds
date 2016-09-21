package metaworlds.admin;

import java.util.List;

import metaworlds.admin.MwAdminContainer.AdminSubWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.client.GuiScrollingList;

public class GuiSubWorldsList extends GuiScrollingList {
    List<AdminSubWorldInfo> adminSubWorldInfos;
    GuiMwAdmin parent;
    
    protected int selElement = -1;

    public GuiSubWorldsList(GuiMwAdmin parParent, int width, int height, int top, int bottom, int left, int entryHeight)
    {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight);
        this.parent = parParent;
    }

    @Override
    protected int getSize()
    {
        if (this.adminSubWorldInfos == null)
            return 0;
        else
            return this.adminSubWorldInfos.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        this.selElement = index;
    }

    @Override
    protected boolean isSelected(int index)
    {
        return index == this.selElement;
    }

    @Override
    protected void drawBackground()
    {
        
    }

    @Override
    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5)
    {
        int color = 0xFFFFFF;
        
        AdminSubWorldInfo curInfo = this.adminSubWorldInfos.get(listIndex);
        if (curInfo.subWorldId == this.parent.guiPlayer.getWorldBelowFeet().getSubWorldID())
            color = 0x00FF00;
        else if (!curInfo.isSpawned)
            color = 0x404040;
        
        String activeString = "Active: ";
        if (curInfo.isSpawned)
        {
            activeString += "Yes";
            activeString += "  Dimension: " + curInfo.dimensionId;
        }
        else
            activeString += "No";
        
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(curInfo.toString(), listWidth - 10), this.left + 3 , var3 + 2, color);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(activeString, listWidth - 10), this.left + 3 , var3 + 12, color);
    }

}

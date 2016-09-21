package metaworlds.admin;

import java.util.List;

import metaworlds.admin.MwAdminContainer.SaveGameSubWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.client.GuiScrollingList;

public class GuiImportSubWorldsList extends GuiScrollingList {
    
    public List<SaveGameSubWorldInfo> subWorldsList;
    GuiMwAdmin parent;
    
    protected int selElement = -1;

    public GuiImportSubWorldsList(GuiMwAdmin parParent, int width, int height, int top, int bottom, int left, int entryHeight)
    {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight);
        this.parent = parParent;
    }

    @Override
    protected int getSize()
    {
        if (this.subWorldsList == null)
            return 0;
        else
            return this.subWorldsList.size();
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
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(this.subWorldsList.get(listIndex).subWorldName, listWidth - 10), this.left + 3 , var3 + 2, 0xFFFFFF);
        //this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mc.getDisplayVersion(), listWidth - 10), this.left + 3 , var3 + 12, 0xCCCCCC);
        //this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mc.getMetadata() !=null ? mc.getMetadata().getChildModCountString() : "Metadata not found", listWidth - 10), this.left + 3 , var3 + 22, 0xCCCCCC);
    }

}

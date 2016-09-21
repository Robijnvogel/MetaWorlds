package metaworlds.admin;

import java.util.List;

import metaworlds.admin.MwAdminContainer.SaveGameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.client.GuiScrollingList;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.LoaderState.ModState;

public class GuiImportWorldsList extends GuiScrollingList {
    
    public List<SaveGameInfo> worldsList;
    GuiMwAdmin parent;
    
    protected int selElement = -1;

    public GuiImportWorldsList(GuiMwAdmin parParent, int width, int height, int top, int bottom, int left, int entryHeight)
    {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight);
        this.parent = parParent;
    }

    @Override
    protected int getSize()
    {
        if (this.worldsList == null)
            return 0;
        else
            return this.worldsList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        this.selElement = index;
        
        parent.guiImportSubWorldsList.subWorldsList = this.worldsList.get(index).subWorldsList;
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
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(this.worldsList.get(listIndex).worldFileName, listWidth - 10), this.left + 3 , var3 + 2, 0xFFFFFF);
        //this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mc.getDisplayVersion(), listWidth - 10), this.left + 3 , var3 + 12, 0xCCCCCC);
        //this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mc.getMetadata() !=null ? mc.getMetadata().getChildModCountString() : "Metadata not found", listWidth - 10), this.left + 3 , var3 + 22, 0xCCCCCC);
    }

}

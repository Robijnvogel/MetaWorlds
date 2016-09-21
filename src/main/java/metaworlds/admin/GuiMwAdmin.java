package metaworlds.admin;

import metaworlds.core.MetaworldsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class GuiMwAdmin extends GuiScreen {
    protected EntityPlayer guiPlayer;
    protected GuiImportWorldsList guiImportWorldsList;
    protected GuiImportSubWorldsList guiImportSubWorldsList;
    protected GuiSubWorldsList guiSubWorldsList;
    
    protected GuiButton buttonTeleportToSubWorld;
    protected GuiButton buttonTeleportSubWorldHere;
    
    protected GuiButton buttonSpawnSubWorld;
    protected GuiButton buttonDespawnSubWorld;
    
    protected GuiButton buttonStopMovement;
    protected GuiButton buttonResetScale;
    
    protected GuiButton buttonImportSelectedSubWorld;
    
    protected int currentTab = 1;
    
    public GuiMwAdmin(EntityPlayer playerPar)
    {
        this.guiPlayer = playerPar;
        
        MetaworldsMod.instance.networkHandler.sendToServer(new MwAdminClientActionPacket(1));
    }
    
    @Override
    public void initGui()
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 180, this.height / 16, 70, 20, "SubWorlds"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 180 + 70 + 4, this.height / 16, 70, 20, "Import"));
        
        int listTop = this.height / 16 + 20 + 4;
        int listBottom = this.height - 38;
        
        this.buttonList.add(this.buttonTeleportToSubWorld = new GuiButton(101, this.width / 2 - 140 - 2, this.height - 30, 140, 20, "Teleport to SubWorld"));
        this.buttonList.add(this.buttonTeleportSubWorldHere = new GuiButton(102, this.width / 2 + 2, this.height - 30, 140, 20, "Teleport SubWorld here"));
        
        this.buttonList.add(this.buttonSpawnSubWorld = new GuiButton(103, this.width / 2 + 28, (listTop + listBottom) / 2 - 22, 110, 20, "Spawn SubWorld"));
        this.buttonList.add(this.buttonDespawnSubWorld = new GuiButton(104, this.width / 2 + 28, (listTop + listBottom) / 2 + 2, 110, 20, "Despawn SubWorld"));
        
        this.buttonList.add(this.buttonStopMovement = new GuiButton(105, this.width / 2 + 28, (listTop + listBottom) / 2 + 34, 110, 20, "Stop Movement"));
        this.buttonList.add(this.buttonResetScale = new GuiButton(106, this.width / 2 + 28, (listTop + listBottom) / 2 + 58, 110, 20, "Reset Scale"));
        
        this.buttonList.add(this.buttonImportSelectedSubWorld = new GuiButton(201, this.width / 2 - 75, this.height - 30, 150, 20, "Import selected SubWorld"));
        this.buttonImportSelectedSubWorld.visible = false;
        
        this.guiImportWorldsList = new GuiImportWorldsList(this, 158, Minecraft.getMinecraft().displayHeight, listTop, listBottom, this.width / 2 - 180, 20);
        this.guiImportSubWorldsList = new GuiImportSubWorldsList(this, 178, Minecraft.getMinecraft().displayHeight, listTop, listBottom, this.width / 2  - 18, 20);
        this.guiSubWorldsList = new GuiSubWorldsList(this, 200, Minecraft.getMinecraft().displayHeight, listTop, listBottom, this.width / 2 - 180, 30);
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton buttonPar)
    {
        if (buttonPar.id == 1)
        {
            MetaworldsMod.instance.networkHandler.sendToServer(new MwAdminClientActionPacket(1));
            this.currentTab = 1;
            
            this.buttonTeleportToSubWorld.visible = true;
            this.buttonTeleportSubWorldHere.visible = true;
            this.buttonSpawnSubWorld.visible = true;
            this.buttonDespawnSubWorld.visible = true;
            this.buttonStopMovement.visible = true;
            this.buttonResetScale.visible = true;
            
            this.buttonImportSelectedSubWorld.visible = false;
        }
        
        if (buttonPar.id == 2)
        {
            MetaworldsMod.instance.networkHandler.sendToServer(new MwAdminClientActionPacket(2));
            this.currentTab = 2;
            
            this.buttonTeleportToSubWorld.visible = false;
            this.buttonTeleportSubWorldHere.visible = false;
            this.buttonSpawnSubWorld.visible = false;
            this.buttonDespawnSubWorld.visible = false;
            this.buttonStopMovement.visible = false;
            this.buttonResetScale.visible = false;
            
            this.buttonImportSelectedSubWorld.visible = true;
        }
        
        if (buttonPar.id >= 101 && buttonPar.id <= 106)
        {
            if (this.guiSubWorldsList.selElement != -1)
            {
                int subWorldId = this.guiSubWorldsList.adminSubWorldInfos.get(this.guiSubWorldsList.selElement).subWorldId;
                MetaworldsMod.instance.networkHandler.sendToServer(new MwAdminClientActionPacket(buttonPar.id, subWorldId));
            }
        }
        
        if (buttonPar.id == 201)
        {
            if (this.guiImportWorldsList.selElement != -1 && this.guiImportSubWorldsList.selElement != -1 && this.guiImportSubWorldsList.selElement < this.guiImportSubWorldsList.subWorldsList.size())
            {
                int actionPar = (this.guiImportWorldsList.selElement & 0xFFF) | (this.guiImportSubWorldsList.selElement << 12);
                MetaworldsMod.instance.networkHandler.sendToServer(new MwAdminClientActionPacket(buttonPar.id, actionPar));
            }
        }
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        if (this.currentTab == 1)
        {
            this.guiSubWorldsList.drawScreen(par1, par2, par3);
        }
        else if (this.currentTab == 2)
        {
            this.guiImportWorldsList.drawScreen(par1, par2, par3);
            this.guiImportSubWorldsList.drawScreen(par1, par2, par3);
        }
        
        super.drawScreen(par1, par2, par3);
    }
    
    public FontRenderer getFontRenderer()
    {
        return this.fontRendererObj;
    }
}

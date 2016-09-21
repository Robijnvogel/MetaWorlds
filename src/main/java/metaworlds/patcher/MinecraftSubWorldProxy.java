package metaworlds.patcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.ResourcePackRepository;

public class MinecraftSubWorldProxy extends Minecraft {
	
	protected Minecraft realMinecraft;
	
	public MinecraftSubWorldProxy(Minecraft original)
	{
		super(original);
		
		this.realMinecraft = original;
	}
	
	@Override
	public void displayGuiScreen(GuiScreen par1GuiScreen)
    {
		this.realMinecraft.displayGuiScreen(par1GuiScreen);
		
		if (par1GuiScreen != null)
		{
			par1GuiScreen.setWorldAndResolution(this, par1GuiScreen.width, par1GuiScreen.height);
		}
    }
	
	@Override
	public TextureManager getTextureManager()
    {
        return this.realMinecraft.getTextureManager();
    }

	@Override
    public IResourceManager getResourceManager()
    {
        return this.realMinecraft.getResourceManager();
    }

	@Override
    public ResourcePackRepository getResourcePackRepository()
    {
        return this.realMinecraft.getResourcePackRepository();
    }

	@Override
    public LanguageManager getLanguageManager()
    {
        return this.realMinecraft.getLanguageManager();
    }
}

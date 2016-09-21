package metaworlds.patcher;

import org.lwjgl.opengl.ARBOcclusionQuery;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;

public class RenderGlobalSubWorld extends RenderGlobal// implements IWorldAccess
{
	private RenderGlobal parentRenderGlobal;
	//private World worldObj;
	
	public RenderGlobalSubWorld(Minecraft par1, RenderGlobal par2)
	{
	    super(par2, par1);
		//this.worldObj = par1;
		this.parentRenderGlobal = par2;
	}
	
	public void onWorldRemove()
	{
	    this.deleteAllDisplayLists();
	    
	    if (this.getOcclusionEnabled())
        {
            ARBOcclusionQuery.glDeleteQueriesARB(this.getOcclusionQueryBase());
        }
	}
	
	/**
     * On the client, re-renders the block. On the server, sends the block to the client (which will re-render it),
     * including the tile entity description packet if applicable. Args: x, y, z
     */
	@Override
    public void markBlockForUpdate(int par1, int par2, int par3)
    {
    	parentRenderGlobal.markBlockForUpdate(par1, par2, par3);
    }

    /**
     * On the client, re-renders this block. On the server, does nothing. Used for lighting updates.
     */
	@Override
    public void markBlockForRenderUpdate(int par1, int par2, int par3)
    {
    	parentRenderGlobal.markBlockForRenderUpdate(par1, par2, par3);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
	@Override
    public void markBlockRangeForRenderUpdate(int par1, int par2, int par3, int par4, int par5, int par6)
    {
    	parentRenderGlobal.markBlockRangeForRenderUpdate(par1, par2, par3, par4, par5, par6);
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
	@Override
    public void playSound(String par1Str, double par2, double par4, double par6, float par8, float par9)
    {
    	parentRenderGlobal.playSound(par1Str, par2, par4, par6, par8, par9);
    }

    /**
     * Plays sound to all near players except the player reference given
     */
	@Override
    public void playSoundToNearExcept(EntityPlayer par1EntityPlayer, String par2Str, double par3, double par5, double par7, float par9, float par10)
    {
    	parentRenderGlobal.playSoundToNearExcept(par1EntityPlayer, par2Str, par3, par5, par7, par9, par10);
    }

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
	@Override
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
	    super.spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
    	/*WorldClient origWorld = parentRenderGlobal.getWorld();
    	parentRenderGlobal.setWorld(this.getWorld();
    	parentRenderGlobal.spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
    	parentRenderGlobal.setWorld(origWorld);*/
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
	@Override
    public void onEntityCreate(Entity par1Entity)
    {
    	parentRenderGlobal.onEntityCreate(par1Entity);
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
	@Override
    public void onEntityDestroy(Entity par1Entity)
    {
    	parentRenderGlobal.onEntityDestroy(par1Entity);
    }

    /**
     * Plays the specified record. Arg: recordName, x, y, z
     */
	@Override
    public void playRecord(String par1Str, int par2, int par3, int par4)
    {
    	parentRenderGlobal.playRecord(par1Str, par2, par3, par4);
    }

	@Override
    public void broadcastSound(int par1, int par2, int par3, int par4, int par5)
    {
    	parentRenderGlobal.broadcastSound(par1, par2, par3, par4, par5);
    }

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
	@Override
    public void playAuxSFX(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
	    super.playAuxSFX(par1EntityPlayer, par2, par3, par4, par5, par6);
    	//parentRenderGlobal.playAuxSFX(par1EntityPlayer, par2, par3, par4, par5, par6);
    }

    /**
     * Starts (or continues) destroying a block with given ID at the given coordinates for the given partially destroyed
     * value
     */
	@Override
    public void destroyBlockPartially(int par1, int par2, int par3, int par4, int par5)
    {
    	parentRenderGlobal.destroyBlockPartially(par1, par2, par3, par4, par5, this.getWorld().getSubWorldID());
    }

    @Override
    public void onStaticEntitiesChanged()
    {
        // TODO Auto-generated method stub
    }
}

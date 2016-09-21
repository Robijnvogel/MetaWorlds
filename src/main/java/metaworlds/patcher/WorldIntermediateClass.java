package metaworlds.patcher;

import java.util.ArrayList;
import java.util.List;

import metaworlds.api.SubWorld;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

public abstract class WorldIntermediateClass extends World {
    
    private ArrayList collidingBBCacheIntermediate = new ArrayList();

	public WorldIntermediateClass(ISaveHandler par1iSaveHandler, String par2Str, WorldProvider par3WorldProvider, WorldSettings par4WorldSettings, Profiler par5Profiler) 
	{
		super(par1iSaveHandler, par2Str, par3WorldProvider, par4WorldSettings, par5Profiler);
	}

	public WorldIntermediateClass(ISaveHandler par1ISaveHandler, String par2Str, WorldSettings par3WorldSettings, WorldProvider par4WorldProvider, Profiler par5Profiler)
	{
		super(par1ISaveHandler, par2Str, par3WorldSettings, par4WorldProvider, par5Profiler);
	}
	
	@Override
	public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5)//rayTraceBlocks_do_do
    {
    	MovingObjectPosition bestResult = null;
    	//As rayTraceBlocks_do_do_single tends to alter par1Vec3 (and maybe also par2Vec3) in some cases
    	//we preserve the actual contents here so all subworlds get the original parameters
    	Vec3 vecSource = this.transformToGlobal(par1Vec3);//getWorldVec3Pool().getVecFromPool(par1Vec3.xCoord, par1Vec3.yCoord, par1Vec3.zCoord);
    	Vec3 vecDest = this.transformToGlobal(par2Vec3);//getWorldVec3Pool().getVecFromPool(par2Vec3.xCoord, par2Vec3.yCoord, par2Vec3.zCoord);
    	//par1Vec3.setComponents(vecSource.xCoord, vecSource.yCoord, vecSource.zCoord);
    	//par2Vec3.setComponents(vecDest.xCoord, vecDest.yCoord, vecDest.zCoord);
    	
    	for (World curWorld : this.getParentWorld().getWorlds())
    	{
    		MovingObjectPosition curResult = ((WorldIntermediateClass)curWorld).rayTraceBlocks_do_do_single(curWorld.transformToLocal(vecSource), curWorld.transformToLocal(vecDest), par3, par4, par5);
    		
    		if (curResult != null)
    		{
    		    curResult.worldObj = curWorld;
    			curResult.hitVec = curWorld.transformLocalToOther(this, curResult.hitVec);
    		}
    			
    		if (bestResult == null || bestResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS || (curResult != null && curResult.typeOfHit != MovingObjectPosition.MovingObjectType.MISS && bestResult.hitVec.squareDistanceTo(par1Vec3) > curResult.hitVec.squareDistanceTo(par1Vec3)))
    			bestResult = curResult;
    	}
    	
    	return bestResult;
    }

    public MovingObjectPosition rayTraceBlocks_do_do_single(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5)
    {
    	return super.func_147447_a(par1Vec3, par2Vec3, par3, par4, par5);//rayTraceBlocks_do_do
    }
    
    @Override
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	this.collidingBBCacheIntermediate.clear();
    	
    	this.collidingBBCacheIntermediate = (ArrayList)this.getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB);
    	
    	for(World curSubWorld : this.getSubWorlds())
    	{
    		this.collidingBBCacheIntermediate.addAll(((SubWorld)curSubWorld).getCollidingBoundingBoxesGlobal(par1Entity, par2AxisAlignedBB));
    	}
    	
    	return this.collidingBBCacheIntermediate;
    }
    
    public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	return super.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }
    
    @Override
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
    	if (this.isMaterialInBBLocal(par1AxisAlignedBB, par2Material))
    		return true;
    	
    	if(!this.isSubWorld())
        {
	        for (World curSubWorld : this.getSubWorlds())
	        {
	        	if(((SubWorld)curSubWorld).isMaterialInBBGlobal(par1AxisAlignedBB, par2Material))
	        		return true;
	        }
        }
    	
    	return false;
    }
    
    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
    	return super.isMaterialInBB(par1AxisAlignedBB, par2Material);
    }
    
    @Override
    public boolean isAABBInMaterial(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
    	if (super.isAABBInMaterial(par1AxisAlignedBB, par2Material))
    		return true;
    	
    	if(!this.isSubWorld())
        {
	        for (World curSubWorld : this.getSubWorlds())
	        {
	        	if(((SubWorld)curSubWorld).isAABBInMaterialGlobal(par1AxisAlignedBB, par2Material))
	        		return true;
	        }
        }
    	
    	return false;
    }
    
    @Override
    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
    	ArrayList arraylist = new ArrayList();
    	
    	arraylist.addAll(this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
    	for (World curSubWorld : this.getSubWorlds())
        {
        	arraylist.addAll(curSubWorld.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB.getTransformedToLocalBoundingBox(curSubWorld), par3IEntitySelector));
        }
    	
    	return arraylist;
    }
    
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector)null);
    }
    
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
        if (par1Entity instanceof EntityPlayer)
            par1Entity = par1Entity.getProxyPlayer(this);
        
    	return super.getEntitiesWithinAABBExcludingEntity(par1Entity, par2AxisAlignedBB, par3IEntitySelector);
    }
    
    @Override
    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
    	ArrayList arraylist = new ArrayList();
    	
    	arraylist.addAll(this.selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
    	for (World curSubWorld : this.getSubWorlds())
        {
        	arraylist.addAll(((WorldIntermediateClass)curSubWorld).selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB.getTransformedToLocalBoundingBox(curSubWorld), par3IEntitySelector));
        }
    	
    	return arraylist;
    }

    public List selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
    	return super.selectEntitiesWithinAABB(par1Class, par2AxisAlignedBB, par3IEntitySelector);
    }
    
    @Override
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
    	boolean result = super.spawnEntityInWorld(par1Entity);
    	
    	//Server side only
    	if (!this.isRemote && !this.isSubWorld() && par1Entity instanceof EntityPlayer)
    	{
    		for (World curSubWorld : this.getSubWorlds())
    		{
    			EntityPlayer proxyPlayer = par1Entity.getProxyPlayer(curSubWorld);
    			
    			if (proxyPlayer == null)
    			{
    				proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP)par1Entity, curSubWorld);
    				
    				//TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the real player
    	    		
    	    		((EntityPlayerMP)proxyPlayer).theItemInWorldManager.setWorld((WorldServer)curSubWorld);
    			}
	    		
	    		curSubWorld.spawnEntityInWorld(proxyPlayer);
    		}
    	}
    	
    	return result;
    }
    
    @Override
    public void removeEntity(Entity par1Entity)
    {
    	super.removeEntity(par1Entity);
    	
    	//Server side only
    	if (!this.isRemote && !this.isSubWorld() && par1Entity instanceof EntityPlayer)
    	{
    		for (World curSubWorld : this.getSubWorlds())
    		{
	    		EntityPlayer proxyPlayer = par1Entity.getProxyPlayer(curSubWorld);
	    		
	    		if (proxyPlayer == null)
	    		    continue;//this can happen if the client closed the connection e.g. due to missing mods
	    		
	    		curSubWorld.removeEntity(proxyPlayer);
	    		
	    		//Sadly we need to do this check because minecraft in inconsequentian in the order it removes the player from a)the World and b)the PlayerManager
	    		if (!((WorldServer)curSubWorld).getPlayerManager().getPlayers().contains(proxyPlayer))
	    			par1Entity.playerProxyMap.remove(curSubWorld.getSubWorldID());//removing in player manager now!
    		}
    	}
    }
    
    @Override
    public void removePlayerEntityDangerously(Entity par1Entity)
    {
    	super.removePlayerEntityDangerously(par1Entity);
    	
    	//Server side only
    	if (!this.isRemote && !this.isSubWorld() && par1Entity instanceof EntityPlayer)
    	{
    		for (World curSubWorld : this.getSubWorlds())
    		{
	    		EntityPlayer proxyPlayer = par1Entity.getProxyPlayer(curSubWorld);
	    		
	    		curSubWorld.removeEntity(proxyPlayer);
	    		
	    		//Sadly we need to do this check because minecraft in inconsequentian in the order it removes the player from a)the World and b)the PlayerManager
	    		if (!((WorldServer)curSubWorld).getPlayerManager().getPlayers().contains(proxyPlayer))
	    			par1Entity.playerProxyMap.remove(curSubWorld.getSubWorldID());//removing in player manager now!
    		}
    	}
    }
}

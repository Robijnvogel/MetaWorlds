package metaworlds.patcher;

import java.util.HashMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import metaworlds.api.EntitySuperClass;
import metaworlds.api.SubWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityDraggableBySubWorld implements Comparable, EntitySuperClass 
{
	public World worldBelowFeet;
	protected byte tractionLoss;//Value indicating how much traction is lost (in ticks since ground was left)
	public static final byte tractionLossThreshold = 20;//Value at which the traction is completely lost and the worldBelowFeet is reset
	protected boolean losingTraction = false;
	
	public double getTractionFactor()
	{
	    return 1.0d - ((double)tractionLoss * (double)tractionLoss) / 400.0d;
	}
	
	public byte getTractionLossTicks()
	{
	    return this.tractionLoss;
	}
	
	public void setTractionTickCount(byte newTickCount)
	{
	    this.tractionLoss = newTickCount;
	}
	
	public boolean isLosingTraction()
	{
	    return this.losingTraction;
	}
    
    public void slowlyRemoveWorldBelowFeet()
    {
        this.losingTraction = true;
        //this.tractionLoss = 0;
    }
	
	// for client use only
    public int serverPosXOnSubWorld;
    public int serverPosYOnSubWorld;
    public int serverPosZOnSubWorld;
	
	public void setWorldBelowFeet(World newWorldBelowFeet)
	{
	    this.losingTraction = false;
	    this.tractionLoss = 0;
	    
		if (newWorldBelowFeet == this.worldBelowFeet)
			return;
		
		if (this.worldBelowFeet != null && this.worldBelowFeet.isSubWorld())
		{
			((SubWorld)this.worldBelowFeet).unregisterEntityToDrag(this);
		}
		
		this.worldBelowFeet = newWorldBelowFeet;
		
		if (this.worldBelowFeet != null && this.worldBelowFeet.isSubWorld())
		{
			((SubWorld)this.worldBelowFeet).registerEntityToDrag(this);
		}
		
		if (this.worldBelowFeet != ((Entity)this).worldObj && ((Entity)this).worldObj.isSubWorld())
		{
			((SubWorld)((Entity)this).worldObj).registerDetachedEntity(this);
		}
		else if (this.worldBelowFeet == ((Entity)this).worldObj && ((Entity)this).worldObj.isSubWorld())
		{
			((SubWorld)((Entity)this).worldObj).unregisterDetachedEntity(this);
		}
	}
	
	public World getWorldBelowFeet()
	{
		if (worldBelowFeet == null)
			return ((Entity)this).worldObj;
		
		return worldBelowFeet;
	}
	
	public int compareTo(Object par1Obj)
    {
		if (this instanceof Entity && par1Obj instanceof Entity)
		{
			return ((Entity)par1Obj).getEntityId() - ((Entity)this).getEntityId();
		}
		else 
			return 0;
    }
	
	public Vec3 getGlobalPos()
	{
		return ((Entity)this).worldObj.transformToGlobal((Entity)this); 
	}
	
	public Vec3 getLocalPos(World referenceWorld)
	{
		Entity eThis = (Entity)this; 
		if (referenceWorld == null && eThis.worldObj == null)
			return Vec3.createVectorHelper(eThis.posX, eThis.posY, eThis.posZ);
		
		if (referenceWorld == eThis.worldObj || referenceWorld == null)
			return eThis.worldObj.getWorldVec3Pool().getVecFromPool(eThis.posX, eThis.posY, eThis.posZ);
		
		return referenceWorld.transformToLocal(this.getGlobalPos());
	}
	
	public double getGlobalRotationYaw()
	{
		return ((Entity)this).rotationYaw - ((Entity)this).worldObj.getRotationYaw();
	}
	
	public double getDistanceSq(double par1, double par3, double par5, World targetWorld)
    {
		Vec3 thisVecGlobal = this.getGlobalPos();
		Vec3 targetVecGlobal = targetWorld.transformToGlobal(par1, par3, par5);
		return targetVecGlobal.squareDistanceTo(thisVecGlobal);
    }
	
	public boolean getIsJumping()
	{
		if (this instanceof EntityLivingBase)
			return ((EntityLivingBase)this).isJumping;
		
		return false;
	}
	
	//Players only
	public HashMap<Integer, EntityPlayerProxy> playerProxyMap = (this instanceof EntityPlayer) ? new HashMap<Integer, EntityPlayerProxy>() : null;
	public EntityPlayer getProxyPlayer(World subWorld) { return getProxyPlayer(subWorld.getSubWorldID()); }
	public EntityPlayer getProxyPlayer(int subWorldID)
	{
		if (subWorldID == 0)
			return (EntityPlayer)this;
		
		return (EntityPlayer)this.playerProxyMap.get(subWorldID);
	}
}

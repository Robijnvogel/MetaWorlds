package metaworlds.patcher;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityIntermediateClass extends EntityLivingBase {
	private boolean isPlayerProxy = this instanceof EntityPlayerProxy;
	
	private static boolean isTransformingClient = false;
	private static boolean isTransformingServer = false;

	public EntityIntermediateClass(World par1World) {
		super(par1World);
	}
	
	@Override
	public void setPosition(double par1, double par3, double par5)
	{
		super.setPosition(par1, par3, par5);
		
		if (!tryLockTransformations())
            return;
		
		if (this.isPlayerProxy)
		{
			EntityPlayerProxy proxyThis = (EntityPlayerProxy)this;
			Vec3 transformedToGlobalPos = this.getGlobalPos();
			EntityPlayer realPlayer = proxyThis.getRealPlayer();
			
			//This happens if this method is called from the constructor of a player entity
			if (realPlayer == null)
			{
			    releaseTransformationLock();
				return;
			}
			
			realPlayer.setPositionLocal(transformedToGlobalPos.xCoord, transformedToGlobalPos.yCoord, transformedToGlobalPos.zCoord);
			
			for (EntityPlayerProxy curProxy : realPlayer.playerProxyMap.values())
			{
				if (curProxy == this)
					continue;
				
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(transformedToGlobalPos);
				curProxyPlayer.setPositionLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord);
			}
		}
		else if (this.isPlayer())
		{
			for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
			{
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(par1, par3, par5);
				curProxyPlayer.setPositionLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord);
			}
		}
		
		releaseTransformationLock();
	}
	
	public void setPositionLocal(double par1, double par3, double par5)
	{
		super.setPosition(par1, par3, par5);
	}
	
	@Override
    public void setRotation(float par1, float par2)
    {
        super.setRotation(par1, par2);
        
        if (!tryLockTransformations())
            return;
        
        if (this.isPlayerProxy)
        {
            EntityPlayerProxy proxyThis = (EntityPlayerProxy)this;
            EntityPlayer realPlayer = proxyThis.getRealPlayer();
            
            //This happens if this method is called from the constructor of a player entity
            if (realPlayer == null)
            {
                releaseTransformationLock();
                return;
            }
            
            realPlayer.setRotation(transformYawToGlobal(par1, this), par2);
            
            for (EntityPlayerProxy curProxy : realPlayer.playerProxyMap.values())
            {
                if (curProxy == this)
                    continue;
                
                EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
                curProxyPlayer.setRotation(transformYawToLocal(realPlayer.rotationYaw, curProxyPlayer), par2);
            }
        }
        else if (this.isPlayer())
        {
            for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
            {
                EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
                curProxyPlayer.setRotation(transformYawToLocal(par1, curProxyPlayer), par2);
            }
        }
        
        releaseTransformationLock();
    }
	
    public void setRotationLocal(float par1, float par2)
    {
        super.setRotation(par1, par2);
    }
	
	@Override
	public void moveEntity(double par1, double par3, double par5)
	{
		super.moveEntity(par1, par3, par5);
		
		if (this.isPlayerProxy || this.isPlayer())
		{
			//For the sake of not having to copy paste everything let's do the synchronization with proxy players this way:
			this.setPosition(this.posX, this.posY, this.posZ);
			
			if (!this.isPlayerProxy)
			{
			    for (EntityPlayerProxy curProxyPlayer : this.playerProxyMap.values())
			    {
			        ((EntityPlayer)curProxyPlayer).onGround = this.onGround;
			    }
			}
		}
	}
	
	@Override
	public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8)
	{
		super.setPositionAndRotation(par1, par3, par5, par7, par8);
		
		if (!tryLockTransformations())
            return;
		
		if (this.isPlayerProxy)
		{
			EntityPlayerProxy proxyThis = (EntityPlayerProxy)this;
			Vec3 transformedToGlobalPos = this.getGlobalPos();
			EntityPlayer realPlayer = proxyThis.getRealPlayer();
			
			//This happens if this method is called from the constructor of a player entity
			if (realPlayer == null)
			{
			    releaseTransformationLock();
				return;
			}
			
			realPlayer.setPositionAndRotationLocal(transformedToGlobalPos.xCoord, transformedToGlobalPos.yCoord, transformedToGlobalPos.zCoord, transformYawToGlobal(par7, this), par8);
			
			for (EntityPlayerProxy curProxy : realPlayer.playerProxyMap.values())
			{
				if (curProxy == this)
					continue;
				
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(transformedToGlobalPos);
				curProxyPlayer.setPositionAndRotationLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord, transformYawToLocal(realPlayer.rotationYaw, curProxyPlayer), par8);
			}
		}
		else if (this.isPlayer())
		{
			for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
			{
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(par1, par3, par5);
				curProxyPlayer.setPositionAndRotationLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord, transformYawToLocal(par7, curProxyPlayer), par8);
			}
		}
		
		releaseTransformationLock();
	}
	
	public void setPositionAndRotationLocal(double par1, double par3, double par5, float par7, float par8)
	{
		super.setPositionAndRotation(par1, par3, par5, par7, par8);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setAngles(float par1, float par2)
	{
	    super.setAngles(par1, par2);
	    
	    if (!tryLockTransformations())
            return;
        
	    if (this.isPlayerProxy)
        {
            EntityPlayerProxy proxyThis = (EntityPlayerProxy)this;
            EntityPlayer realPlayer = proxyThis.getRealPlayer();
            
            //This happens if this method is called from the constructor of a player entity
            if (realPlayer == null)
            {
                releaseTransformationLock();
                return;
            }
            
            realPlayer.setAngles(par1, par2);//no need to transform this as it works incrementally and we assume subworlds only being rotated with yaw for now
            
            for (EntityPlayerProxy curProxy : realPlayer.playerProxyMap.values())
            {
                if (curProxy == this)
                    continue;
                
                EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
                curProxyPlayer.setAngles(par1, par2);
            }
        }
        else if (this.isPlayer())
        {
            for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
            {
                EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
                curProxyPlayer.setAngles(par1, par2);
            }
        }
        
        releaseTransformationLock();
	}
	
	public void setAnglesLocal(float par1, float par2)
	{
	    super.setAngles(par1, par2);
	}
	
	@Override
	public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8)
	{
		super.setLocationAndAngles(par1, par3, par5, par7, par8);
		
		if (!tryLockTransformations())
		    return;
		
		if (this.isPlayerProxy)
		{
			EntityPlayerProxy proxyThis = (EntityPlayerProxy)this;
			Vec3 transformedToGlobalPos = this.getGlobalPos();
			EntityPlayer realPlayer = proxyThis.getRealPlayer();
			
			//This happens if this method is called from the constructor of a player entity
			if (realPlayer == null)
			{
			    releaseTransformationLock();
				return;
			}
			
			realPlayer.setLocationAndAnglesLocal(transformedToGlobalPos.xCoord, transformedToGlobalPos.yCoord, transformedToGlobalPos.zCoord, transformYawToGlobal(par7, this), par8);
			
			for (EntityPlayerProxy curProxy : realPlayer.playerProxyMap.values())
			{
				if (curProxy == this)
					continue;
				
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(transformedToGlobalPos);
				curProxyPlayer.setLocationAndAnglesLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord, transformYawToLocal(realPlayer.rotationYaw, curProxyPlayer), par8);
			}
		}
		else if (this.isPlayer())
		{
			for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
			{
				EntityPlayer curProxyPlayer = (EntityPlayer)curProxy;
				Vec3 transformedToLocalPos = curProxyPlayer.worldObj.transformToLocal(par1, par3, par5);
				curProxyPlayer.setLocationAndAnglesLocal(transformedToLocalPos.xCoord, transformedToLocalPos.yCoord, transformedToLocalPos.zCoord, transformYawToLocal(par7, curProxyPlayer), par8);
			}
		}
		
		releaseTransformationLock();
	}
	
	public void setLocationAndAnglesLocal(double par1, double par3, double par5, float par7, float par8)
	{
		super.setLocationAndAngles(par1, par3, par5, par7, par8);
	}
	
	private boolean tryLockTransformations()//avoids endless recursion
	{
	    if ((this.worldObj.isRemote && this.isTransformingClient) || 
                (!this.worldObj.isRemote && this.isTransformingServer))
            return false;
        
        if (this.worldObj.isRemote)
            this.isTransformingClient = true;
        else
            this.isTransformingServer = true;
        
        return true;
	}
	
	private void releaseTransformationLock()
	{
	    if (this.worldObj.isRemote)
            this.isTransformingClient = false;
        else
            this.isTransformingServer = false;
	}
	
	private static float transformYawToGlobal(float parYaw, Entity transformingEntity)
	{
	    return (float)(parYaw - transformingEntity.worldObj.getRotationYaw());
	}
	
	private static float transformYawToLocal(float parYaw, Entity transformingEntity)
	{
        return (float)(parYaw + transformingEntity.worldObj.getRotationYaw());
    }
}

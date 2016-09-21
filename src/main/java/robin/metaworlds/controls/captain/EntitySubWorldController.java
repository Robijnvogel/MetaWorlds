package robin.metaworlds.controls.captain;

import robin.metaworlds.api.EntitySuperClass;
import robin.metaworlds.api.SubWorld;
import robin.metaworlds.api.WorldSuperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntitySubWorldController  extends Entity
{
	private boolean riseSubWorld = true;
    private boolean wasJumping = false;
    
    public World controlledWorld;
    public double startingYaw = 0.0D;
    
    public EntitySubWorldController(World par1World)
    {
    	this(par1World, null);
    }
    
	public EntitySubWorldController(World par1World, World par2ControlledWorld)
    {
        super(par1World);
        //this.field_70279_a = true;
        //this.speedMultiplier = 0.07D;
        this.preventEntitySpawning = true;
        this.setSize(1.5F, 0.6F);
        this.yOffset = 0;//this.height / 2.0F;
        this.controlledWorld = par2ControlledWorld;
    }
	
	public EntitySubWorldController(World par1World, World par2ControlledWorld, double par2, double par4, double par6)
    {
        this(par1World, par2ControlledWorld);
        this.setPosition(par2, par4/* + (double)this.yOffset*/, par6);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = par2;
        this.prevPosY = par4;
        this.prevPosZ = par6;
        
    }
	
	/**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}
    
    protected void entityInit()
    {
    	this.dataWatcher.addObject(21, new Integer(0));//SubWorldID of controlledWorld
    	this.dataWatcher.addObject(22, new Float(0));//startingYaw
    	this.dataWatcher.addObject(23, new Integer(0));//EntityID of the controlling Player/Entity
    }
    
    public void setControlledWorld(World newControlledWorld)
    {
    	this.controlledWorld = newControlledWorld;
    	
    	if (this.controlledWorld != null)
    		this.dataWatcher.updateObject(21, Integer.valueOf(((WorldSuperClass)this.controlledWorld).getSubWorldID()));
    	else
    		this.dataWatcher.updateObject(21, Integer.valueOf(0));
    }
    
    public void setStartingYaw(float newStartingYaw)
    {
    	this.startingYaw = newStartingYaw;
    	this.dataWatcher.updateObject(22, Float.valueOf(newStartingYaw));
    }
    
    public boolean interactFirst(EntityPlayer par1EntityPlayer)
    {
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != par1EntityPlayer)
        {
            return true;
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                par1EntityPlayer.mountEntity(this);
                
                if (this.riddenByEntity != null && !this.worldObj.isRemote)
                	this.dataWatcher.updateObject(23, this.riddenByEntity.getEntityId());
            }

            return true;
        }
    }
    
    public void onUpdate()
    {
        super.onUpdate();
        
        if (this.riddenByEntity == null && this.worldObj.isRemote && Minecraft.getMinecraft().renderViewEntity != null)
        {
        	if (this.dataWatcher.getWatchableObjectInt(23) == Minecraft.getMinecraft().renderViewEntity.getEntityId())
        		this.interactFirst((EntityPlayer)Minecraft.getMinecraft().renderViewEntity);
        }
        
        this.controlledWorld = ((WorldSuperClass)this.worldObj).getSubWorld(this.dataWatcher.getWatchableObjectInt(21));
        this.startingYaw = this.dataWatcher.getWatchableObjectFloat(22);
        
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        if (this.riddenByEntity != null && this.controlledWorld instanceof SubWorld)
        {
        	SubWorld subWorldObj = (SubWorld)this.controlledWorld;
        	
        	//Handle rotation
        	double sourceYaw = MathHelper.wrapAngleTo180_double(subWorldObj.getRotationYaw() + this.riddenByEntity.rotationYaw);
            double destYaw = MathHelper.wrapAngleTo180_double(this.startingYaw);
            double rotationSpeed = MathHelper.wrapAngleTo180_double(destYaw - sourceYaw) * 0.05D;
        	if (rotationSpeed > -0.25d && rotationSpeed < 0.25d)
        		rotationSpeed = 0.0d;
        	
        	subWorldObj.setRotationYawSpeed(rotationSpeed);
        	
        	//Handle linear movement
        	//XZ plane
        	double curStrafe = (double)((EntityLivingBase)this.riddenByEntity).moveStrafing;
        	double curForward = (double)((EntityLivingBase)this.riddenByEntity).moveForward;
            
            double newMotionX = subWorldObj.getMotionX();
            double newMotionZ = subWorldObj.getMotionZ();
            
            if (curForward != 0 || curStrafe != 0)
            {
            	double cosDir = Math.cos(this.riddenByEntity.rotationYaw * Math.PI / 180.0d);
            	double sinDir = Math.sin(this.riddenByEntity.rotationYaw * Math.PI / 180.0d);
                
            	double controlMagnitude = Math.sqrt(curForward * curForward + curStrafe * curStrafe);
            	
            	double accelerationFwd = curForward / controlMagnitude * 0.01d;
            	double accelerationStrafe = curStrafe / controlMagnitude * 0.01d;
            	
            	newMotionX += -accelerationFwd * sinDir + accelerationStrafe * cosDir;
            	newMotionZ += accelerationFwd * cosDir + accelerationStrafe * sinDir;
            }
            else
            {
            	newMotionX -= subWorldObj.getMotionX() * 0.1d;
            	newMotionZ -= subWorldObj.getMotionZ() * 0.1d;
            	
            	if (newMotionX * newMotionX + newMotionZ * newMotionZ < 0.005d * 0.005d)
            	{
            		newMotionX = 0;
            		newMotionZ = 0;
            	}
            }
            
            //Y-direction
            double accelerationY = 0.0d;
        	
        	boolean jumping = ((EntitySuperClass)this.riddenByEntity).getIsJumping();
        	
        	boolean sinking = false;
        	if (this.controlledWorld.isRemote)
        		sinking = SubWorldControllerKeyHandler.ctrl_down;
        	else
        	{
        		ControllerKeyServerStore keyStore = (ControllerKeyServerStore)this.riddenByEntity.getExtendedProperties("LCTRL");
        		if (keyStore != null)
        			sinking = keyStore.ctrlDown;
        	}
            
            if (jumping)
            {
            	accelerationY = 0.01d;
            }
            else if (sinking)
            {
            	accelerationY = -0.01d;
            }
            else
            {
            	accelerationY = -subWorldObj.getMotionY() * 0.1d;
            	if (Math.abs(accelerationY) < 0.005d)
            		accelerationY = -subWorldObj.getMotionY();
            }
            
            //Calculate result
            double newMotionY = subWorldObj.getMotionY() + accelerationY;
            
            double newVel = newMotionX * newMotionX + newMotionY * newMotionY + newMotionZ * newMotionZ;
            if (newVel > 0.6 * 0.6)
            {
            	newVel = Math.sqrt(newVel);
            	newMotionX *= 0.6 / newVel;
            	newMotionY *= 0.6 / newVel;
            	newMotionZ *= 0.6 / newVel;
            }
            
            subWorldObj.setMotion(newMotionX,  newMotionY,  newMotionZ);
        }
        
        if (this.riddenByEntity == null && !this.worldObj.isRemote)
        	this.setDead();
    }
    
    public void updateRiderPosition()
    {
    	/*if (this.riddenByEntity != null && !this.worldObj.isRemote)
    	{
	    	if (!this.riderLocationSaved)
	    	{
	    		EntitySuperClass superRider = (EntitySuperClass)this.riddenByEntity;
	    		
	    		WorldSuperClass worldBelowFeet = this.worldObj;
		    	if (superRider.getWorldBelowFeet() != null)
		    		worldBelowFeet = (WorldSuperClass)superRider.getWorldBelowFeet();
		    	
		    	this.setWorldBelowFeet((World)worldBelowFeet);
	    		
        		this.riderLocalX = worldBelowFeet.transformToLocalX(this.riddenByEntity.posX, this.riddenByEntity.posZ);
        		this.riderLocalY = worldBelowFeet.transformToLocalY(this.riddenByEntity.posY);
        		this.riderLocalZ = worldBelowFeet.transformToLocalZ(this.riddenByEntity.posX, this.riddenByEntity.posZ);
        		
        		this.riderLocationSaved = true;
	    	}
	    	else
	    	{
	    		EntitySuperClass superThis = (EntitySuperClass)this;
	    		
	    		WorldSuperClass worldBelowFeet = this.worldObj;
		    	if (superThis.getWorldBelowFeet() != null)
		    		worldBelowFeet = (WorldSuperClass)superThis.getWorldBelowFeet();
	    		
		    	this.riddenByEntity.setPosition(worldBelowFeet.transformToGlobalX(this.riderLocalX, this.riderLocalZ), worldBelowFeet.transformToGlobalY(this.riderLocalY), worldBelowFeet.transformToGlobalZ(this.riderLocalX, this.riderLocalZ));
	    	}
    	}*/
        /*if (this.riddenByEntity != null)
        {
        	this.riddenByEntity.motionX = 0;
        	this.riddenByEntity.motionY = 0;
        	this.riddenByEntity.motionZ = 0;
        }*/
        //	EntitySuperClass superThis = (EntitySuperClass)this;
        //	this.riddenByEntity.setPosition(superThis.getGlobalPosX(), superThis.getGlobalPosY()/* + this.getMountedYOffset() + this.riddenByEntity.getYOffset()*/, superThis.getGlobalPosZ());
            //double var1 = Math.cos((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
            //double var3 = Math.sin((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
            //this.riddenByEntity.setPosition(this.posX + var1, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + var3);
        //}
    	if (this.riddenByEntity != null)
        {
    		this.riddenByEntity.setPosition(this.posX, this.posY + 1.0d, this.posZ);
        }
    }
    
    public boolean shouldRenderInPass(int pass)
    {
    	//return super.shouldRenderInPass(pass);
    	return false;
    }
    
    /*public void moveEntity(double par1, double par3, double par5) { }
    
    public void setPosition(double par1, double par3, double par5)
    {
    	if (!this.worldObj.isRemote)
    	{
    		int deleteMe = 0;
    	}
    	
    	super.setPosition(par1, par3, par5);
    }
    
    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8)
    {
    	if (!this.worldObj.isRemote)
    	{
    		int deleteMe = 0;
    	}
    	
    	super.setPositionAndRotation(par1, par3, par5, par7, par8);
    }*/
}

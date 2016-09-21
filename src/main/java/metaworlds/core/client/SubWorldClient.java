package metaworlds.core.client;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import org.jblas.DoubleMatrix;
import org.lwjgl.BufferUtils;

import metaworlds.api.EntitySuperClass;
import metaworlds.api.SubWorld;
import metaworlds.core.SubWorldTransformationHandler;
import metaworlds.core.SubWorldUpdatePacket;
import metaworlds.patcher.EntityDraggableBySubWorld;
import metaworlds.patcher.OrientedBB;
import metaworlds.patcher.RenderGlobalSubWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class SubWorldClient extends WorldClient implements SubWorld
{
	private WorldClient m_parentWorld;
	//private IChunkProvider m_chunkProvider = null;
	private int subWorldID;
	
	private ArrayList collidingBBCache = new ArrayList();
	
	private SubWorldTransformationHandler transformationHandler = new SubWorldTransformationHandler(this);
	
	public int localTickCounter;
	public int lastServerTickReceived;
	public float serverTickDiff;

    
    private double lastTickX;
	private double lastTickY;
	private double lastTickZ;
	private double lastTickRotationYaw;
	private double lastTickRotationPitch;
	private double lastTickRotationRoll;
	private double lastTickScaling = 1.0d;
	
	private double nextTickX;
	private double nextTickY;
	private double nextTickZ;
	private double nextTickRotationYaw;
	private double nextTickRotationPitch;
	private double nextTickRotationRoll;
	private double nextTickScaling = 1.0d;
	
	/** Position when the renderers were updated last */
	private double prevRendererUpdateX;
	private double prevRendererUpdateY;
	private double prevRendererUpdateZ;
	private double prevRendererRotation;
	private double prevRendererScaling;
    
    private Map<Entity, Vec3> entitiesToDrag = new TreeMap<Entity, Vec3>();
    private Map<Entity, Vec3> entitiesToNotDrag = new TreeMap<Entity, Vec3>();
    
    /** Information about world size */
    private ChunkCoordinates minCoordinates = new ChunkCoordinates();
    private ChunkCoordinates maxCoordinates = new ChunkCoordinates();
    private double maxRadius = 0.0d;
    
    private int subWorldType;
    
    private RenderGlobalSubWorld renderGlobalSubWorld;
    
    private SubWorldUpdatePacket updatePacketToHandle;//The latest update packet to be handled in the upcoming tick
	
	public SubWorldClient(WorldClient parentWorld, int newSubWorldID, NetHandlerPlayClient par1NetClientHandler, WorldSettings par2WorldSettings, int par3, EnumDifficulty par4, Profiler par5Profiler)
	{
		super(par1NetClientHandler, par2WorldSettings, par3, par4, par5Profiler);
		this.m_parentWorld = parentWorld;
		
		this.isRemote = true;
		
		this.subWorldID = newSubWorldID;
		this.setRotationYaw(0.0d);
		this.setTranslation(0.0d, 0.0d, 0.0d);
		this.lastTickX = 0.0d;
		this.lastTickY = 0.0d;
		this.lastTickZ = 0.0d;
		this.lastTickRotationYaw = 0.0d;
		this.lastTickScaling = 1.0d;
		this.localTickCounter = 0;
		this.lastServerTickReceived = -1;
		this.serverTickDiff = -1.0f;
		
		this.minCoordinates.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		this.maxCoordinates.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}
	
	@Override 
	public World CreateSubWorld()
    {
		return m_parentWorld.CreateSubWorld();
    }
	
	@Override
	public World CreateSubWorld(int newSubWorldID)
	{
		return m_parentWorld.CreateSubWorld(newSubWorldID);
	}
	
	@Override 
	public void removeSubWorld()
	{
		m_parentWorld.getSubWorlds().remove(this);
		Minecraft.getMinecraft().renderGlobal.unloadRenderersForSubWorld(this.getSubWorldID());
		
		if (this.renderGlobalSubWorld != null)
		{
		    this.renderGlobalSubWorld.onWorldRemove();
		}
	}
	
	public SubWorldUpdatePacket getUpdatePacketToHandle()
	{
	    return this.updatePacketToHandle;
	}
	
	public void setUpdatePacketToHandle(SubWorldUpdatePacket newPacket)
	{
	    this.updatePacketToHandle = newPacket;
	}
	
	@Override
	public void addWorldAccess(IWorldAccess par1IWorldAccess)
	{
	    super.addWorldAccess(par1IWorldAccess);
	    
	    if (par1IWorldAccess instanceof RenderGlobalSubWorld)
	        this.renderGlobalSubWorld = (RenderGlobalSubWorld)par1IWorldAccess;
	}
	
	public RenderGlobal getRenderGlobal()
	{
	    return this.renderGlobalSubWorld;
	}
	
	public boolean rendererUpdateRequired()
	{
		double dX = this.getTranslationX() - this.prevRendererUpdateX;
		double dY = this.getTranslationY() - this.prevRendererUpdateY;
		double dZ = this.getTranslationZ() - this.prevRendererUpdateZ;
		double dSsq = dX * dX + dY * dY + dZ * dZ;  
		double dScale = this.maxRadius * Math.abs(this.getScaling() - this.prevRendererScaling);
		
		return (dSsq > 16.0d * 16.0d || (Math.sqrt(dSsq) + dScale + Math.min(Math.abs(this.getRotationYaw() - this.prevRendererRotation), 180.0d) * Math.PI / 180.0d * this.maxRadius) > 16.0d);
	}
	
	public void markRendererUpdateDone()
	{
		this.prevRendererUpdateX = this.getTranslationX();
		this.prevRendererUpdateY = this.getTranslationY();
		this.prevRendererUpdateZ = this.getTranslationZ();
		this.prevRendererRotation = this.getRotationYaw();
		this.prevRendererScaling = this.getScaling();
	}
	
	/*@Override protected IChunkProvider createChunkProvider()
    {
		this.m_chunkProvider = new ChunkProviderClient(this);
		return this.m_chunkProvider;
    }*/
	
	/*@Override public Entity getEntityByID(int par1)
    {
		return m_parentWorld.getEntityByID(par1);
    }*/
	
	@Override
	public World getParentWorld()
	{
		return m_parentWorld;
	}
	
	@Override public int getSubWorldID()
    {
    	return subWorldID;
    }
	
	public int getSubWorldType()
    {
        return this.subWorldType;
    }
    
    public void setSubWorldType(int newType)
    {
        this.subWorldType = newType;
    }
	
	private List entitiesWithinAABBExcludingEntityResult = new ArrayList();
	@Override
	public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
		//ArrayList arraylist = new ArrayList();
		this.entitiesWithinAABBExcludingEntityResult.clear();

		this.entitiesWithinAABBExcludingEntityResult.addAll(this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
    	AxisAlignedBB globalBB = par2AxisAlignedBB.getTransformedToGlobalBoundingBox(this);
    	this.entitiesWithinAABBExcludingEntityResult.addAll(m_parentWorld.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, globalBB, par3IEntitySelector));
    	for (World curSubWorld : m_parentWorld.getSubWorlds())
        {
    		if (curSubWorld == this)
    			continue;
    		
    		this.entitiesWithinAABBExcludingEntityResult.addAll(curSubWorld.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, globalBB.getTransformedToLocalBoundingBox(curSubWorld), par3IEntitySelector));
        }
    	
    	return this.entitiesWithinAABBExcludingEntityResult;
    }
	
	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB par1AxisAlignedBB, Entity par2Entity)
	{
		if(super.checkNoEntityCollision(par1AxisAlignedBB, par2Entity))
			return this.m_parentWorld.checkNoEntityCollision(par1AxisAlignedBB.getTransformedToGlobalBoundingBox(this), par2Entity);
		
		return false;
	}
	
	////////////////////////////////
	// State properties
	////////////////////////////////
	//Getters
	public double getTranslationX() { return this.transformationHandler.getTranslationX(); }
	public double getTranslationY() { return this.transformationHandler.getTranslationY(); }
	public double getTranslationZ() { return this.transformationHandler.getTranslationZ(); }
	
	public double getCenterX() { return this.transformationHandler.getCenterX(); }
	public double getCenterY() { return this.transformationHandler.getCenterY(); }
	public double getCenterZ() { return this.transformationHandler.getCenterZ(); }
	
	public double getRotationYaw() { return this.transformationHandler.getRotationYaw(); }
	public double getRotationPitch() { return this.transformationHandler.getRotationPitch(); }
	public double getRotationRoll() { return this.transformationHandler.getRotationRoll(); }
	
	public double getCosRotationYaw() { return this.transformationHandler.getCosRotationYaw(); }
	public double getSinRotationYaw() { return this.transformationHandler.getSinRotationYaw(); }
	public double getCosRotationPitch() { return this.transformationHandler.getCosRotationPitch(); }
	public double getSinRotationPitch() { return this.transformationHandler.getSinRotationPitch(); }
	public double getCosRotationRoll() { return this.transformationHandler.getCosRotationRoll(); }
	public double getSinRotationRoll() { return this.transformationHandler.getSinRotationRoll(); }
	
	public double getScaling() { return this.transformationHandler.getScaling(); }
	
	//Setters
	public void setTranslation(Vec3 newTranslation) { this.transformationHandler.setTranslation(newTranslation); }
	public void setTranslation(double newX, double newY, double newZ) { this.transformationHandler.setTranslation(newX, newY, newZ); }
	
	public void setCenter(Vec3 newCenter) { this.transformationHandler.setCenter(newCenter); }
	public void setCenter(double newX, double newY, double newZ) { this.transformationHandler.setCenter(newX, newY, newZ); }
	
	public void setRotationYaw(double newYaw) { this.transformationHandler.setRotationYaw(newYaw); }
	public void setRotationPitch(double newPitch) { this.transformationHandler.setRotationPitch(newPitch); }
	public void setRotationRoll(double newRoll) { this.transformationHandler.setRotationRoll(newRoll); }
	
	public void setScaling(double newScaling) { this.transformationHandler.setScaling(newScaling); }
	
	////////////////////////////////
	// Dynamic properties
	////////////////////////////////
	//Getters
	public double getMotionX() { return this.transformationHandler.getMotionX(); }
	public double getMotionY() { return this.transformationHandler.getMotionY(); }
	public double getMotionZ() { return this.transformationHandler.getMotionZ(); }
	
	public double getRotationYawSpeed() { return this.transformationHandler.getRotationYawSpeed(); }
	public double getRotationPitchSpeed() { return this.transformationHandler.getRotationPitchSpeed(); }
	public double getRotationRollSpeed() { return this.transformationHandler.getRotationRollSpeed(); }
	
	public double getScaleChangeRate() { return this.transformationHandler.getScaleChangeRate(); }
	
	//Setters
	public void setMotion(double par1MotionX, double par2MotionY, double par3MotionZ) { this.transformationHandler.setMotion(par1MotionX, par2MotionY, par3MotionZ); }
	
	public void setRotationYawSpeed(double par1Speed) { this.transformationHandler.setRotationYawSpeed(par1Speed); }
	public void setRotationPitchSpeed(double par1Speed) { this.transformationHandler.setRotationPitchSpeed(par1Speed); }
	public void setRotationRollSpeed(double par1Speed) { this.transformationHandler.setRotationRollSpeed(par1Speed); }
	
	public void setScaleChangeRate(double par1Rate) { this.transformationHandler.setScaleChangeRate(par1Rate); }
	
	////////////////////////////////
	// Transformations
	////////////////////////////////
	public DoubleBuffer getTransformToLocalMatrixDirectBuffer() { return this.transformationHandler.getTransformToLocalMatrixDirectBuffer(); }
	public DoubleBuffer getTransformToGlobalMatrixDirectBuffer() { return this.transformationHandler.getTransformToGlobalMatrixDirectBuffer(); }
	
	////////////////////////////////
	// Transformation to local
	////////////////////////////////
	
	public Vec3 transformToLocal(Vec3 globalVec) { return this.transformationHandler.transformToLocal(globalVec); }
	public Vec3 transformToLocal(double globalX, double globalY, double globalZ) { return this.transformationHandler.transformToLocal(globalX, globalY, globalZ); }
	public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) { return this.transformationHandler.transformToLocal(globalVectors); }
	public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result) { return this.transformationHandler.transformToLocal(globalVectors, result); }
	
	////////////////////////////////
	// Transformation to global
	////////////////////////////////
	
	public Vec3 transformToGlobal(Vec3 localVec) { return this.transformationHandler.transformToGlobal(localVec); }
	public Vec3 transformToGlobal(double localX, double localY, double localZ) { return this.transformationHandler.transformToGlobal(localX, localY, localZ); }
	public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) { return this.transformationHandler.transformToGlobal(localVectors); }
	public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) { return this.transformationHandler.transformToGlobal(localVectors, result); }
	
	//Transform from this world's coordinates to another world's coordinates
	public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec) { return this.transformationHandler.transformLocalToOther(targetWorld, localVec); }
	public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ) { return this.transformationHandler.transformLocalToOther(targetWorld, localX, localY, localZ); }
	public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors) { return this.transformationHandler.transformLocalToOther(targetWorld, localVectors); }
	public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result) { return this.transformationHandler.transformLocalToOther(targetWorld, localVectors, result); }
	
	//Transform from another world's coordinates to this world's coordinates
	public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec) { return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVec); }
	public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ) { return this.transformationHandler.transformOtherToLocal(sourceWorld, otherX, otherY, otherZ); }
	public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors) { return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVectors); }
	public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result) { return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVectors, result); }
	
	////////////////////////
	// Rotation
	////////////////////////
	
	public Vec3 rotateToGlobal(Vec3 localVec) { return this.transformationHandler.rotateToGlobal(localVec); }
	public Vec3 rotateToGlobal(double localX, double localY, double localZ) { return this.transformationHandler.rotateToGlobal(localX, localY, localZ); }
	public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) { return this.transformationHandler.rotateToGlobal(localVectors); }
	
	public Vec3 rotateToLocal(Vec3 globalVec) { return this.transformationHandler.rotateToLocal(globalVec); }
	public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) { return this.transformationHandler.rotateToLocal(globalX, globalY, globalZ); }
	public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) { return this.transformationHandler.rotateToLocal(globalVectors); }
	
	public Vec3 rotateYawToGlobal(Vec3 localVec) { return this.transformationHandler.rotateYawToGlobal(localVec); }
    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ) { return this.transformationHandler.rotateYawToGlobal(localX, localY, localZ); }
    public DoubleMatrix rotateYawToGlobal(DoubleMatrix localVectors) { return this.transformationHandler.rotateYawToGlobal(localVectors); }
    
    public Vec3 rotateYawToLocal(Vec3 globalVec) { return this.transformationHandler.rotateYawToLocal(globalVec); }
    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ) { return this.transformationHandler.rotateYawToLocal(globalX, globalY, globalZ); }
    public DoubleMatrix rotateYawToLocal(DoubleMatrix globalVectors) { return this.transformationHandler.rotateYawToLocal(globalVectors); }
	
	////////////////////////////////
	// Boundaries
	////////////////////////////////
	public int getMinX() { return this.minCoordinates.posX; } 
	public int getMinY() { return this.minCoordinates.posY; }
	public int getMinZ() { return this.minCoordinates.posZ; }
	public ChunkCoordinates getMinCoordinates() { return this.minCoordinates; }
	public int getMaxX() { return this.maxCoordinates.posX; }
	public int getMaxY() { return this.maxCoordinates.posY; }
	public int getMaxZ() { return this.maxCoordinates.posZ; }
	public ChunkCoordinates getMaxCoordinates() { return this.maxCoordinates; }
	
	public void setBoundaries(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		if (this.getMinX() == minX && this.getMinY() == minY && this.getMinZ() == minZ && 
				this.getMaxX() == maxX && this.getMaxY() == maxY && this.getMaxZ() == maxZ)
			return;
		
		this.minCoordinates.posX = minX;
		this.minCoordinates.posY = minY;
		this.minCoordinates.posZ = minZ;
		this.maxCoordinates.posX = maxX;
		this.maxCoordinates.posY = maxY;
		this.maxCoordinates.posZ = maxZ;
		
		//Find max radius to calculate tangential velocities
		double minXSq = minX * minX;
		double minYSq = minY * minY;
		double minZSq = minZ * minZ;
		double maxXSq = maxX * maxX;
		double maxYSq = maxY * maxY;
		double maxZSq = maxZ * maxZ;
		
		double l1 = minXSq + minZSq + Math.max(minYSq, maxYSq);
		double l2 = minXSq + maxZSq + Math.max(minYSq, maxYSq);
		double l3 = maxXSq + minZSq + Math.max(minYSq, maxYSq);
		double l4 = maxXSq + maxZSq + Math.max(minYSq, maxYSq);
		double lmaxSq = Math.max(Math.max(l1, l2), Math.max(l3, l4));
		
		if (lmaxSq > this.maxRadius * this.maxRadius)
			this.maxRadius = Math.sqrt(lmaxSq);
		
		EntityLivingBase playerEntity = Minecraft.getMinecraft().renderViewEntity;
		Minecraft.getMinecraft().renderGlobal.markRenderersForNewPositionSingle(playerEntity.posX, playerEntity.posY, playerEntity.posZ, this.getSubWorldID());
	}
	
	////////////////////////////////
	////////////////////////////////
    
	@Override
    public void playSoundEffect(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
    	Vec3 newPos = this.transformToGlobal(par1, par3, par5);
    	super.playSoundEffect(newPos.xCoord, newPos.yCoord, newPos.zCoord, par7Str, par8, par9);
    }
    
	@Override
    public void playAuxSFXAtEntity(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
    	//Vec3 newPos = this.transformToGlobal(par3, par4, par5);
    	//super.playAuxSFXAtEntity(par1EntityPlayer, par2, (int)newPos.xCoord, (int)newPos.yCoord, (int)newPos.zCoord, par6);
	    super.playAuxSFXAtEntity(par1EntityPlayer, par2, par3, par4, par5, par6);
    }
    
    /*public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
    	Vec3 transformedPos = this.transformToGlobal(par2, par4, par6);
    	super.spawnParticle(par1Str, transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord, par8, par10, par12);
    }*/
    
    //Override methods for world-time changing so that the time of day will not pass faster the more subworlds exist
	@Override
    public void func_82738_a(long par1) { }
	@Override
    public void setWorldTime(long par1) { }
    
    @Override
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
    	return super.spawnEntityInWorld(par1Entity);
    	
    	/*par1Entity.setWorld(this.m_parentWorld);
    	par1Entity.setPositionAndRotation(this.transformToGlobalX(par1Entity.posX, par1Entity.posZ), this.transformToGlobalY(par1Entity.posY), this.transformToGlobalZ(par1Entity.posX, par1Entity.posZ), (float)this.rotationYaw, 0.0F);
    	return this.m_parentWorld.spawnEntityInWorld(par1Entity);*/
    }
    
    //Makes sure this entity keeps its local coordinates in this world when this world moves - used when worldBelowFeet for an entity changes
    public void registerEntityToDrag(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity && !(targetEntity instanceof EntityFX) && ((Entity)targetEntity).worldObj != this)
    		this.entitiesToDrag.put((Entity)targetEntity, (Vec3)null);
    }
    
    public void unregisterEntityToDrag(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity && !(targetEntity instanceof EntityFX) && ((Entity)targetEntity).worldObj != this)
    		this.entitiesToDrag.remove((Entity)targetEntity);
    }
    
    //Makes sure this entity is not moved with this world (in case the entity is spawned in this world) - used when worldBelowFeet for an entity changes
    public void registerDetachedEntity(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity && !(targetEntity instanceof EntityFX) && ((Entity)targetEntity).worldObj == this)
    		this.entitiesToNotDrag.put((Entity)targetEntity, (Vec3)null);
    }
    
    public void unregisterDetachedEntity(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity && !(targetEntity instanceof EntityFX) && ((Entity)targetEntity).worldObj == this)
    		this.entitiesToNotDrag.remove((Entity)targetEntity);
    }
    
    @Override
    public void tick()
    {
    	++this.localTickCounter;
    	
    	super.tick();
    	
    	if (this.getUpdatePacketToHandle() != null)
    	{
    	    this.getUpdatePacketToHandle().executeOnTick();
    	    this.setUpdatePacketToHandle(null);
    	}
    	
    	this.tickPosition(1);
    }
    
    public void tickPosition(int count)
    {
    	if (count == 0)
    		return;
    	
    	//this.setTranslation(this.lastTickX, this.lastTickY, this.lastTickZ);
    	//this.setRotationYaw(this.lastTickRotationYaw);
    	//this.setScaling(this.lastTickScaling);
    	
    	if (!this.transformationHandler.getIsInMotion())
    		return;
    	
    	double prevRotationYaw = this.getRotationYaw();
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    		curEntry.setValue(this.transformToLocal(curEntry.getKey()));
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		curEntry.setValue(this.transformToGlobal(curEntry.getKey()));
    	}
    	
    	this.setTranslation(this.getTranslationX() + this.getMotionX() * (double)count, 
				this.getTranslationY() + this.getMotionY() * (double)count, 
				this.getTranslationZ() + this.getMotionZ() * (double)count);
    	
    	this.setRotationYaw(this.getRotationYaw() + this.getRotationYawSpeed() * (double)count);
    	this.setRotationPitch(this.getRotationPitch() + this.getRotationPitchSpeed() * (double)count);
    	this.setRotationRoll(this.getRotationRoll() + this.getRotationRollSpeed() * (double)count);
    	
    	this.setScaling(this.getScaling() + this.getScaleChangeRate() * (double)count);
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    	    Entity curEntity = curEntry.getKey();
            double subWorldWeight = curEntity.getTractionFactor();
            double globalWeight = 1.0d - subWorldWeight;
    	    
    		Vec3 newPosition = this.transformToGlobal(curEntry.getValue());
    		curEntry.getKey().setPosition(curEntity.posX * globalWeight + newPosition.xCoord * subWorldWeight, curEntity.posY * globalWeight + newPosition.yCoord * subWorldWeight, curEntity.posZ * globalWeight + newPosition.zCoord * subWorldWeight);
    		
    		float newEntityPrevRotationYawDiff = curEntry.getKey().prevRotationYaw - (curEntry.getKey().rotationYaw - (float)(this.getRotationYaw() - prevRotationYaw));
    		
    		curEntry.getKey().setRotation(curEntry.getKey().rotationYaw - (float)(this.getRotationYaw() - prevRotationYaw), curEntry.getKey().rotationPitch);
    		
    		curEntry.getKey().prevRotationYaw = newEntityPrevRotationYawDiff + curEntry.getKey().rotationYaw;
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		Vec3 newPosition = this.transformToLocal(curEntry.getValue());
    		curEntry.getKey().setPosition(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord);
    		
    		float newEntityPrevRotationYawDiff = curEntry.getKey().prevRotationYaw - (curEntry.getKey().rotationYaw + (float)(this.getRotationYaw() - prevRotationYaw));
    		
    		curEntry.getKey().setRotation(curEntry.getKey().rotationYaw + (float)(this.getRotationYaw() - prevRotationYaw), curEntry.getKey().rotationPitch);
    		
    		curEntry.getKey().prevRotationYaw = newEntityPrevRotationYawDiff + curEntry.getKey().rotationYaw;
    	}
    	
    	this.nextTickX = this.getTranslationX();
    	this.nextTickY = this.getTranslationY();
    	this.nextTickZ = this.getTranslationZ();
    	this.nextTickRotationYaw = this.getRotationYaw();
    	this.nextTickRotationPitch = this.getRotationPitch();
    	this.nextTickRotationRoll = this.getRotationRoll();
    	this.nextTickScaling = this.getScaling();
    	
    	//this.lastTickX = this.getTranslationX() - this.getMotionX();
    	//this.lastTickY = this.getTranslationY() - this.getMotionY();
    	//this.lastTickZ = this.getTranslationZ() - this.getMotionZ();
    	//this.lastTickRotationYaw = this.getRotationYaw() - this.getRotationYawSpeed();
    	//this.lastTickScaling = this.getScaling() - this.getScaleChangeRate();
    }
    
    public void doTickPartial(double interpolationFactor)
    {
    	if (interpolationFactor == 1.0d)
    		return;
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    		Entity curEntity = curEntry.getKey();
    		double curEntityX = curEntity.prevPosX + (curEntity.posX - curEntity.prevPosX) * interpolationFactor;
    		double curEntityY = curEntity.prevPosY + (curEntity.posY - curEntity.prevPosY) * interpolationFactor;
    		double curEntityZ = curEntity.prevPosZ + (curEntity.posZ - curEntity.prevPosZ) * interpolationFactor;
    		//double curEntityX = curEntity.lastTickPosX + (curEntity.posX - curEntity.lastTickPosX) * interpolationFactor;
    		//double curEntityY = curEntity.lastTickPosY + (curEntity.posY - curEntity.lastTickPosY) * interpolationFactor;
    		//double curEntityZ = curEntity.lastTickPosZ + (curEntity.posZ - curEntity.lastTickPosZ) * interpolationFactor;
    		Vec3 curEntityPos = this.transformToLocal(curEntityX, curEntityY, curEntityZ);
    		
    		curEntry.setValue(curEntityPos);
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		Entity curEntity = curEntry.getKey();
    		double curEntityX = curEntity.prevPosX + (curEntity.posX - curEntity.prevPosX) * interpolationFactor;
    		double curEntityY = curEntity.prevPosY + (curEntity.posY - curEntity.prevPosY) * interpolationFactor;
    		double curEntityZ = curEntity.prevPosZ + (curEntity.posZ - curEntity.prevPosZ) * interpolationFactor;
    		Vec3 curEntityPos = this.transformToGlobal(curEntityX, curEntityY, curEntityZ);
    		
    		curEntry.setValue(curEntityPos);
    	}
    	
    	this.setTranslation(this.lastTickX + (this.nextTickX - this.lastTickX) * interpolationFactor, 
    						this.lastTickY + (this.nextTickY - this.lastTickY) * interpolationFactor, 
    						this.lastTickZ + (this.nextTickZ - this.lastTickZ) * interpolationFactor);
    	this.setRotationYaw(this.lastTickRotationYaw + (this.nextTickRotationYaw - this.lastTickRotationYaw) * interpolationFactor);
    	this.setRotationPitch(this.lastTickRotationPitch + (this.nextTickRotationPitch - this.lastTickRotationPitch) * interpolationFactor);
    	this.setRotationRoll(this.lastTickRotationRoll + (this.nextTickRotationRoll - this.lastTickRotationRoll) * interpolationFactor);
    	this.setScaling(this.lastTickScaling + (this.nextTickScaling - this.lastTickScaling) * interpolationFactor);
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    	    Entity curEntity = curEntry.getKey();
            double subWorldWeight = curEntity.getTractionFactor();
            double globalWeight = 1.0d - subWorldWeight;
    	    
    		Vec3 newPosition = this.transformToGlobal(curEntry.getValue());
    		double curEntityX = curEntity.prevPosX + (curEntity.posX - curEntity.prevPosX) * interpolationFactor;
            double curEntityY = curEntity.prevPosY + (curEntity.posY - curEntity.prevPosY) * interpolationFactor;
            double curEntityZ = curEntity.prevPosZ + (curEntity.posZ - curEntity.prevPosZ) * interpolationFactor;
    		newPosition.xCoord = curEntityX * globalWeight + newPosition.xCoord * subWorldWeight;
    		newPosition.yCoord = curEntityY * globalWeight + newPosition.yCoord * subWorldWeight;
    		newPosition.zCoord = curEntityZ * globalWeight + newPosition.zCoord * subWorldWeight;
    		//Entity curEntity = curEntry.getKey();
    		curEntity.prevPosX = newPosition.xCoord + (newPosition.xCoord - curEntity.posX) * interpolationFactor / (1 - interpolationFactor);
    		curEntity.prevPosY = newPosition.yCoord + (newPosition.yCoord - curEntity.posY) * interpolationFactor / (1 - interpolationFactor);
    		curEntity.prevPosZ = newPosition.zCoord + (newPosition.zCoord - curEntity.posZ) * interpolationFactor / (1 - interpolationFactor);
    		//curEntity.lastTickPosX = newPosition.xCoord + (newPosition.xCoord - curEntity.posX) * interpolationFactor / (1 - interpolationFactor);
    		//curEntity.lastTickPosY = newPosition.yCoord + (newPosition.yCoord - curEntity.posY) * interpolationFactor / (1 - interpolationFactor);
    		//curEntity.lastTickPosZ = newPosition.zCoord + (newPosition.zCoord - curEntity.posZ) * interpolationFactor / (1 - interpolationFactor);
    		
    		//curEntity.prevRotationYaw
    		//curEntry.getKey().setRotation(curEntry.getKey().rotationYaw - (float)(this.getRotationYaw() - prevRotationYaw), curEntry.getKey().rotationPitch);
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		Vec3 newPosition = this.transformToLocal(curEntry.getValue());
    		Entity curEntity = curEntry.getKey();
    		curEntity.prevPosX = newPosition.xCoord + (newPosition.xCoord - curEntity.posX) * interpolationFactor / (1 - interpolationFactor);
    		curEntity.prevPosY = newPosition.yCoord + (newPosition.yCoord - curEntity.posY) * interpolationFactor / (1 - interpolationFactor);
    		curEntity.prevPosZ = newPosition.zCoord + (newPosition.zCoord - curEntity.posZ) * interpolationFactor / (1 - interpolationFactor);
    		
    		//curEntity.prevRotationYaw
    		//curEntry.getKey().setRotation(curEntry.getKey().rotationYaw - (float)(this.getRotationYaw() - prevRotationYaw), curEntry.getKey().rotationPitch);
    	}
    	
    	//this.setTranslation(curX, curY, curZ);
    	//this.setRotationYaw(curYaw);
    	//this.setScaling(curScaling);
    }
    
    public void UpdatePositionAndRotation(double newX, double newY, double newZ, double newRotationYaw, double newRotationPitch, double newRotationRoll, double newScaling)
    {
    	//this.setTranslation(this.lastTickX, this.lastTickY, this.lastTickZ);
    	//this.setRotationYaw(this.lastTickRotationYaw);
    	//this.setScaling(this.lastTickScaling);
    	
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    		curEntry.setValue(this.transformToLocal(curEntry.getKey()));
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		curEntry.setValue(this.transformToGlobal(curEntry.getKey()));
    	}
    	
    	this.setTranslation(newX, newY, newZ);
    	double rotationDiff = newRotationYaw - this.getRotationYaw();
    	this.setRotationYaw(newRotationYaw);
    	this.setRotationPitch(newRotationPitch);
    	this.setRotationRoll(newRotationRoll);
    	this.setScaling(newScaling);
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
    	{
    	    Entity curEntity = curEntry.getKey();
            double subWorldWeight = curEntity.getTractionFactor();
            double globalWeight = 1.0d - subWorldWeight;
    	    
    		Vec3 newPosition = this.transformToGlobal(curEntry.getValue());
    		curEntry.getKey().setPosition(curEntity.posX * globalWeight + newPosition.xCoord * subWorldWeight, curEntity.posY * globalWeight + newPosition.yCoord * subWorldWeight, curEntity.posZ * globalWeight + newPosition.zCoord * subWorldWeight);
    		float newEntityPrevRotationYawDiff = curEntry.getKey().prevRotationYaw - (curEntry.getKey().rotationYaw - (float)rotationDiff);
    		curEntry.getKey().setRotation(curEntry.getKey().rotationYaw - (float)rotationDiff, curEntry.getKey().rotationPitch);
    		curEntry.getKey().prevRotationYaw = newEntityPrevRotationYawDiff + curEntry.getKey().rotationYaw;
    		//curEntry.getKey().setPositionAndRotation(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord, curEntry.getKey().rotationYaw - (float)rotationDiff, curEntry.getKey().rotationPitch);
    	}
    	
    	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
    	{
    		Vec3 newPosition = this.transformToLocal(curEntry.getValue());
    		curEntry.getKey().setPosition(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord);
    		float newEntityPrevRotationYawDiff = curEntry.getKey().prevRotationYaw - (curEntry.getKey().rotationYaw + (float)rotationDiff);
    		curEntry.getKey().setRotation(curEntry.getKey().rotationYaw + (float)rotationDiff, curEntry.getKey().rotationPitch);
    		curEntry.getKey().prevRotationYaw = newEntityPrevRotationYawDiff + curEntry.getKey().rotationYaw;
    		//curEntry.getKey().setPositionAndRotation(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord, curEntry.getKey().rotationYaw - (float)rotationDiff, curEntry.getKey().rotationPitch);
    	}
    	
    	this.nextTickX = this.getTranslationX();
    	this.nextTickY = this.getTranslationY();
    	this.nextTickZ = this.getTranslationZ();
    	this.nextTickRotationYaw = this.getRotationYaw();
    	this.nextTickRotationPitch = this.getRotationPitch();
    	this.nextTickRotationRoll = this.getRotationRoll();
    	this.nextTickScaling = this.getScaling();
    	
    	/*this.lastTickX = this.getTranslationX() - this.getMotionX();
    	this.lastTickY = this.getTranslationY() - this.getMotionY();
    	this.lastTickZ = this.getTranslationZ() - this.getMotionZ();
    	this.lastTickRotationYaw = this.getRotationYaw() - this.getRotationYawSpeed();
    	this.lastTickScaling = this.getScaling() - this.getScaleChangeRate();*/
    }
    
    public void onPreTick()
    {
    	this.setTranslation(this.nextTickX, 
				this.nextTickY, 
				this.nextTickZ);
		this.setRotationYaw(this.nextTickRotationYaw);
		this.setRotationPitch(this.nextTickRotationPitch);
		this.setRotationRoll(this.nextTickRotationRoll);
		this.setScaling(this.nextTickScaling);
    	
    	this.lastTickX = this.getTranslationX();
    	this.lastTickY = this.getTranslationY();
    	this.lastTickZ = this.getTranslationZ();
    	this.lastTickRotationYaw = this.getRotationYaw();
    	this.lastTickRotationPitch = this.getRotationPitch();
    	this.lastTickRotationRoll = this.getRotationRoll();
    	this.lastTickScaling = this.getScaling();
    }
    
    @Override
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	List result = (ArrayList)this.getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB);
    	
    	AxisAlignedBB globalAABBPar = par2AxisAlignedBB.getTransformedToGlobalBoundingBox(this);
    	
    	for (World curSubWorld : this.getParentWorld().getWorlds())
    	//for (int curSubWorldID = 0; curSubWorldID < this.getParentWorld().getWorldsCount(); ++curSubWorldID)
    	{
    		if (curSubWorld.getSubWorldID() == this.getSubWorldID())
    			continue;
    		
    		//World curSubWorld = this.getParentWorld().getSubWorld(curSubWorldID);
    		List curResult = curSubWorld.getCollidingBoundingBoxesGlobal(par1Entity, globalAABBPar);
    		ListIterator iter = curResult.listIterator();
        	while(iter.hasNext())
        	{
        		AxisAlignedBB replacementBB = ((AxisAlignedBB)iter.next()).getTransformedToLocalBoundingBox(this);
        		((OrientedBB)replacementBB).lastTransformedBy = curSubWorld;
        		iter.set(replacementBB);
        	}
        	result.addAll(curResult);
    	}

    	return result;
    }
    
    @Override
    public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
        this.collidingBBCache.clear();
        int i = MathHelper.floor_double(Math.max(par2AxisAlignedBB.minX, this.getMinX()));
	    int j = MathHelper.floor_double(Math.min(par2AxisAlignedBB.maxX + 1.0D, this.getMaxX()));
	    int k = MathHelper.floor_double(Math.max(par2AxisAlignedBB.minY, this.getMinY()));
	    int l = MathHelper.floor_double(Math.min(par2AxisAlignedBB.maxY + 1.0D, this.getMaxY()));
	    int i1 = MathHelper.floor_double(Math.max(par2AxisAlignedBB.minZ, this.getMinZ()));
	    int j1 = MathHelper.floor_double(Math.min(par2AxisAlignedBB.maxZ + 1.0D, this.getMaxZ()));

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = i1; l1 < j1; ++l1)
            {
                if (this.blockExists(k1, 64, l1))
                {
                    for (int i2 = k - 1; i2 < l; ++i2)
                    {
                        Block block = this.getBlock(k1, i2, l1);

                        if (block != Blocks.air)
                        {
                            block.addCollisionBoxesToList(this, k1, i2, l1, par2AxisAlignedBB, this.collidingBBCache, par1Entity);
                        }
                    }
                }
            }
        }

        double d0 = 0.25D;
        List list = this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB.expand(d0, d0, d0));

        for (int j2 = 0; j2 < list.size(); ++j2)
        {
            AxisAlignedBB axisalignedbb1 = ((Entity)list.get(j2)).getBoundingBox();

            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(par2AxisAlignedBB))
            {
                this.collidingBBCache.add(axisalignedbb1);
            }

            axisalignedbb1 = par1Entity.getCollisionBox((Entity)list.get(j2));

            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(par2AxisAlignedBB))
            {
                this.collidingBBCache.add(axisalignedbb1);
            }
        }

        return this.collidingBBCache;
    }
    
    /*public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	List result = this.getParentWorld().getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB.getTransformedToGlobalBoundingBox(this));
    	ListIterator iter = result.listIterator();
    	while(iter.hasNext())
    	{
    		AxisAlignedBB replacementBB = ((AxisAlignedBB)iter.next()).getTransformedToLocalBoundingBox(this);
    		iter.set(replacementBB);
    	}
    	
    	return result;
    }*/
    
    //public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    //{
    //	return super.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    //}
    
    @Override
    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	List result = this.getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB.getTransformedToLocalBoundingBox(this));
    	ListIterator iter = result.listIterator();
    	while(iter.hasNext())
    	{
    		AxisAlignedBB replacementBB = ((AxisAlignedBB)iter.next()).getTransformedToGlobalBoundingBox(this);
    		iter.set(replacementBB);
    	}
    	
    	return result;
    }
    
    @Override
    public boolean isAnyLiquid(AxisAlignedBB par1AxisAlignedBB)
    {	
    	return super.isAnyLiquid(par1AxisAlignedBB.getTransformedToLocalBoundingBox(this));
    }
    
    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB par1AxisAlignedBB, Material par2Material, Entity par3Entity)
    {
    	return this.handleMaterialAccelerationLocal(par1AxisAlignedBB.getTransformedToLocalBoundingBox(this), par2Material, par3Entity);
    }
    
    public boolean handleMaterialAccelerationLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material, Entity par3Entity)
    {
    	int i = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minX, this.getMinX()));
	    int j = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxX + 1.0D, this.getMaxX()));
	    int k = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minY, this.getMinY()));
	    int l = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxY + 1.0D, this.getMaxY()));
	    int i1 = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minZ, this.getMinZ()));
	    int j1 = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxZ + 1.0D, this.getMaxZ()));

        if (!this.checkChunksExist(i, k, i1, j, l, j1))
        {
            return false;
        }
        else
        {
            boolean flag = false;
            Vec3 vec3 = this.getWorldVec3Pool().getVecFromPool(0.0D, 0.0D, 0.0D);

            for (int k1 = i; k1 < j; ++k1)
            {
                for (int l1 = k; l1 < l; ++l1)
                {
                    for (int i2 = i1; i2 < j1; ++i2)
                    {
                        Block block = this.getBlock(k1, l1, i2);

                        if (block != Blocks.air && block.getMaterial() == par2Material)
                        {
                            double d0 = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(this.getBlockMetadata(k1, l1, i2)));

                            if ((double)l >= d0)
                            {
                                flag = true;
                                block.velocityToAddToEntity(this, k1, l1, i2, par3Entity, vec3);
                            }
                        }
                    }
                }
            }

            if (vec3.lengthVector() > 0.0D && par3Entity.isPushedByWater())
            {
                vec3 = vec3.normalize();
                double d1 = 0.014D;
                vec3 = this.rotateToGlobal(vec3.xCoord * d1, vec3.yCoord * d1, vec3.zCoord * d1);
                par3Entity.motionX += vec3.xCoord;
                par3Entity.motionY += vec3.yCoord;
                par3Entity.motionZ += vec3.zCoord;
            }

            return flag;
        }
    }
    
    @Override
    public boolean isAABBInMaterialGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
    	return super.isAABBInMaterial(par1AxisAlignedBB.getTransformedToLocalBoundingBox(this), par2Material);
    }
    
    @Override
    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
	    int i = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minX, this.getMinX()));
	    int j = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxX + 1.0D, this.getMaxX()));
	    int k = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minY, this.getMinY()));
	    int l = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxY + 1.0D, this.getMaxY()));
	    int i1 = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minZ, this.getMinZ()));
	    int j1 = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxZ + 1.0D, this.getMaxZ()));
	
	    for (int k1 = i; k1 < j; ++k1)
	    {
	        for (int l1 = k; l1 < l; ++l1)
	        {
	            for (int i2 = i1; i2 < j1; ++i2)
	            {
	                if (this.getBlock(k1, l1, i2).getMaterial() == par2Material)
	                {
	                    return true;
	                }
	            }
	        }
	    }
	
	    return false;
    }
    
    @Override
    public boolean isMaterialInBBGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
    	return super.isMaterialInBB(par1AxisAlignedBB.getTransformedToLocalBoundingBox(this), par2Material);
    }
    
    @Override public void updateWeatherBody() { }
    
    @Override public long getTotalWorldTime()
    {
    	if (m_parentWorld == null)
    		return Minecraft.getMinecraft().theWorld.getTotalWorldTime();//necessary for compatibility with codechicken multipart
    	
        return m_parentWorld.getTotalWorldTime();
    }
    
    @Override public EntityPlayer getClosestPlayer(double par1, double par3, double par5, double par7)
    {
        EntityPlayer closestPlayer = super.getClosestPlayer(par1, par3, par5, par7);
        
        EntityPlayer localProxyPlayer = Minecraft.getMinecraft().thePlayer.getProxyPlayer(this);
        
        if (closestPlayer == null)
        {
            if (par7 < 0.0d || localProxyPlayer.getDistanceSq(par1, par3, par5) < par7 * par7)
                closestPlayer = localProxyPlayer;
        }
        else
        {
            if (localProxyPlayer.getDistanceSq(par1, par3, par5) < closestPlayer.getDistanceSq(par1, par3, par5))
                closestPlayer = localProxyPlayer;
        }
        
        return closestPlayer;
        
    	//Vec3 transformedPos = this.transformToGlobal(par1, par3, par5);
        //return m_parentWorld.getClosestPlayer(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord, par7);
    }
}

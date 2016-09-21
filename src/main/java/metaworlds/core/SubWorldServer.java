package metaworlds.core;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.DoubleMatrix;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import metaworlds.api.EntitySuperClass;
import metaworlds.api.SubWorld;
import metaworlds.patcher.ChunkSubWorld;
import metaworlds.patcher.EntityDraggableBySubWorld;
import metaworlds.patcher.EntityPlayerMPSubWorldProxy;
import metaworlds.patcher.MinecraftServerSubWorldProxy;
import metaworlds.patcher.OrientedBB;
import metaworlds.patcher.WorldIntermediateClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

public class SubWorldServer extends WorldServer implements SubWorld
{
    private static final Logger logger = LogManager.getLogger();
    
	//GLOBAL HELPER
	public static int global_newSubWorldID = 1;//ID of the subworld currently being created
	//
	
	private WorldServer m_parentWorld;
	private int subWorldID;
	
	private ArrayList collidingBBCache = new ArrayList();
	
	private SubWorldTransformationHandler transformationHandler = new SubWorldTransformationHandler(this);
	
	/** World origin translation relative to parent world's origin */
	//private double translationX;
	//private double translationY;
	//private double translationZ;
	
	//private double motionX;
	//private double motionY;
	//private double motionZ;
	
	/** World rotation Yaw */
	//private double rotationYaw;
	//private double cosRotationYaw;
	//private double sinRotationYaw;
	//private double rotationYawFrequency; //Degree per tick
    
    /** World scaling */
	//private double scaling = 1.0d;
	//private double scaleChangeRate;
	
	//DoubleMatrix matrixTranslation = DoubleMatrix.eye(4);
	//DoubleMatrix matrixTranslationInverse = DoubleMatrix.eye(4);
	//DoubleMatrix matrixRotation = DoubleMatrix.eye(4);
	//DoubleMatrix matrixRotationInverse = DoubleMatrix.eye(4);
	//DoubleMatrix matrixScaling = DoubleMatrix.eye(4);
	//DoubleMatrix matrixScalingInverse = DoubleMatrix.eye(4);
	
	//DoubleMatrix matrixTransformToLocal = DoubleMatrix.eye(4);
	//DoubleMatrix matrixTransformToGlobal = DoubleMatrix.eye(4);
    
    private Map<Entity, Vec3> entitiesToDrag = new TreeMap<Entity, Vec3>();
    private Map<Entity, Vec3> entitiesToNotDrag = new TreeMap<Entity, Vec3>();
    
    /** Information about world size */
    private ChunkCoordinates minCoordinates = new ChunkCoordinates();
    private ChunkCoordinates maxCoordinates = new ChunkCoordinates();
    private boolean boundariesChanged = true;
    private boolean centerChanged = true;
    private boolean isEmpty = true;
    
    private int subWorldType;
	
	public SubWorldServer(WorldServer parentWorld, int newSubWorldID, MinecraftServer par1MinecraftServer, ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings, Profiler par6Profiler)
	{
		super(new MinecraftServerSubWorldProxy(par1MinecraftServer), par2ISaveHandler, par3Str, par4, par5WorldSettings, par6Profiler);
		m_parentWorld = parentWorld;
		
		this.subWorldID = newSubWorldID;
		this.setRotationYaw(45.0d);
		this.setTranslation(0.0d, 0.0d, 0.0d);
		
		this.setBoundaries(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		((MinecraftServerSubWorldProxy)this.func_73046_m()).setWorld(this);
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
		//remove player proxies:
		for (Object curPlayer : this.playerEntities)
		{
			EntityPlayerMPSubWorldProxy curPlayerEntity = (EntityPlayerMPSubWorldProxy)curPlayer;
			
			curPlayerEntity.getRealPlayer().playerProxyMap.remove(this.getSubWorldID());
		}
		
		this.m_parentWorld.getSubWorlds().remove(this);
		
		try
    	{
    		Class[] cArg = new Class[1];
        	cArg[0] = World.class;
        	Method unloadWorldMethod = ForgeChunkManager.class.getDeclaredMethod("unloadWorld", cArg);
        	unloadWorldMethod.setAccessible(true);
        	try
        	{
        		unloadWorldMethod.invoke(null, this);
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
    	}
    	catch(NoSuchMethodException e)
    	{
    		System.out.println(e.toString());
    	}
		
		//Store the data about this subWorld's position etc. immediately for reuse in CreateSubWorld
		DimensionManager.getWorld(0).getWorldInfo().updateSubWorldInfo(this);
		try
        {
            this.saveAllChunks(true, (IProgressUpdate)null);
        }
        catch (MinecraftException minecraftexception)
        {
        	logger.warn(minecraftexception.getMessage());
        }
    	
    	//ForgeChunkManager.unloadWorld(this);
		
		//Tell clients to destroy the subWorld
		MetaworldsMod.instance.networkHandler.sendToDimension(new SubWorldDestroyPacket(1, new Integer[]{this.getSubWorldID()}), this.getDimension());
		//PacketDispatcher.sendPacketToAllInDimension(new SubWorldDestroyPacket(1, new Integer[]{this.getSubWorldID()}).makePacket(), this.getDimension());
	}
	
	@Override
	protected IChunkProvider createChunkProvider()
    {
		IChunkLoader var1;
		if (this.saveHandler instanceof SaveHandler)
		{
			File file1 = ((SaveHandler)this.saveHandler).getWorldDirectory();

        	if (DimensionManager.getWorld(this.provider.dimensionId) != null)
    		{
        		file1 = new File(file1, "SUBWORLD" + SubWorldServer.global_newSubWorldID);
        		file1.mkdirs();
    		}
        	
            var1 = new AnvilChunkLoaderSubWorld(file1);
		}
		else
			var1 = this.saveHandler.getChunkLoader(this.provider);
		
        this.theChunkProviderServer = new ChunkProviderServerSubWorld(this, var1, new ChunkProviderServerSubWorldBlank(this, var1, null));
        return this.theChunkProviderServer;
    }
	
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
	public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
	{
		ArrayList arraylist = new ArrayList();

    	arraylist.addAll(this.selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
    	AxisAlignedBB globalBB = par2AxisAlignedBB.getTransformedToGlobalBoundingBox(this);
    	arraylist.addAll(m_parentWorld.selectEntitiesWithinAABBLocal(par1Class, globalBB, par3IEntitySelector));
    	for (World curSubWorld : m_parentWorld.getSubWorlds())
        {
    		if (curSubWorld == this)
    			continue;
    		
        	arraylist.addAll(((WorldIntermediateClass)curSubWorld).selectEntitiesWithinAABBLocal(par1Class, globalBB.getTransformedToLocalBoundingBox(curSubWorld), par3IEntitySelector));
        }
    	
    	return arraylist;
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
	
	public void setCenter(Vec3 newCenter) { this.setCenter(newCenter.xCoord, newCenter.yCoord, newCenter.zCoord); }
	public void setCenter(double newX, double newY, double newZ) 
	{
		if (!this.centerChanged)
			this.centerChanged = this.getCenterX() != newX || this.getCenterY() != newY || this.getCenterZ() != newZ;
			
		this.transformationHandler.setCenter(newX, newY, newZ);
	}
	
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
		if (!this.boundariesChanged)
			this.boundariesChanged = 
				(this.minCoordinates.posX != minX || 
				this.minCoordinates.posY != minY || 
				this.minCoordinates.posZ != minZ || 
				this.maxCoordinates.posX != maxX || 
				this.maxCoordinates.posY != maxY || 
				this.maxCoordinates.posZ != maxZ);
		
		boolean willBeEmpty = !(maxX > minX && maxY > minY && maxZ > minZ);
		
		if (this.boundariesChanged)
		{
			if (willBeEmpty)
			{
				minX = 0;
				maxX = 0;
				minY = 0;
				maxY = 0;
				minZ = 0;
				maxZ = 0;
			}
			
			//Active chunks are all chunks inside the boundaries + 1 chunk margin around
			if (!willBeEmpty)
			{
			    List<ChunkCoordIntPair> chunksToAddToWatch;
			    
			    if (this.isEmpty)
			    {
			        chunksToAddToWatch = makeChunkList(minX, minZ, maxX, maxZ);
			    }
			    else
			    {
			        //Calculate which chunks are active now which were not active before
	                chunksToAddToWatch = makeChunkListAreaAWithoutB(minX, minZ, maxX, maxZ, this.getMinX(), this.getMinZ(), this.getMaxX(), this.getMaxZ());
			    }
				
				this.getPlayerManager().addWatchableChunks(chunksToAddToWatch);
			}
			
			if (!this.isEmpty)
			{
			    List<ChunkCoordIntPair> chunksToRemoveFromWatch;
			    
			    if (willBeEmpty)
			    {
			        chunksToRemoveFromWatch = makeChunkList(this.getMinX(), this.getMinZ(), this.getMaxX(), this.getMaxZ());
			    }
			    else
			    {
			        //Calculate which chunks are not active anymore which were active before
			        chunksToRemoveFromWatch = makeChunkListAreaAWithoutB(this.getMinX(), this.getMinZ(), this.getMaxX(), this.getMaxZ(), minX, minZ, maxX, maxZ);
			    }
			    
				this.getPlayerManager().removeWatchableChunks(chunksToRemoveFromWatch);
			}
			
			this.minCoordinates.posX = minX;
			this.minCoordinates.posY = minY;
			this.minCoordinates.posZ = minZ;
			this.maxCoordinates.posX = maxX;
			this.maxCoordinates.posY = maxY;
			this.maxCoordinates.posZ = maxZ;
		}
		
		this.isEmpty = willBeEmpty;
	}
	
	private List<ChunkCoordIntPair> makeChunkList(int minX, int minZ, int maxX, int maxZ)
	{
	    List<ChunkCoordIntPair> chunksList = new ArrayList<ChunkCoordIntPair>();
	    
	    for (int curChunkX = (minX >> 4) - 1; curChunkX <= ((maxX - 1) >> 4) + 1; ++curChunkX)
	    {
	        for (int curChunkZ = (minZ >> 4) - 1; curChunkZ <= ((maxZ - 1) >> 4) + 1; ++curChunkZ)
	        {
	            chunksList.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
	        }
	    }
	    
	    return chunksList;
	}
	
	private List<ChunkCoordIntPair> makeChunkListAreaAWithoutB(int minXA, int minZA, int maxXA, int maxZA, 
			int minXB, int minZB, int maxXB, int maxZB)
	{
		List<ChunkCoordIntPair> chunksAWithoutB = new ArrayList<ChunkCoordIntPair>();
		
		//MinX column
		for (int curChunkX = (minXA >> 4) - 1; curChunkX < (minXB >> 4) - 1; ++curChunkX)
		{
			for (int curChunkZ = (minZA >> 4) - 1; curChunkZ <= ((maxZA - 1) >> 4) + 1; ++curChunkZ)
			{
				chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
			}
		}
		
		//MaxX column
		for (int curChunkX = ((maxXB - 1) >> 4) + 2; curChunkX <= ((maxXA - 1) >> 4) + 1; ++curChunkX)
		{
			for (int curChunkZ = (minZA >> 4) - 1; curChunkZ <= ((maxZA - 1) >> 4) + 1; ++curChunkZ)
			{
				chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
			}
		}
		
		//MinZ row between the X-columns
		int startX = (Math.max(minXA, minXB) >> 4) - 1;
		int endX = (Math.min(maxXA - 1, maxXB - 1) >> 4) + 1;
		for (int curChunkX = startX; curChunkX <= endX; ++curChunkX)
		{
			for (int curChunkZ = (minZA >> 4) - 1; curChunkZ < (minZB >> 4) - 1; ++ curChunkZ)
			{
				chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
			}
		}
		
		//MaxZ row between the X-columns
		for (int curChunkX = startX; curChunkX <= endX; ++curChunkX)
		{
			for (int curChunkZ = ((maxZB - 1) >> 4) + 2; curChunkZ <= ((maxZA - 1) >> 4) + 1; ++ curChunkZ)
			{
				chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
			}
		}
		
		return chunksAWithoutB;
	}
	
	public boolean isEmpty()
	{
		return this.isEmpty;
	}
	
	@Override
	public boolean isChunkWatchable(int chunkX, int chunkZ)
	{
		return !this.isEmpty() && chunkX >= (this.getMinX() >> 4) - 1 && chunkX <= ((this.getMaxX() - 1) >> 4) + 1 && chunkZ >= (this.getMinZ() >> 4) - 1 && chunkZ <= ((this.getMaxZ() - 1) >> 4) + 1; 
	}
	
	@Override
	public Block getBlock(int par1, int par2, int par3)
    {
		if (this.minCoordinates != null && this.maxCoordinates != null)
		{
			int chunkX = (par1 >> 4);
			int chunkZ = (par3 >> 4);
			if (chunkX < (this.getMinX() >> 4) || chunkX > ((this.getMaxX() - 1) >> 4) || chunkZ < (this.getMinZ() >> 4) || chunkZ > ((this.getMaxZ() - 1) >> 4))
				return Blocks.air;
		}
		
		return super.getBlock(par1, par2, par3);
    }
	
	/*@Override
    public void playSoundEffect(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
    	Vec3 transformedPos = this.transformToGlobal(par1, par3, par5);
    	super.playSoundEffect(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord, par7Str, par8, par9);
    }*/
    
	@Override
    public void playAuxSFXAtEntity(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
    	Vec3 transformedPos = this.transformToGlobal(par3, par4, par5);
    	super.playAuxSFXAtEntity(par1EntityPlayer, par2, (int)transformedPos.xCoord, (int)transformedPos.yCoord, (int)transformedPos.zCoord, par6);
    }
    
    public void tick()
    {
    	super.tick();
    	
    	//this.setScaleChangeRate(Math.sin((double)MinecraftServer.getServer().getTickCounter() * Math.PI / 100.0d) * 0.1d);
    	//this.setScaleChangeRate(0);
    	//this.setScaleChangeRate(0.004d);
    	//this.setScaling(2.0d);
    	
    	//if (MinecraftServer.getServer().getTickCounter() % 10 == 0)
    		//MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).sendPacketToAllPlayersInDimension(new SubWorldUpdatePacket(this), this.provider.dimensionId);
    	
    	int updateFlags = 0;
    	if (MinecraftServer.getServer().getTickCounter() % 20 == 0)
    	{
    		updateFlags |= 0x01;//include position
    		
    		if (this.transformationHandler.getIsInMotion())
    			updateFlags |= 0x02;//include velocities
    		
    		updateFlags |= 0x04;//include center coordinates
    		
    		updateFlags |= 0x08;//include boundaries
    		
    		updateFlags |= 0x10;//include sub-world type
    	}
    	else
    	{
    		if (this.transformationHandler.getIsInMotion())
    			updateFlags |= 0x01 | 0x02;//include position and velocities
    		else if (MinecraftServer.getServer().getTickCounter() % 5 == 0)
    			updateFlags |= 0x01;
    		
    		if (this.centerChanged)
    		{
    			updateFlags |= 0x04;//include center coordinates
    			this.centerChanged = false;
    		}
    		
    		if (this.boundariesChanged)
    		{
    			updateFlags |= 0x08;//include boundaries
    			this.boundariesChanged = false;
    		}
    	}
    	MetaworldsMod.instance.networkHandler.sendToDimension(new SubWorldUpdatePacket(this, updateFlags), this.provider.dimensionId);
    	//PacketDispatcher.sendPacketToAllInDimension(new SubWorldUpdatePacket(this, updateFlags).makePacket(), this.provider.dimensionId);
    	
    	if (this.transformationHandler.getIsInMotion())
    	{
    		//Store local coordinates of entities
    		for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
        	{
    			curEntry.setValue(this.transformToLocal(curEntry.getKey()));
        	}
    		
    		for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
        	{
    			curEntry.setValue(this.transformToGlobal(curEntry.getKey()));
        	}
        	
    		this.setTranslation(this.getTranslationX() + this.getMotionX(), 
    							this.getTranslationY() + this.getMotionY(), 
    							this.getTranslationZ() + this.getMotionZ());
    		
        	this.setRotationYaw(this.getRotationYaw() + this.getRotationYawSpeed());
        	this.setRotationPitch(this.getRotationPitch() + this.getRotationPitchSpeed());
        	this.setRotationRoll(this.getRotationRoll() + this.getRotationRollSpeed());
        	
        	this.setScaling(this.getScaling() + this.getScaleChangeRate());
        	
        	//Change global coordinates of entities so that their local coordinates remain unchanged
        	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToDrag.entrySet())
        	{
        		Vec3 newPosition = this.transformToGlobal(curEntry.getValue());
        		if (curEntry.getKey() instanceof EntityPlayer)
        		{
        		    Entity curEntity = curEntry.getKey();
        		    double subWorldWeight = curEntity.getTractionFactor();
                    double globalWeight = 1.0d - subWorldWeight;
        		    
                    curEntity.setPosition(curEntity.posX * globalWeight + newPosition.xCoord * subWorldWeight, curEntity.posY * globalWeight + newPosition.yCoord * subWorldWeight, curEntity.posZ * globalWeight + newPosition.zCoord * subWorldWeight);
        		}
        		else
        			curEntry.getKey().setPositionAndRotation(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord, curEntry.getKey().rotationYaw - (float)this.getRotationYawSpeed(), curEntry.getKey().rotationPitch);
        	}
        	
        	for (Map.Entry<Entity, Vec3> curEntry : this.entitiesToNotDrag.entrySet())
        	{
        		Vec3 newPosition = this.transformToLocal(curEntry.getValue());
        		if (curEntry.getKey() instanceof EntityPlayer)
        			curEntry.getKey().setPosition(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord);
        		else
        			curEntry.getKey().setPositionAndRotation(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord, curEntry.getKey().rotationYaw + (float)this.getRotationYawSpeed(), curEntry.getKey().rotationPitch);
        	}
    	}
    	
    	//((ChunkProviderServerSubWorld)this.getChunkProvider()).updateForSubWorld();
    }
    
    //Override methods for world-time changing so that the time of day will not pass faster the more subworlds exist
    @Override
    public void func_82738_a(long par1) { }
    @Override
    public void setWorldTime(long par1) { }
    
    @Override
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
    	if (par1Entity.worldObj != null && par1Entity.worldObj != this)
    		return par1Entity.worldObj.spawnEntityInWorld(par1Entity);
    	
    	return super.spawnEntityInWorld(par1Entity);
    	
    	/*par1Entity.setWorld(this.m_parentWorld);
    	par1Entity.setPositionAndRotation(this.transformToGlobalX(par1Entity.posX, par1Entity.posZ), this.transformToGlobalY(par1Entity.posY), this.transformToGlobalZ(par1Entity.posX, par1Entity.posZ), (float)this.rotationYaw, 0.0F);
    	return this.m_parentWorld.spawnEntityInWorld(par1Entity);*/
    }
    
    public void registerEntityToDrag(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity/* && !(targetEntity instanceof EntityFX)*/ && ((Entity)targetEntity).worldObj != this)
    		this.entitiesToDrag.put((Entity)targetEntity, (Vec3)null);
    }
    
    public void unregisterEntityToDrag(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity/* && !(targetEntity instanceof EntityFX)*/ && ((Entity)targetEntity).worldObj != this)
    		this.entitiesToDrag.remove((Entity)targetEntity);
    }
    
    //Makes sure this entity is not moved with this world (in case the entity is spawned in this world) - used when worldBelowFeet for an entity changes
    public void registerDetachedEntity(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity/* && !(targetEntity instanceof EntityFX)*/ && ((Entity)targetEntity).worldObj == this)
    		this.entitiesToNotDrag.put((Entity)targetEntity, (Vec3)null);
    }
    
    public void unregisterDetachedEntity(EntitySuperClass targetEntity)
    {
    	if (targetEntity instanceof Entity/* && !(targetEntity instanceof EntityFX)*/ && ((Entity)targetEntity).worldObj == this)
    		this.entitiesToNotDrag.remove((Entity)targetEntity);
    }
    
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
    	return this.isMaterialInBB(par1AxisAlignedBB.getTransformedToLocalBoundingBox(this), par2Material);
    }
    
    @Override
    public boolean setBlock(int par1, int par2, int par3, Block par4, int par5, int par6)
    {
    	boolean result = super.setBlock(par1, par2, par3, par4, par5, par6);
    	
    	if (result)
    	{
	    	if (par4 != Blocks.air)//Block added
	    	{
	    		if (this.isEmpty)
	    			this.setBoundaries(par1, par2, par3, par1 + 1, par2 + 1, par3 + 1);
	    		else
		    		this.setBoundaries(Math.min(this.minCoordinates.posX, par1), Math.min(this.minCoordinates.posY, par2), Math.min(this.minCoordinates.posZ, par3), 
		    				Math.max(this.maxCoordinates.posX, par1 + 1), Math.max(this.maxCoordinates.posY, par2 + 1), Math.max(this.maxCoordinates.posZ, par3 + 1));
	    	}
	    	else if(!this.isEmpty)//Block possibly removed
	    	{
	    		int minX = this.getMinX();
	    		int minY = this.getMinY();
	    		int minZ = this.getMinZ();
	    		int maxX = this.getMaxX();
	    		int maxY = this.getMaxY();
	    		int maxZ = this.getMaxZ();
	    		boolean nowEmpty = this.isEmpty();
	    		//Check new boundaries in Y direction
		    	if (par2 == (maxY - 1))
	    		{
	    			int foundBlockAtY = minY - 1;
	    			
	    			int curY = par2;
	    			
					for (int curChunkX = minX >> 4; curChunkX <= (maxX - 1) >> 4; ++curChunkX)
					{
						for (int curChunkZ = minZ >> 4; curChunkZ <= (maxZ - 1) >> 4; ++curChunkZ)
						{
							Chunk curChunk = getChunkFromChunkCoords(curChunkX, curChunkZ);

							if (curChunk.getAreLevelsEmpty(minY, curY))
								continue;
							
							for (int inChunkX = 0; inChunkX < 16; ++inChunkX)
							{
								int curX = (curChunkX << 4) + inChunkX;
								
								if (curX < minX)
									continue;
								else if (curX >= maxX)
									break;
								
								for (int inChunkZ = 0; inChunkZ < 16; ++inChunkZ)
								{
									int curZ = (curChunkZ << 4) + inChunkZ;
									
									if (curZ < minZ)
										continue;
									else if (curZ >= maxZ)
										break;
									
									foundBlockAtY = Math.max(foundBlockAtY, curChunk.getHeightValue(inChunkX, inChunkZ) - 1);
									
									if (foundBlockAtY >= maxY - 1)
										break;
								}
								
								if (foundBlockAtY >= maxY - 1)
									break;
							}
							
							if (foundBlockAtY >= maxY - 1)
								break;
						}
						
						if (foundBlockAtY >= maxY - 1)
							break;
					}
					
					if (foundBlockAtY == (minY - 1))
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtY + 1 != maxY)
	    				this.boundariesChanged = true;
	    			
	    			maxY = foundBlockAtY + 1;
	    		}
		    	else if (par2 == minY)
	    		{
	    			int foundBlockAtY = maxY;
	    			
	    			for (int curY = par2; curY < maxY && foundBlockAtY == maxY; ++curY)
	    			{
	    				for (int curChunkX = minX >> 4; curChunkX <= (maxX - 1) >> 4; ++curChunkX)
	    				{
	    					for (int curChunkZ = minZ >> 4; curChunkZ <= (maxZ - 1) >> 4; ++curChunkZ)
	    					{
	    						Chunk curChunk = getChunkFromChunkCoords(curChunkX, curChunkZ);
	    						
	    						//getAreLevelsEmpty checks 16 levels at once so we still need to check further if it returns false
	    						if (curChunk.getAreLevelsEmpty(curY, curY))
	    							continue;
	    						
	    						ExtendedBlockStorage curExtBlockStorage = curChunk.getBlockStorageArray()[curY >> 4];
	    						for (int inChunkX = 0; inChunkX < 16; ++inChunkX)
	    						{
	    							int curX = (curChunkX << 4) + inChunkX;
	    							
	    							if (curX < minX)
	    								continue;
	    							else if (curX >= maxX)
	    								break;
	    							
	    							for (int inChunkZ = 0; inChunkZ < 16; ++inChunkZ)
	    							{
	    								int curZ = (curChunkZ << 4) + inChunkZ;
	    								
	    								if (curZ < minZ)
	    									continue;
	    								else if (curZ >= maxZ)
	    									break;
	    								
	    								if (curExtBlockStorage.getBlockByExtId(inChunkX, curY & 15, inChunkZ) != Blocks.air)
	    								{
	    									foundBlockAtY = curY;
	    									break;
	    								}
	    							}
	    							
	    							if (foundBlockAtY != maxY)
	    								break;
	    						}
	    						
	    						if (foundBlockAtY != maxY)
									break;
	    					}
	    					
	    					if (foundBlockAtY != maxY)
								break;
	    				}
	    				
	    				if (foundBlockAtY != maxY)
							break;
	    			}
	    			
	    			if (foundBlockAtY == maxY)
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtY != minZ)
	    				this.boundariesChanged = true;
	    			
	    			minY = foundBlockAtY;
	    		}
	    		
	    		//Check new boundaries in X direction
	    		if (!nowEmpty && par1 == minX)
	    		{
	    			int foundBlockAtX = maxX;
	    			
	    			for (int curX = par1; curX < maxX && foundBlockAtX == maxX; ++curX)
	    			{
	    				for (int curChunkZ = minZ >> 4; curChunkZ <= ((maxZ - 1) >> 4) && foundBlockAtX == maxX; ++curChunkZ)
	    				{
	    					Chunk curChunk = getChunkFromChunkCoords((curX >> 4), curChunkZ);
		    				int inChunkX = curX & 15;
		    				
		    				for (int inChunkZ = 0; inChunkZ < 16; ++inChunkZ)
		    				{
		    					int curZ = (curChunkZ << 4) + inChunkZ; 
		    					
		    					if (curZ < minZ)
		    						continue;
		    					else if(curZ > (maxZ - 1))
		    						break;
		    					
		    					if (curChunk.getHeightValue(inChunkX, inChunkZ) > 0)
		    					{
		    						foundBlockAtX = curX;
		    						break;
		    					}
		    				}
	    				}
	    			}
	    			
	    			if (foundBlockAtX == maxX)
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtX != minX)
	    				this.boundariesChanged = true;
	    			
	    			minX = foundBlockAtX;
	    		}
	    		else if (!nowEmpty && par1 == (maxX - 1))
	    		{
	    			int foundBlockAtX = minX - 1;
	    			
	    			for (int curX = par1; curX >= minX && foundBlockAtX == (minX - 1); --curX)
	    			{
	    				for (int curChunkZ = minZ >> 4; curChunkZ <= ((maxZ - 1) >> 4) && foundBlockAtX == (minX - 1); ++curChunkZ)
	    				{
	    					Chunk curChunk = getChunkFromChunkCoords((curX >> 4), curChunkZ);
		    				int inChunkX = curX & 15;
		    				
		    				for (int inChunkZ = 0; inChunkZ < 16; ++inChunkZ)
		    				{
		    					int curZ = (curChunkZ << 4) + inChunkZ; 
		    					
		    					if (curZ < minZ)
		    						continue;
		    					else if(curZ > (maxZ - 1))
		    						break;
		    					
		    					if (curChunk.getHeightValue(inChunkX, inChunkZ) > 0)
		    					{
		    						foundBlockAtX = curX;
		    						break;
		    					}
		    				}
	    				}
	    			}
	    			
	    			if (foundBlockAtX == (minX - 1))
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtX + 1 != maxX)
	    				this.boundariesChanged = true;
	    			
	    			maxX = foundBlockAtX + 1;
	    		}
	    	
		    	//Check new boundaries in Z direction
		    	if (!nowEmpty && par3 == minZ)
	    		{
	    			int foundBlockAtZ = maxZ;
	    			
	    			for (int curZ = par3; curZ < maxZ && foundBlockAtZ == maxZ; ++curZ)
	    			{
	    				for (int curChunkX = minX >> 4; curChunkX <= ((maxX - 1) >> 4) && foundBlockAtZ == maxZ; ++curChunkX)
	    				{
	    					Chunk curChunk = getChunkFromChunkCoords(curChunkX, (curZ >> 4));
		    				int inChunkZ = curZ & 15;
		    				
		    				for (int inChunkX = 0; inChunkX < 16; ++inChunkX)
		    				{
		    					int curX = (curChunkX << 4) + inChunkX; 
		    					
		    					if (curX < minX)
		    						continue;
		    					else if(curX > (maxX - 1))
		    						break;
		    					
		    					if (curChunk.getHeightValue(inChunkX, inChunkZ) > 0)
		    					{
		    						foundBlockAtZ = curZ;
		    						break;
		    					}
		    				}
	    				}
	    			}
	    			
	    			if (foundBlockAtZ == maxZ)
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtZ != minZ)
	    				this.boundariesChanged = true;
	    			
	    			minZ = foundBlockAtZ;
	    		}
	    		else if (!nowEmpty && par3 == (maxZ - 1))
	    		{
	    			int foundBlockAtZ = minZ - 1;
	    			
	    			for (int curZ = par3; curZ >= minZ && foundBlockAtZ == (minZ - 1); --curZ)
	    			{
	    				for (int curChunkX = minX >> 4; curChunkX <= ((maxX - 1) >> 4) && foundBlockAtZ == (minZ - 1); ++curChunkX)
	    				{
	    					Chunk curChunk = getChunkFromChunkCoords(curChunkX, (curZ >> 4));
		    				int inChunkZ = curZ & 15;
		    				
		    				for (int inChunkX = 0; inChunkX < 16; ++inChunkX)
		    				{
		    					int curX = (curChunkX << 4) + inChunkX; 
		    					
		    					if (curX < minX)
		    						continue;
		    					else if(curX > (maxX - 1))
		    						break;
		    					
		    					if (curChunk.getHeightValue(inChunkX, inChunkZ) > 0)
		    					{
		    						foundBlockAtZ = curZ;
		    						break;
		    					}
		    				}
	    				}
	    			}
	    			
	    			if (foundBlockAtZ == (minZ - 1))
	    				nowEmpty = true;
	    			
	    			if (foundBlockAtZ + 1 != maxZ)
	    				this.boundariesChanged = true;
	    			
	    			maxZ = foundBlockAtZ + 1;
	    		}
		    	
		    	this.setBoundaries(minX, minY, minZ, maxX, maxY, maxZ);
	    	}
    	}
    	
    	return result;
    }
    
    @Override public void updateWeatherBody() { }
    
    @Override public long getTotalWorldTime()
    {
        return m_parentWorld.getTotalWorldTime();
    }
    
    /*@Override public EntityPlayer getClosestPlayer(double par1, double par3, double par5, double par7)
    {
    	Vec3 transformedPos = this.transformToGlobal(par1, par3, par5);
    	return m_parentWorld.getClosestPlayer(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord, par7);
    }*/
    
    @Override
    public Chunk createNewChunk(int xPos, int zPos)
    {
    	return new ChunkSubWorld(this, xPos, zPos);
    }
    
    @Override
    public boolean canBlockFreezeBody(int par1, int par2, int par3, boolean par4)
    {
        return false;
    }
}

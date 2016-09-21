package metaworlds.patcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jblas.DoubleMatrix;

import metaworlds.api.SubWorld;
import metaworlds.api.WorldSuperClass;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class WorldTransformable implements WorldSuperClass 
{
	private final boolean isSubWorld = (this instanceof SubWorld);
	public Map<Integer, World> childSubWorlds = new TreeMap<Integer, World>();
	private UnmodifiableSingleObjPlusCollection<World> allWorlds = new UnmodifiableSingleObjPlusCollection((World)this, childSubWorlds.values());
	
	public abstract World CreateSubWorld();
	public abstract World CreateSubWorld(int newSubWorldID);
	
	public Collection<World> getWorlds()
	{
		return this.allWorlds;
	}
    
	public Collection<World> getSubWorlds()
	{
		return this.childSubWorlds.values();
	}
	
	public Map<Integer, World> getSubWorldsMap()
	{
		return this.childSubWorlds;
	}
	
	public int getWorldsCount() //Including this one
	{
		return this.childSubWorlds.size() + 1;
	}
	
    public int getSubWorldID()
    {
    	return 0;
    }
    
    public World getParentWorld()
    {
    	return (World)this;
    }
    
    public World getSubWorld(int targetSubWorldID)
    {
    	if (targetSubWorldID < 0)// || targetSubWorldID > childSubWorlds.size())
    		return null;
    	else if (targetSubWorldID == 0)
    		return (World)this;
    	
    	return childSubWorlds.get(targetSubWorldID);
    }
    
    public boolean isSubWorld()
    {
    	return this.isSubWorld;
    }
    
    public double getTranslationX() { return 0.0D; }
    public double getTranslationY() { return 0.0D; }
    public double getTranslationZ() { return 0.0D; }
	
	public double getRotationYaw() { return 0.0D; }
	public double getRotationPitch() { return 0.0D; }
	public double getRotationRoll() { return 0.0D; }
	
	public double getScaling() { return 1.0D; }
	
	public double getCenterX() { return 0.0D; }
	public double getCenterY() { return 0.0D; }
	public double getCenterZ() { return 0.0D; }
    
	//Transformations
	public Vec3 transformToGlobal(Entity localEntity)
    {
    	return this.transformToGlobal(localEntity.posX, localEntity.posY, localEntity.posZ);
    }
	public Vec3 transformToGlobal(Vec3 localVec) { return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord); }
    public Vec3 transformToGlobal(double localX, double localY, double localZ) 
    { 
    	return ((World)this).getWorldVec3Pool().getVecFromPool(localX, localY, localZ); 
    }
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) { return localVectors.dup(); }
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) 
    {
    	result.copy(localVectors);
    	return result;
    }
	
    public Vec3 transformToLocal(Entity globalEntity)
	{
		return this.transformToLocal(globalEntity.posX, globalEntity.posY, globalEntity.posZ);
	}
    public Vec3 transformToLocal(Vec3 globalVec) { return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord); }
    public Vec3 transformToLocal(double globalX, double globalY, double globalZ)
    {
    	return ((World)this).getWorldVec3Pool().getVecFromPool(globalX, globalY, globalZ);
    }
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) { return globalVectors.dup(); }
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result)
    {
    	result.copy(globalVectors);
    	return result;
    }
    
    //Transform from this world's coordinates to another world's coordinates
    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity)
    {
    	return this.transformLocalToOther(targetWorld, localEntity.posX, localEntity.posY, localEntity.posZ);
    }
    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec)
    {
    	if (targetWorld == null)
    		return this.transformToLocal(localVec);
    	
    	return targetWorld.transformToLocal(localVec);
    }
    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ)
    {
    	if (targetWorld == null)
    		return ((World)this).getWorldVec3Pool().getVecFromPool(localX, localY, localZ);
    	
    	return targetWorld.transformToLocal(localX, localY, localZ);
    }
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors)
    {
    	if (targetWorld == null)
    		return localVectors.dup();
    	
    	return targetWorld.transformToLocal(localVectors);
    }
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result)
    {
    	if (targetWorld == null)
    		return result.copy(localVectors);
    	
    	return targetWorld.transformToLocal(localVectors, result);
    }
    
    //Transform from another world's coordinates to this world's coordinates
    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity)
    {
    	return this.transformOtherToLocal(sourceWorld, otherEntity.posX, otherEntity.posY, otherEntity.posZ);
    }
    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec)
    {
    	if (sourceWorld == null)
    		return this.transformToGlobal(otherVec);
    	
    	return sourceWorld.transformToGlobal(otherVec);
    }
    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ)
    {
    	if (sourceWorld == null)
    		return ((World)this).getWorldVec3Pool().getVecFromPool(otherX, otherY, otherZ);
    	
    	return sourceWorld.transformToGlobal(otherX, otherY, otherZ);
    }
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors)
    {
    	if (sourceWorld == null)
    		return otherVectors.dup();
    	
    	return sourceWorld.transformToGlobal(otherVectors);
    }
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result)
    {
    	if (sourceWorld == null)
    		return result.copy(otherVectors);
    	
    	return sourceWorld.transformToGlobal(otherVectors, result);
    }
    
    //Rotations
    public Vec3 rotateToGlobal(Vec3 localVec) { return localVec; }
    public Vec3 rotateToGlobal(double localX, double localY, double localZ)
    {
    	return ((World)this).getWorldVec3Pool().getVecFromPool(localX, localY, localZ);
    }
    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) { return localVectors; }
    
    public Vec3 rotateToLocal(Vec3 globalVec) {return globalVec; }
    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ)
    {
    	return ((World)this).getWorldVec3Pool().getVecFromPool(globalX, globalY, globalZ);
    }
    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) {return globalVectors; }
    
    ////////////////
    
    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	return ((WorldIntermediateClass)this).getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB);
    }
    
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
        return ((WorldIntermediateClass)this).getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB);
    }
	
	public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector)
    {
		return ((WorldIntermediateClass)this).getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector);
    }
	
	public void doTickPartial(double interpolationFactor) { }
	
	public boolean isChunkWatchable(int chunkX, int chunkZ) { return true; }
	
	public Chunk createNewChunk(int xPos, int zPos) { return new Chunk((World)this, xPos, zPos); }
}

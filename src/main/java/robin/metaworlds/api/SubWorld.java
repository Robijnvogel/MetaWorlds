package robin.metaworlds.api;

import java.nio.DoubleBuffer;
import java.util.List;

import org.jblas.DoubleMatrix;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public interface SubWorld 
{
	public World getParentWorld();
	public int getSubWorldID();
	
	public void removeSubWorld();//Unloads the SubWorld without deleting its data from the server
	
	public double getTranslationX();
	public double getTranslationY();
	public double getTranslationZ();
	
	public double getRotationYaw();
	public double getRotationPitch();
	public double getRotationRoll();
	
	public double getCosRotationYaw();
	public double getSinRotationYaw();
	public double getCosRotationPitch();
	public double getSinRotationPitch();
	public double getCosRotationRoll();
	public double getSinRotationRoll();
	
	public double getScaling();
	
	//Returns the position in local coordinates around which the world is rotating and scaling
	public double getCenterX();
	public double getCenterY();
	public double getCenterZ();
	
	//Deprecated:
	//public void setTranslationX(double newX);
	//public void setTranslationY(double newY);
	//public void setTranslationZ(double newZ);
	
	public void setTranslation(double newX, double newY, double newZ);
	public void setTranslation(Vec3 newTranslation);
	
	public void setRotationYaw(double newYaw);
	public void setRotationPitch(double newPitch);
	public void setRotationRoll(double newRoll);
	
	public void setScaling(double newScaling);
	
	//Sets the position in LOCAL coordinates around which the world is rotating and scaling
	public void setCenter(double newX, double newY, double newZ);
	public void setCenter(Vec3 newCenter);
	
	public void setMotion(double par1MotionX, double par2MotionY, double par3MotionZ);
	
	public void setRotationYawSpeed(double par1Speed);
	public void setRotationPitchSpeed(double par1Speed);
	public void setRotationRollSpeed(double par1Speed);
	
	public void setScaleChangeRate(double par1Rate);
	
	public double getMotionX();
	public double getMotionY();
	public double getMotionZ();
	
	public double getRotationYawSpeed();
	public double getRotationPitchSpeed();
	public double getRotationRollSpeed();
	
	public double getScaleChangeRate();
	
	//Information about total size of the subworld
	public int getMinX();
	public int getMinY();
	public int getMinZ();
	public ChunkCoordinates getMinCoordinates();
	public int getMaxX();
	public int getMaxY();
	public int getMaxZ();
	public ChunkCoordinates getMaxCoordinates();
	
	//setBoundaries should be used carefully as world block changes also act on the boundaries
	//so if you set boundaries manually don't assume them to remain unchanged
	public void setBoundaries(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
	
	//Transformation matrices
	public DoubleBuffer getTransformToLocalMatrixDirectBuffer();
	public DoubleBuffer getTransformToGlobalMatrixDirectBuffer();
	
	//Transformations
	//public double transformToLocalX(double globalPosX, double globalPosZ);
    //public double transformToLocalY(double globalPosY);
    //public double transformToLocalZ(double globalPosX, double globalPosZ);
    
	public Vec3 transformToLocal(Entity globalEntity);
	public Vec3 transformToLocal(Vec3 globalVec);
    public Vec3 transformToLocal(double globalX, double globalY, double globalZ);
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors);
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result);//In-Place operation for smaller memory footprint
    
    //public double transformToGlobalX(double localPosX, double localPosZ);
    //public double transformToGlobalY(double localPosY);
    //public double transformToGlobalZ(double lcoalPosX, double localPosZ);
    
    public Vec3 transformToGlobal(Entity localEntity);
    public Vec3 transformToGlobal(Vec3 localVec);
    public Vec3 transformToGlobal(double localX, double localY, double localZ);
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors);
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result);//In-Place operation for smaller memory footprint
    
    //Transform from this world's coordinates to another world's coordinates
    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity);
    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec);
    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ);
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors);
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result);//In-Place operation for smaller memory footprint
    
    //Transform from another world's coordinates to this world's coordinates
    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity);
    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec);
    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ);
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors);
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result);//In-Place operation for smaller memory footprint 
    
    //Deprecated
    //public double rotateToGlobalX(double localX, double localZ);
    //public double rotateToGlobalY(double localY);
    //public double rotateToGlobalZ(double localX, double localZ);
    
    //public double rotateToLocalX(double globalX, double globalZ);
    //public double rotateToLocalY(double globalY);
    //public double rotateToLocalZ(double globalX, double globalZ);
    
    //Apply yaw, pitch, roll rotation
    public Vec3 rotateToGlobal(Vec3 localVec);
    public Vec3 rotateToGlobal(double localX, double localY, double localZ);
    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors);
    
    public Vec3 rotateToLocal(Vec3 globalVec);
    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ);
    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors);
    
    //Apply yaw rotation only
    public Vec3 rotateYawToGlobal(Vec3 localVec);
    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ);
    public DoubleMatrix rotateYawToGlobal(DoubleMatrix localVectors);
    
    public Vec3 rotateYawToLocal(Vec3 globalVec);
    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ);
    public DoubleMatrix rotateYawToLocal(DoubleMatrix globalVectors);
    
    //public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);
    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);
    
    //same as isAABBInMaterialGlobal but expects par1AxisAlignedBB in global coordinates, not in local
    public boolean isAABBInMaterialGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material);
    public boolean isMaterialInBBGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material);
    
    ////////////////////////////////
    //Meant for internal use only:
    ////////////////////////////////
    //Makes sure this entity keeps its local coordinates in this world when this world moves - used when worldBelowFeet for an entity changes
    public void registerEntityToDrag(EntitySuperClass targetEntity);
    public void unregisterEntityToDrag(EntitySuperClass targetEntity);
    
    //Makes sure this entity is not moved with this world (in case the entity is spawned in this world) - used when worldBelowFeet for an entity changes
    public void registerDetachedEntity(EntitySuperClass targetEntity);
    public void unregisterDetachedEntity(EntitySuperClass targetEntity);
}

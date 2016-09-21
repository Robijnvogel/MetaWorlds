package metaworlds.core;

import java.nio.DoubleBuffer;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.jblas.DoubleMatrix;
import org.lwjgl.BufferUtils;

public class SubWorldTransformationHandler {
	
	World holderWorld;
	
	public SubWorldTransformationHandler(World thisHolderWorld)
	{
		this.holderWorld = thisHolderWorld;
	}
	
	/** World origin translation relative to parent world's origin */
	private double translationX;
	private double translationY;
	private double translationZ;
	
	private double motionX;
	private double motionY;
	private double motionZ;
	
	/** World center point for rotation and scaling */
	private double centerX;
	private double centerY;
	private double centerZ;
	
	/** World rotation */
    private double rotationYaw;
    private double rotationPitch;
    private double rotationRoll;
    
    private double cosRotationYaw = 1.0d;
    private double sinRotationYaw;
    private double cosRotationPitch = 1.0d;
    private double sinRotationPitch;
    private double cosRotationRoll = 1.0d;
    private double sinRotationRoll;
    
    private double rotationYawFrequency; //Degree per tick
    private double rotationPitchFrequency;
    private double rotationRollFrequency;
    
    /** World scaling */
    private double scaling = 1.0d;
    
    private double scaleChangeRate;
    
	////////////////////////
	// Matrices
	////////////////////////
	
    //Translation matrices
	DoubleMatrix matrixTranslation = DoubleMatrix.eye(4);
	DoubleMatrix matrixTranslationInverse = DoubleMatrix.eye(4);
	
	//Center translation matrices
	DoubleMatrix matrixCenterTranslation = DoubleMatrix.eye(4);
	DoubleMatrix matrixCenterTranslationInverse = DoubleMatrix.eye(4);
	
	//Rotation matrices
	boolean needRotationRecalc = false;
	DoubleMatrix matrixRotationYaw = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationYawInverse = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationPitch = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationPitchInverse = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationRoll = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationRollInverse = DoubleMatrix.eye(4);
	
	DoubleMatrix matrixRotation = DoubleMatrix.eye(4);
	DoubleMatrix matrixRotationInverse = DoubleMatrix.eye(4);
	
	//Scaling matrices
	DoubleMatrix matrixScaling = DoubleMatrix.eye(4);
	DoubleMatrix matrixScalingInverse = DoubleMatrix.eye(4);
	
	//Full transformation matrices
	boolean needTransformationRecalc = false;
	DoubleMatrix matrixTransformToLocal = DoubleMatrix.eye(4);
	DoubleMatrix matrixTransformToGlobal = DoubleMatrix.eye(4);
	
	
	////////////////////////
	// Getters
	////////////////////////
	
	public double getTranslationX() { return this.translationX; }
	public double getTranslationY() { return this.translationY; }
	public double getTranslationZ() { return this.translationZ; }
	
	public double getCenterX() { return this.centerX; }
	public double getCenterY() { return this.centerY; }
	public double getCenterZ() { return this.centerZ; }
	
	public double getRotationYaw() { return this.rotationYaw; }
	public double getRotationPitch() { return this.rotationPitch; }
	public double getRotationRoll() { return this.rotationRoll; }
	
	public double getCosRotationYaw() { return this.cosRotationYaw; }
	public double getSinRotationYaw() { return this.sinRotationYaw; }
	public double getCosRotationPitch() { return this.cosRotationPitch; }
	public double getSinRotationPitch() { return this.sinRotationPitch; }
	public double getCosRotationRoll() { return this.cosRotationRoll; }
	public double getSinRotationRoll() { return this.sinRotationRoll; }
	
	public double getScaling() { return this.scaling; }
	
	////////////////////////
	// Setters
	////////////////////////
	
	public void setTranslation(Vec3 newTranslation)
	{
		this.setTranslation(newTranslation.xCoord, newTranslation.yCoord, newTranslation.zCoord);
	}
	
	public void setTranslation(double newX, double newY, double newZ)
	{
		this.translationX = newX;
		this.translationY = newY;
		this.translationZ = newZ;
		
		this.matrixTranslation.data[12] = newX;
		this.matrixTranslation.data[13] = newY;
		this.matrixTranslation.data[14] = newZ;
		this.matrixTranslationInverse.data[12] = -newX;
		this.matrixTranslationInverse.data[13] = -newY;
		this.matrixTranslationInverse.data[14] = -newZ;
		this.needTransformationRecalc = true;
	}
	
	public void setCenter(Vec3 newCenter)
	{
		this.setCenter(newCenter.xCoord, newCenter.yCoord, newCenter.zCoord);
	}
	
	public void setCenter(double newX, double newY, double newZ)
	{
		//Make sure the world does not move by changing the center
		Vec3 oldGlobalPos = this.transformToGlobal(0, 0, 0);
		
		this.centerX = newX;
		this.centerY = newY;
		this.centerZ = newZ;
		
		this.matrixCenterTranslation.data[12] = -newX;
		this.matrixCenterTranslation.data[13] = -newY;
		this.matrixCenterTranslation.data[14] = -newZ;
		this.matrixCenterTranslationInverse.data[12] = newX;
		this.matrixCenterTranslationInverse.data[13] = newY;
		this.matrixCenterTranslationInverse.data[14] = newZ;
		this.needTransformationRecalc = true;
		
		Vec3 newGlobalPos = this.transformToGlobal(0, 0, 0);
		this.setTranslation(this.getTranslationX() + oldGlobalPos.xCoord - newGlobalPos.xCoord, this.getTranslationY() + oldGlobalPos.yCoord - newGlobalPos.yCoord, this.getTranslationZ() + oldGlobalPos.zCoord - newGlobalPos.zCoord);
	}
	
	public void setRotationYaw(double newYaw)
	{
		this.rotationYaw = newYaw;
		this.cosRotationYaw = Math.cos(newYaw * Math.PI / 180.0d);
		this.sinRotationYaw = Math.sin(newYaw * Math.PI / 180.0d);
		
		this.matrixRotationYaw.data[0] = this.cosRotationYaw;
		this.matrixRotationYaw.data[8] = this.sinRotationYaw;
		this.matrixRotationYaw.data[2] = -this.sinRotationYaw;
		this.matrixRotationYaw.data[10] = this.cosRotationYaw;
		
		this.matrixRotationYawInverse.data[0] = this.cosRotationYaw;
		this.matrixRotationYawInverse.data[8] = -this.sinRotationYaw;
		this.matrixRotationYawInverse.data[2] = this.sinRotationYaw;
		this.matrixRotationYawInverse.data[10] = this.cosRotationYaw;
		
		this.needRotationRecalc = true;
		this.needTransformationRecalc = true;
	}
	
	public void setRotationPitch(double newPitch)
	{
		this.rotationPitch = newPitch;
		this.cosRotationPitch = Math.cos(newPitch * Math.PI / 180.0d);
		this.sinRotationPitch = Math.sin(newPitch * Math.PI / 180.0d);
		
		this.matrixRotationPitch.data[0] = this.cosRotationPitch;
		this.matrixRotationPitch.data[1] = this.sinRotationPitch;
		this.matrixRotationPitch.data[4] = -this.sinRotationPitch;
		this.matrixRotationPitch.data[5] = this.cosRotationPitch;
		
		this.matrixRotationPitchInverse.data[0] = this.cosRotationPitch;
		this.matrixRotationPitchInverse.data[1] = -this.sinRotationPitch;
		this.matrixRotationPitchInverse.data[4] = this.sinRotationPitch;
		this.matrixRotationPitchInverse.data[5] = this.cosRotationPitch;
		
		this.needRotationRecalc = true;
		this.needTransformationRecalc = true;
	}
	
	public void setRotationRoll(double newRoll)
	{
		this.rotationRoll = newRoll;
		this.cosRotationRoll = Math.cos(newRoll * Math.PI / 180.0d);
		this.sinRotationRoll = Math.sin(newRoll * Math.PI / 180.0d);
		
		this.matrixRotationRoll.data[5] = this.cosRotationRoll;
		this.matrixRotationRoll.data[6] = this.sinRotationRoll;
		this.matrixRotationRoll.data[9] = -this.sinRotationRoll;
		this.matrixRotationRoll.data[10] = this.cosRotationRoll;
		
		this.matrixRotationRollInverse.data[5] = this.cosRotationRoll;
		this.matrixRotationRollInverse.data[6] = -this.sinRotationRoll;
		this.matrixRotationRollInverse.data[9] = this.sinRotationRoll;
		this.matrixRotationRollInverse.data[10] = this.cosRotationRoll;
		
		this.needRotationRecalc = true;
		this.needTransformationRecalc = true;
	}
	
	public void setScaling(double newScaling)
	{
		if (Math.abs(newScaling) < 0.0001d)
			newScaling = 0.0001d;
		
		this.scaling = newScaling;
		
		this.matrixScaling.data[0] = this.scaling;
		this.matrixScaling.data[5] = this.scaling;
		this.matrixScaling.data[10] = this.scaling;
		//this.matrixScaling.data[15] = this.scaling;
		
		this.matrixScalingInverse.data[0] = 1.0d / this.scaling;
		this.matrixScalingInverse.data[5] = 1.0d / this.scaling;
		this.matrixScalingInverse.data[10] = 1.0d / this.scaling;
		//this.matrixScalingInverse.data[15] = 1.0d / this.scaling;
		
		this.needTransformationRecalc = true;
	}
	
	
	
	
	
	
	
	
	
	public void recalcRotationMatrix()
	{
		if (!this.needRotationRecalc)
			return;
		
		//this.matrixRotation = this.matrixRotationYaw;
		this.matrixRotation = this.matrixRotationYaw.mmul(this.matrixRotationPitch.mmul(this.matrixRotationRoll));
		//this.matrixRotationInverse = this.matrixRotationYawInverse;
		this.matrixRotationInverse = this.matrixRotationRollInverse.mmul(this.matrixRotationPitchInverse.mmul(this.matrixRotationYawInverse));
		
		this.needRotationRecalc = false;
	}
	
	public void recalcTransformationMatrices()
	{
		this.recalcRotationMatrix();
		
		if (!this.needTransformationRecalc)
			return;
		
		/**Transformation Local to Global:
		 * move to rotation/scaling center
		 * rotate
		 * scale
		 * move back from rotation/scaling center
		 * translate to global position
		*/
		//this.matrixTransformToGlobal = this.matrixTranslation.mmul(this.matrixScaling.mmul(this.matrixRotation));
		this.matrixTransformToGlobal = this.matrixTranslation.mmul(this.matrixCenterTranslationInverse.mmul(this.matrixScaling.mmul(this.matrixRotation.mmul(this.matrixCenterTranslation))));
		
		/**Transformation Global to Local:
		 * translate from global position
		 * move to rotation/scaling center
		 * scale back
		 * rotate back
		 * move back from rotation/scaling center
		*/
		//this.matrixTransformToLocal = this.matrixRotationInverse.mmul(this.matrixScalingInverse.mmul(this.matrixTranslationInverse));
		this.matrixTransformToLocal = this.matrixCenterTranslationInverse.mmul(this.matrixRotationInverse.mmul(this.matrixScalingInverse.mmul(this.matrixCenterTranslation.mmul(this.matrixTranslationInverse))));
		
		this.needTransformationRecalc = false;
	}
	
	
	
	
	
	
	
	
	public void setMotion(double par1MotionX, double par2MotionY, double par3MotionZ)
	{
		this.motionX = par1MotionX;
		this.motionY = par2MotionY;
		this.motionZ = par3MotionZ;
	}
	
	public void setRotationYawSpeed(double par1Speed) { this.rotationYawFrequency = par1Speed; }
	public void setRotationPitchSpeed(double par1Speed) { this.rotationPitchFrequency = par1Speed; }
	public void setRotationRollSpeed(double par1Speed) { this.rotationRollFrequency = par1Speed; }
	
	public void setScaleChangeRate(double par1Rate) { this.scaleChangeRate = par1Rate; }
	
	public double getMotionX() { return this.motionX; }
	public double getMotionY() { return this.motionY; }
	public double getMotionZ() { return this.motionZ; }
	
	public double getRotationYawSpeed() { return this.rotationYawFrequency; }
	public double getRotationPitchSpeed() { return this.rotationPitchFrequency; }
	public double getRotationRollSpeed() { return this.rotationRollFrequency; }
	
	public double getScaleChangeRate() { return this.scaleChangeRate; }
	
	public boolean getIsInMotion() 
	{
		return this.motionX != 0.0D || this.motionY != 0.0D || this.motionZ != 0.0D || this.rotationYawFrequency != 0.0D || this.rotationPitchFrequency != 0.0D || this.rotationRollFrequency != 0.0D || this.scaleChangeRate != 0.0D;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public DoubleMatrix getRotationMatrix()
	{
		this.recalcRotationMatrix();
		
		return this.matrixRotation;
	}
	
	public DoubleMatrix getRotationInverseMatrix()
	{
		this.recalcRotationMatrix();
		
		return this.matrixRotationInverse;
	}
	
	public DoubleMatrix getTransformToLocalMatrix()
	{
		this.recalcTransformationMatrices();
		
		return this.matrixTransformToLocal;
	}
	
	public DoubleMatrix getTransformToGlobalMatrix()
	{
		this.recalcTransformationMatrices();
		
		return this.matrixTransformToGlobal;
	}
	
	public DoubleBuffer getTransformToLocalMatrixDirectBuffer()
	{
		this.recalcTransformationMatrices();
		
		return (DoubleBuffer) BufferUtils.createDoubleBuffer(16).put(this.matrixTransformToLocal.data).rewind();
	}
	
	public DoubleBuffer getTransformToGlobalMatrixDirectBuffer()
	{
		this.recalcTransformationMatrices();
		
		return (DoubleBuffer) BufferUtils.createDoubleBuffer(16).put(this.matrixTransformToGlobal.data).rewind();
	}

	////////////////////////////////
	// Transformation to local
	////////////////////////////////
	
    public Vec3 transformToLocal(Vec3 globalVec)
    {
    	return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }
    
    public Vec3 transformToLocal(double globalX, double globalY, double globalZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{globalX, globalY, globalZ, 1});
    	this.transformToLocal(tempVector, tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors)
    {
    	return this.getTransformToLocalMatrix().mmul(globalVectors);
    }
    
    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result)
    {
    	return this.getTransformToLocalMatrix().mmuli(globalVectors, result);
    }
    
	////////////////////////////////
	// Transformation to global
	////////////////////////////////
    
    public Vec3 transformToGlobal(Vec3 localVec)
    {
    	return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }
    
    public Vec3 transformToGlobal(double localX, double localY, double localZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{localX, localY, localZ, 1});
    	this.transformToGlobal(tempVector, tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors)
    {
    	return this.getTransformToGlobalMatrix().mmul(localVectors);
    }
    
    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result)
    {
    	return this.getTransformToGlobalMatrix().mmuli(localVectors, result);
    }
    
    //Transform from this world's coordinates to another world's coordinates
    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec)
    {
    	return this.transformLocalToOther(targetWorld, localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }
    
    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ)
    {
    	if (targetWorld == null)
    		targetWorld = this.holderWorld.getParentWorld();
    	
    	if (!targetWorld.isSubWorld())
    		return this.transformToGlobal(localX, localY, localZ);
    	else if (targetWorld == this.holderWorld)
    		return this.holderWorld.getWorldVec3Pool().getVecFromPool(localX, localY, localZ);
    	
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{localX, localY, localZ, 1});
    	this.transformLocalToOther(targetWorld, tempVector, tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors)
    {
    	if (targetWorld == null)
    		targetWorld = this.holderWorld.getParentWorld();
    	
    	if (targetWorld == this.holderWorld)
    		return localVectors.dup();
    	
    	DoubleMatrix tempVectors = this.transformToGlobal(localVectors);
    	if (!targetWorld.isSubWorld())
    		return tempVectors;
    	return targetWorld.transformToLocal(tempVectors, tempVectors);
    }
    
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result)
    {
    	if (targetWorld == null)
    		targetWorld = this.holderWorld.getParentWorld();
    	
    	if (targetWorld == this.holderWorld)
    		return result.copy(localVectors);
    	
    	localVectors = this.transformToGlobal(localVectors, result);
    	if (!targetWorld.isSubWorld())
    		return localVectors;
    	return targetWorld.transformToLocal(localVectors, result);
    }
    
    //Transform from another world's coordinates to this world's coordinates
    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec)
    {
    	return this.transformOtherToLocal(sourceWorld, otherVec.xCoord, otherVec.yCoord, otherVec.zCoord);
    }
    
    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ)
    {
    	if (sourceWorld == null)
    		sourceWorld = this.holderWorld.getParentWorld();
    	
    	if (!sourceWorld.isSubWorld())
    		return this.transformToLocal(otherX, otherY, otherZ);
    	else if (sourceWorld == this.holderWorld)
    		return this.holderWorld.getWorldVec3Pool().getVecFromPool(otherX, otherY, otherZ);
    	
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{otherX, otherY, otherZ, 1});
    	this.transformOtherToLocal(sourceWorld, tempVector, tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors)
    {
    	if (sourceWorld == null)
    		sourceWorld = this.holderWorld.getParentWorld();
    	
    	if (sourceWorld == this.holderWorld)
    		return otherVectors.dup();
    	
    	DoubleMatrix tempVectors = otherVectors;
    	if (sourceWorld.isSubWorld())
    		tempVectors = sourceWorld.transformToGlobal(tempVectors);
    	return this.transformToLocal(tempVectors, tempVectors);
    }
    
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result)
    {
    	if (sourceWorld == null)
    		sourceWorld = this.holderWorld.getParentWorld();
    	
    	if (sourceWorld == this.holderWorld)
    		return result.copy(otherVectors);
    	
    	if (sourceWorld.isSubWorld())
    		otherVectors = sourceWorld.transformToGlobal(otherVectors, result);
    	return this.transformToLocal(otherVectors, result);
    }
    
    ////////////////////////
    // Rotation
    ////////////////////////
    
    public Vec3 rotateToGlobal(Vec3 localVec)
    {
    	return this.rotateToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }
    
    public Vec3 rotateToGlobal(double localX, double localY, double localZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{localX, localY, localZ, 1});
    	tempVector = rotateToGlobal(tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors)
    {
    	return this.getRotationMatrix().mmul(localVectors);
    }
    
    public Vec3 rotateToLocal(Vec3 globalVec)
    {
    	return this.rotateToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }
    
    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{globalX, globalY, globalZ, 1});
    	tempVector = rotateToLocal(tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors)
    {
    	return this.getRotationInverseMatrix().mmul(globalVectors);
    }
    
    
    
    
    
    public Vec3 rotateYawToGlobal(Vec3 localVec)
    {
    	return this.rotateYawToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }
    
    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{localX, localY, localZ, 1});
    	tempVector = rotateYawToGlobal(tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix rotateYawToGlobal(DoubleMatrix localVectors)
    {
    	return this.matrixRotationYaw.mmul(localVectors);
    }
    
    public Vec3 rotateYawToLocal(Vec3 globalVec)
    {
    	return this.rotateYawToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }
    
    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ)
    {
    	DoubleMatrix tempVector = new DoubleMatrix(new double[]{globalX, globalY, globalZ, 1});
    	tempVector = rotateYawToLocal(tempVector);
    	return this.holderWorld.getWorldVec3Pool().getVecFromPool(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }
    
    public DoubleMatrix rotateYawToLocal(DoubleMatrix globalVectors)
    {
    	return this.matrixRotationYawInverse.mmul(globalVectors);
    }
    
    ////////////////////////////////
    ////////////////////////////////
}

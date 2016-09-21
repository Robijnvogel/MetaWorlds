package metaworlds.patcher;

import org.jblas.DoubleMatrix;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.World;

public class OrientedBB extends AxisAlignedBB {
	/** ThreadLocal AABBPool */
    private static final ThreadLocal theOBBLocalPool = new OBBLocalPool();
    
    //public static Vec3Pool globalVec3Pool = Vec3.fakePool;
    
	//public Vec3 vertices[] = new Vec3[8];
    public DoubleMatrix vertices = new DoubleMatrix(4, 8);
	public Vec3 dimensions = Vec3.createVectorHelper(0, 0, 0);//null;
	
	public World lastTransformedBy = null;
	
	OrientedBB(double par1, double par3, double par5, double par7, double par9, double par11)
	{
		super(par1, par3, par5, par7, par9, par11);
		
		this.setVerticesAndDimensions(this);
	}
	
	/**
     * Sets the bounds of the bounding box. Args: minX, minY, minZ, maxX, maxY, maxZ
     */
	@Override public AxisAlignedBB setBounds(double par1, double par3, double par5, double par7, double par9, double par11)
    {
		super.setBounds(par1, par3, par5, par7, par9, par11);

		this.setVerticesAndDimensions(this);

        return this;
    }
	
	/**
     * Returns a copy of the bounding box.
     */
    @Override public AxisAlignedBB copy()
    {
    	OrientedBB newOBB = getOBBPool().getOBB(this);
    	
    	newOBB.vertices.copy(this.vertices);
    	
    	newOBB.dimensions.setComponents(this.dimensions.xCoord, this.dimensions.yCoord, this.dimensions.zCoord);
        
    	return (AxisAlignedBB)newOBB;
    }
    
    /**
     * Sets the bounding box to the same bounds as the bounding box passed in. Args: axisAlignedBB
     */
    @Override public void setBB(AxisAlignedBB par1AxisAlignedBB)
    {
    	super.setBB(par1AxisAlignedBB);
    	
    	if (par1AxisAlignedBB instanceof OrientedBB)
    	{
    		this.vertices.copy(((OrientedBB)par1AxisAlignedBB).vertices);
    		
    		this.dimensions.setComponents(((OrientedBB)par1AxisAlignedBB).dimensions.xCoord, ((OrientedBB)par1AxisAlignedBB).dimensions.yCoord, ((OrientedBB)par1AxisAlignedBB).dimensions.zCoord);
    	}
    	else
    	{
    		this.setVerticesAndDimensions(par1AxisAlignedBB);
    	}
    }
    
    @Override public OrientedBB rotateYaw(double targetYaw)
    {
    	if (targetYaw == 0.0d)
    		return this;
    	
    	double cosYaw = Math.cos(targetYaw * Math.PI / 180.0d);
    	double sinYaw = Math.sin(targetYaw * Math.PI / 180.0d);
        
        double centerX = (this.minX + this.maxX) * 0.5d;
        double centerZ = (this.minZ + this.maxZ) * 0.5d;
        DoubleMatrix centerMatrix = DoubleMatrix.eye(4);
        DoubleMatrix rotationMatrix = DoubleMatrix.eye(4);
        DoubleMatrix centerMatrixInverse = DoubleMatrix.eye(4);
        centerMatrix.data[12] = centerX;
        centerMatrix.data[14] = centerZ;
        centerMatrixInverse.data[12] = -centerX;
        centerMatrixInverse.data[14] = -centerZ;
        rotationMatrix.data[0] = cosYaw;
        rotationMatrix.data[8] = sinYaw;
        rotationMatrix.data[2] = -sinYaw;
        rotationMatrix.data[10] = cosYaw;
        
        DoubleMatrix fullTransformMatrix = centerMatrix.mmuli(rotationMatrix.muli(centerMatrixInverse));
        fullTransformMatrix.mmuli(this.vertices, this.vertices);
        
    	for(int i = 0; i < 8; ++i)
    	{
    		if(i == 0)
    		{
    			this.minX = this.getX(i);
    			this.maxX = this.getX(i);
    			this.minZ = this.getZ(i);
    			this.maxZ = this.getZ(i);
    		}
    		else
    		{
    			this.minX = Math.min(this.minX, this.getX(i));
    			this.maxX = Math.max(this.maxX, this.getX(i));
    			this.minZ = Math.min(this.minZ, this.getZ(i));
    			this.maxZ = Math.max(this.maxZ, this.getZ(i));
    		}
    	}
        
    	return this;
    }
	
	public int getCWNeighbourIndexXZ(int prevIndex)
	{
		switch(prevIndex % 4)
		{
			case 0:
				return 2 + (prevIndex > 4 ? 4 : 0);
			case 1:
				return 0 + (prevIndex > 4 ? 4 : 0);
			case 2:
				return 3 + (prevIndex > 4 ? 4 : 0);
			case 3:
				return 1 + (prevIndex > 4 ? 4 : 0);
		}
		
		return 0;
	}
	
	public int getCCWNeighbourIndexXZ(int prevIndex)
	{
		switch(prevIndex % 4)
		{
			case 0:
				return 1 + (prevIndex > 4 ? 4 : 0);
			case 1:
				return 3 + (prevIndex > 4 ? 4 : 0);
			case 2:
				return 0 + (prevIndex > 4 ? 4 : 0);
			case 3:
				return 2 + (prevIndex > 4 ? 4 : 0);
		}
		
		return 0;
	}
	
	public double getX(int index)
	{
		return this.vertices.data[index * 4];
	}
	
	public double getY(int index)
	{
		return this.vertices.data[index * 4 + 1];
	}
	
	public double getZ(int index)
	{
		return this.vertices.data[index * 4 + 2];
	}
	
	/*OrientedBB(AxisAlignedBB sourceBB)
	{
		this.vertices[0] = Vec3.createVectorHelper(sourceBB.minX, sourceBB.minY, sourceBB.minZ);
		this.vertices[1] = Vec3.createVectorHelper(sourceBB.minX, sourceBB.minY, sourceBB.maxZ);
		this.vertices[2] = Vec3.createVectorHelper(sourceBB.minX, sourceBB.maxY, sourceBB.minZ);
		this.vertices[3] = Vec3.createVectorHelper(sourceBB.minX, sourceBB.maxY, sourceBB.maxZ);
		this.vertices[4] = Vec3.createVectorHelper(sourceBB.maxX, sourceBB.minY, sourceBB.minZ);
		this.vertices[5] = Vec3.createVectorHelper(sourceBB.maxX, sourceBB.minY, sourceBB.maxZ);
		this.vertices[6] = Vec3.createVectorHelper(sourceBB.maxX, sourceBB.maxY, sourceBB.minZ);
		this.vertices[7] = Vec3.createVectorHelper(sourceBB.maxX, sourceBB.maxY, sourceBB.maxZ);
		
		this.dimensions = Vec3.createVectorHelper(sourceBB.maxX - sourceBB.minX, sourceBB.maxY - sourceBB.minY, sourceBB.maxZ - sourceBB.minZ);
		
		this.minX = sourceBB.minX;
		this.maxX = sourceBB.maxX;
		this.minY = sourceBB.minY;
		this.maxY = sourceBB.maxY;
		this.minZ = sourceBB.minZ;
		this.maxZ = sourceBB.maxZ;
	}*/
	
	public void fromAABB(AxisAlignedBB sourceBB)
	{
		this.setVerticesAndDimensions(sourceBB);
		
		this.minX = sourceBB.minX;
		this.maxX = sourceBB.maxX;
		this.minY = sourceBB.minY;
		this.maxY = sourceBB.maxY;
		this.minZ = sourceBB.minZ;
		this.maxZ = sourceBB.maxZ;
	}
	
	public void setVerticesAndDimensions(AxisAlignedBB sourceBB)
	{
		this.vertices.data[0] = sourceBB.minX;
		this.vertices.data[1] = sourceBB.minY;
		this.vertices.data[2] = sourceBB.minZ;
		this.vertices.data[3] = 1;
		//this.vertices[0].setComponents(sourceBB.minX, sourceBB.minY, sourceBB.minZ);
		this.vertices.data[4] = sourceBB.minX;
		this.vertices.data[5] = sourceBB.minY;
		this.vertices.data[6] = sourceBB.maxZ;
		this.vertices.data[7] = 1;
		//this.vertices[1].setComponents(sourceBB.minX, sourceBB.minY, sourceBB.maxZ);
		this.vertices.data[8] = sourceBB.maxX;
		this.vertices.data[9] = sourceBB.minY;
		this.vertices.data[10] = sourceBB.minZ;
		this.vertices.data[11] = 1;
		//this.vertices[2].setComponents(sourceBB.maxX, sourceBB.minY, sourceBB.minZ);
		this.vertices.data[12] = sourceBB.maxX;
		this.vertices.data[13] = sourceBB.minY;
		this.vertices.data[14] = sourceBB.maxZ;
		this.vertices.data[15] = 1;
		//this.vertices[3].setComponents(sourceBB.maxX, sourceBB.minY, sourceBB.maxZ);
		this.vertices.data[16] = sourceBB.minX;
		this.vertices.data[17] = sourceBB.maxY;
		this.vertices.data[18] = sourceBB.minZ;
		this.vertices.data[19] = 1;
		//this.vertices[4].setComponents(sourceBB.minX, sourceBB.maxY, sourceBB.minZ);
		this.vertices.data[20] = sourceBB.minX;
		this.vertices.data[21] = sourceBB.maxY;
		this.vertices.data[22] = sourceBB.maxZ;
		this.vertices.data[23] = 1;
		//this.vertices[5].setComponents(sourceBB.minX, sourceBB.maxY, sourceBB.maxZ);
		this.vertices.data[24] = sourceBB.maxX;
		this.vertices.data[25] = sourceBB.maxY;
		this.vertices.data[26] = sourceBB.minZ;
		this.vertices.data[27] = 1;
		//this.vertices[6].setComponents(sourceBB.maxX, sourceBB.maxY, sourceBB.minZ);
		this.vertices.data[28] = sourceBB.maxX;
		this.vertices.data[29] = sourceBB.maxY;
		this.vertices.data[30] = sourceBB.maxZ;
		this.vertices.data[31] = 1;
		//this.vertices[7].setComponents(sourceBB.maxX, sourceBB.maxY, sourceBB.maxZ);
		
		this.dimensions.setComponents(sourceBB.maxX - sourceBB.minX, sourceBB.maxY - sourceBB.minY, sourceBB.maxZ - sourceBB.minZ);
	}
	
	public void recalcAABB()
	{
		this.minX = this.vertices.data[0];
		this.minY = this.vertices.data[1];
		this.minZ = this.vertices.data[2];
		
		this.maxX = this.vertices.data[0];
		this.maxY = this.vertices.data[1];
		this.maxZ = this.vertices.data[2];
		
		for (int i = 4; i < 32; i += 4)
		{
			if (this.vertices.data[i] < this.minX)
				this.minX = this.vertices.data[i];
			else if (this.vertices.data[i] > this.maxX)
				this.maxX = this.vertices.data[i];
			
			if (this.vertices.data[i+1] < this.minY)
				this.minY = this.vertices.data[i+1];
			else if (this.vertices.data[i+1] > this.maxY)
				this.maxY = this.vertices.data[i+1];
			
			if (this.vertices.data[i+2] < this.minZ)
				this.minZ = this.vertices.data[i+2];
			else if (this.vertices.data[i+2] > this.maxZ)
				this.maxZ = this.vertices.data[i+2];
		}
		
		/*DoubleMatrix minValues = this.vertices.rowMins();
		DoubleMatrix maxValues = this.vertices.rowMaxs();
		this.minX = minValues.data[0];
		this.minY = minValues.data[1];
		this.minZ = minValues.data[2];
		
		this.maxX = maxValues.data[0];
		this.maxY = maxValues.data[1];
		this.maxZ = maxValues.data[2];*/
	}
	
	/**
     * Gets the ThreadLocal OBBPool
     */
    public static OBBPool getOBBPool()
    {
        return (OBBPool)theOBBLocalPool.get();
    }
    
    public OrientedBB transformBoundingBoxToGlobal(World transformerWorld)
    {
    	transformerWorld.transformToGlobal(this.vertices, this.vertices);
    	this.dimensions.setComponents(this.dimensions.xCoord * transformerWorld.getScaling(), this.dimensions.yCoord * transformerWorld.getScaling(), this.dimensions.zCoord * transformerWorld.getScaling());
    	
    	this.recalcAABB();
    	
    	this.lastTransformedBy = transformerWorld;
    	
    	return this;
    }
    
    public OrientedBB transformBoundingBoxToLocal(World transformerWorld)
    {
    	transformerWorld.transformToLocal(this.vertices, this.vertices);
    	this.dimensions.setComponents(this.dimensions.xCoord / transformerWorld.getScaling(), this.dimensions.yCoord / transformerWorld.getScaling(), this.dimensions.zCoord / transformerWorld.getScaling());
    	
    	this.recalcAABB();
    	
    	this.lastTransformedBy = transformerWorld;
    	
    	return this;
    }
    
    @Override public OrientedBB offset(double par1, double par3, double par5)
    {
    	super.offset(par1, par3, par5);
    	
    	for(int i = 0; i < 8; ++i)
    	{
    		vertices.data[i * 4] += par1;
    		vertices.data[i * 4 + 1] += par3;
    		vertices.data[i * 4 + 2] += par5;
    	}
        
        return this;
    }
    
    @Override public OrientedBB getOrientedBB()
    {
    	return this;
    }
    
    /**
     * Returns whether the given bounding box intersects with this one. Args: axisAlignedBB
     */
    public boolean intersectsWith(AxisAlignedBB par1AxisAlignedBB)
    {
    	if(par1AxisAlignedBB instanceof OrientedBB)
    		return this.intersectsWithOBB((OrientedBB)par1AxisAlignedBB);
    	
    	//////////////////////////////
    	//Check Y and check XZ direction 1 of 2 (OBB outside AABB)
    	//////////////////////////////
    	if(!super.intersectsWith(par1AxisAlignedBB))
    		return false;
    	
		//////////////////////////////
		//Check XZ direction 2 of 2 (AABB outside OBB)
		//////////////////////////////
    	if(!checkIntersectsWithXZdirection2(par1AxisAlignedBB))
    		return false;

    	//////////////////////////////
    	return true;
    }
    
    private boolean checkIntersectsWithXZdirection2(AxisAlignedBB par1AxisAlignedBB)
    {
		//////////////////////////////
		//Check XZ direction 2 of 2 (AABB outside OBB)
		//////////////////////////////
		// p1 = minX, minZ
		// p2 = minX, maxZ
		// p3 = maxX, minZ
		// p4 = maxX, maxZ
		
		// vertexX = vertices[2] - vertices[0]
		// vertexZ = vertices[1] - vertices[0]
		
		// vertex_x = vertexX.x, vertex_z = vertexX.z
		double vertex_x = this.getX(2) - this.getX(0);
		double vertex_z = this.getZ(2) - this.getZ(0);
		// p1
		double dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.minZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.xCoord;
		double curMinProjCoord = dotProduct;
		double curMaxProjCoord = dotProduct;
		
		// p2
		dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.maxZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.xCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		// p3
		dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.minZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.xCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		// p4
		dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.maxZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.xCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		if(!(curMaxProjCoord > 0 && curMinProjCoord < dimensions.xCoord))
			return false;
		
		
		
		// vertex_x = vertexZ.x, vertex_z = vertexZ.z
		vertex_x = this.getX(1) - this.getX(0);
		vertex_z = this.getZ(1) - this.getZ(0);
		// p1
		dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.minZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.zCoord;
		curMinProjCoord = dotProduct;
		curMaxProjCoord = dotProduct;
		
		// p2
		dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.maxZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.zCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		// p3
		dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.minZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.zCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		// p4
		dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * (vertex_x) + (par1AxisAlignedBB.maxZ - this.getZ(0)) * (vertex_z);
		dotProduct /= dimensions.zCoord;
		curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
		curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
		
		if(!(curMaxProjCoord > 0 && curMinProjCoord < dimensions.zCoord))
			return false;
		
		//////////////////////////////
		return true;
    }
    
    public boolean intersectsWithOBB(OrientedBB par1OrientedBB)
    {
		//////////////////////////////
		//Check Y
		//////////////////////////////
    	if (!(par1OrientedBB.maxY > this.minY && par1OrientedBB.minY < this.maxY))
    		return false;
		
		//////////////////////////////
		//Check XZ direction 1 of 2 (OBB1 outside OBB2)
		//////////////////////////////
    	if(!checkIntersectsWithXZ_OBB_oneDirection(par1OrientedBB))
			return false;
		
		//////////////////////////////
		//Check XZ direction 2 of 2 (OBB2 outside OBB1)
		//////////////////////////////
		if(!par1OrientedBB.checkIntersectsWithXZ_OBB_oneDirection(this))
			return false;
		
		//////////////////////////////
		return true;
    }
    
    public boolean checkIntersectsWithXZ_OBB_oneDirection(OrientedBB par1OrientedBB)
    {
		//////////////////////////////
		//Check XZ one direction
		//////////////////////////////
		// p1 = minX, minZ
		// p2 = minX, maxZ
		// p3 = maxX, minZ
		// p4 = maxX, maxZ
		
		// vertexX = vertices[2] - vertices[0]
		// vertexZ = vertices[1] - vertices[0]
		
		// vertex_x = vertexX.x, vertex_z = vertexX.z
		double vertex_x = this.getX(2) - this.getX(0);
		double vertex_z = this.getZ(2) - this.getZ(0);
		
		double curMinProjCoord = 0.0d;
		double curMaxProjCoord = 0.0d;
		
		// p1-4
		for (int i = 0; i < 4; ++i)
		{
			double dotProduct = (par1OrientedBB.getX(i) - this.getX(0)) * (vertex_x) + (par1OrientedBB.getZ(i) - this.getZ(0)) * (vertex_z);
			dotProduct /= dimensions.xCoord;
			
			if (i == 0)
			{
				curMinProjCoord = dotProduct;
				curMaxProjCoord = dotProduct;
			}
			else
			{
				curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
				curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
			}
		}
		
		if(!(curMaxProjCoord > 0 && curMinProjCoord < dimensions.xCoord))
			return false;
		
		
		
		// vertex_x = vertexZ.x, vertex_z = vertexZ.z
		vertex_x = this.getX(1) - this.getX(0);
		vertex_z = this.getZ(1) - this.getZ(0);
		
		// p1-4
		for (int i = 0; i < 4; ++i)
		{
			double dotProduct = (par1OrientedBB.getX(i) - this.getX(0)) * (vertex_x) + (par1OrientedBB.getZ(i) - this.getZ(0)) * (vertex_z);
			dotProduct /= dimensions.zCoord;
			
			if (i == 0)
			{
				curMinProjCoord = dotProduct;
				curMaxProjCoord = dotProduct;
			}
			else
			{
				curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
				curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
			}
		}
		
		if(!(curMaxProjCoord > 0 && curMinProjCoord < dimensions.zCoord))
			return false;
		
		//////////////////////////////
		return true;
    }
    
    public boolean isVecInside(Vec3 par1Vec3)
    {
    	return super.isVecInside(par1Vec3);
    	//TODO
    }
    
    public MovingObjectPosition calculateIntercept(Vec3 par1Vec3, Vec3 par2Vec3, World par3World)
    {
    	return super.calculateIntercept(par1Vec3, par2Vec3, par3World);
    	//TODO
    }
    
    public double calculateXOffset(AxisAlignedBB par1AxisAlignedBB, double par2)
    {
        if (par1AxisAlignedBB.maxY > this.minY && par1AxisAlignedBB.minY < this.maxY)
        {
            if (par1AxisAlignedBB.maxZ > this.minZ && par1AxisAlignedBB.minZ < this.maxZ)
            {
                double var4;
                
                if (par2 > 0.0D)
                {
                	double curMinX = this.getX(0);
                	int curMinIndex = 0;
                	
                	for(int i = 1; i < 4; ++i)
                	{
                		if (this.getX(i) < curMinX)
                		{
                			curMinX = this.getX(i);
                			curMinIndex = i;
                		}
                	}
                	
                	if (this.getZ(curMinIndex) <= par1AxisAlignedBB.minZ)
                	{
                		int neighbourIndex = this.getCCWNeighbourIndexXZ(curMinIndex);
            			
            			if(this.getZ(neighbourIndex) != this.getZ(curMinIndex))
            			{
            				curMinX = this.getX(neighbourIndex) + (this.getX(curMinIndex) - this.getX(neighbourIndex)) * 
            						(par1AxisAlignedBB.minZ - this.getZ(neighbourIndex)) / (this.getZ(curMinIndex) - this.getZ(neighbourIndex));
            			} 
                	}
            		else if (this.getZ(curMinIndex) >= par1AxisAlignedBB.maxZ)
                	{
            			int neighbourIndex = this.getCWNeighbourIndexXZ(curMinIndex);
                			
            			if(this.getZ(neighbourIndex) != this.getZ(curMinIndex))
            			{
            				curMinX = this.getX(neighbourIndex) + (this.getX(curMinIndex) - this.getX(neighbourIndex)) * 
            						(par1AxisAlignedBB.maxZ - this.getZ(neighbourIndex)) / (this.getZ(curMinIndex) - this.getZ(neighbourIndex));
            			}
                	}
                	
                	if (par1AxisAlignedBB.maxX <= (curMinX + 0.75d))
        			{
        				var4 = curMinX - par1AxisAlignedBB.maxX - 0.01d;

                        if (var4 < par2)
                        {
                            par2 = var4;
                        }
        			}
                }
                
                if (par2 < 0.0D)
                {
                	double curMaxX = this.getX(0);
                	int curMaxIndex = 0;
                	
                	for(int i = 1; i < 4; ++i)
                	{
                		if (this.getX(i) > curMaxX)
                		{
                			curMaxX = this.getX(i);
                			curMaxIndex = i;
                		}
                	}
                	
                	if (this.getZ(curMaxIndex) <= par1AxisAlignedBB.minZ)
                	{
                		int neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
            			
            			if(this.getZ(neighbourIndex) != this.getZ(curMaxIndex))
            			{
            				curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex)) * 
            						(par1AxisAlignedBB.minZ - this.getZ(neighbourIndex)) / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
            			} 
                	}
            		else if (this.getZ(curMaxIndex) >= par1AxisAlignedBB.maxZ)
                	{
            			int neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                			
            			if(this.getZ(neighbourIndex) != this.getZ(curMaxIndex))
            			{
            				curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex)) * 
            						(par1AxisAlignedBB.maxZ - this.getZ(neighbourIndex)) / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
            			}
                	}
                	
                	if (par1AxisAlignedBB.minX >= (curMaxX - 0.75d))
                    {
                        var4 = curMaxX - par1AxisAlignedBB.minX + 0.01d;

                        if (var4 > par2)
                        {
                            par2 = var4;
                        }
                    }
                }
            }
        }
        
        return par2;
    }
    
    public double calculateYOffset(AxisAlignedBB par1AxisAlignedBB, double par2)
    {
        if (par1AxisAlignedBB.maxX > this.minX && par1AxisAlignedBB.minX < this.maxX && 
        		par1AxisAlignedBB.maxZ > this.minZ && par1AxisAlignedBB.minZ < this.maxZ && 
        		this.checkIntersectsWithXZdirection2(par1AxisAlignedBB))
        {
            double var4;

            if (par2 > 0.0D && par1AxisAlignedBB.maxY <= (this.minY + 0.75d))
            {
                var4 = this.minY - par1AxisAlignedBB.maxY;

                if (var4 < par2)
                {
                    par2 = var4;// - 0.01d;
                }
            }

            if (par2 < 0.0D && par1AxisAlignedBB.minY >= (this.maxY - 0.75d))
            {
                var4 = this.maxY - par1AxisAlignedBB.minY;

                if (var4 > par2)
                {
                    par2 = var4;// + 0.01d;
                }
            }

            return par2;
        }
        else
        {
            return par2;
        }
    }
    
    public double calculateZOffset(AxisAlignedBB par1AxisAlignedBB, double par2)
    {
        if (par1AxisAlignedBB.maxY > this.minY && par1AxisAlignedBB.minY < this.maxY)
        {
            if (par1AxisAlignedBB.maxX > this.minX && par1AxisAlignedBB.minX < this.maxX)
            {
                double var4;
                
                if (par2 > 0.0D)
                {
                	double curMinZ = this.getZ(0);
                	int curMinIndex = 0;
                	
                	for(int i = 1; i < 4; ++i)
                	{
                		if (this.getZ(i) < curMinZ)
                		{
                			curMinZ = this.getZ(i);
                			curMinIndex = i;
                		}
                	}
                	
                	if (this.getX(curMinIndex) <= par1AxisAlignedBB.minX)
                	{
                		int neighbourIndex = this.getCWNeighbourIndexXZ(curMinIndex);
            			
            			if(this.getX(neighbourIndex) != this.getX(curMinIndex))
            			{
            				curMinZ = this.getZ(neighbourIndex) + (this.getZ(curMinIndex) - this.getZ(neighbourIndex)) * 
            						(par1AxisAlignedBB.minX - this.getX(neighbourIndex)) / (this.getX(curMinIndex) - this.getX(neighbourIndex));
            			} 
                	}
            		else if (this.getX(curMinIndex) >= par1AxisAlignedBB.maxX)
                	{
            			int neighbourIndex = this.getCCWNeighbourIndexXZ(curMinIndex);
                			
            			if(this.getX(neighbourIndex) != this.getX(curMinIndex))
            			{
            				curMinZ = this.getZ(neighbourIndex) + (this.getZ(curMinIndex) - this.getZ(neighbourIndex)) * 
            						(par1AxisAlignedBB.maxX - this.getX(neighbourIndex)) / (this.getX(curMinIndex) - this.getX(neighbourIndex));
            			}
                	}
                	
                	if (par1AxisAlignedBB.maxZ <= (curMinZ + 0.75d))
        			{
        				var4 = curMinZ - par1AxisAlignedBB.maxZ - 0.01d;

                        if (var4 < par2)
                        {
                            par2 = var4;
                        }
        			}
                }
                
                if (par2 < 0.0D)
                {
                	double curMaxZ = this.getZ(0);
                	int curMaxIndex = 0;
                	
                	for(int i = 1; i < 4; ++i)
                	{
                		if (this.getZ(i) > curMaxZ)
                		{
                			curMaxZ = this.getZ(i);
                			curMaxIndex = i;
                		}
                	}
                	
                	if (this.getX(curMaxIndex) <= par1AxisAlignedBB.minX)
                	{
                		int neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
            			
            			if(this.getX(neighbourIndex) != this.getX(curMaxIndex))
            			{
            				curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex)) * 
            						(par1AxisAlignedBB.minX - this.getX(neighbourIndex)) / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
            			} 
                	}
            		else if (this.getX(curMaxIndex) >= par1AxisAlignedBB.maxX)
                	{
            			int neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                			
            			if(this.getX(neighbourIndex) != this.getX(curMaxIndex))
            			{
            				curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex)) * 
            						(par1AxisAlignedBB.maxX - this.getX(neighbourIndex)) / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
            			}
                	}
                	
                	if (par1AxisAlignedBB.minZ >= (curMaxZ - 0.75))
                    {
                        var4 = curMaxZ - par1AxisAlignedBB.minZ + 0.01d;

                        if (var4 > par2)
                        {
                            par2 = var4;
                        }
                    }
                }
            }
        }
        
        return par2;
    }
}

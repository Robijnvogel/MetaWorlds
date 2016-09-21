package metaworlds.patcher;

import metaworlds.api.SubWorld;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class AxisAlignedBB_BaseSubWorld {
	public AxisAlignedBB getTransformedToGlobalBoundingBox(World transformerWorld)
    {
    	if (transformerWorld instanceof SubWorld)
    		return OrientedBB.getOBBPool().getOBB((AxisAlignedBB)this).transformBoundingBoxToGlobal(transformerWorld);
    	else 
    		return (AxisAlignedBB)this;
    }
    
    public AxisAlignedBB getTransformedToLocalBoundingBox(World transformerWorld)
    {
    	if (transformerWorld instanceof SubWorld)
    		return OrientedBB.getOBBPool().getOBB((AxisAlignedBB)this).transformBoundingBoxToLocal(transformerWorld);
    	else
    		return (AxisAlignedBB)this;
    }
    
    public OrientedBB rotateYaw(double targetYaw)
    {
    	return OrientedBB.getOBBPool().getOBB((AxisAlignedBB)this).rotateYaw(targetYaw);
    }
    
    public OrientedBB getOrientedBB()
    {
    	return OrientedBB.getOBBPool().getOBB((AxisAlignedBB)this);
    }
}

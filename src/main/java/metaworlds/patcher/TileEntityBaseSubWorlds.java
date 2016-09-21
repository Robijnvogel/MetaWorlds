package metaworlds.patcher;

import metaworlds.api.SubWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public abstract class TileEntityBaseSubWorlds
{
	public double getDistanceFromGlobal(double par1, double par3, double par5)
    {
		TileEntity tThis = ((TileEntity)this);
		
		if (!tThis.hasWorldObj() || !(tThis.getWorldObj() instanceof SubWorld))
			return tThis.getDistanceFrom(par1, par3, par5);
		else
		{
			Vec3 transformedPos = tThis.getWorldObj().transformToGlobal((double)tThis.xCoord + 0.5D, (double)tThis.yCoord + 0.5D, (double)tThis.zCoord + 0.5D);
			return transformedPos.squareDistanceTo(par1, par3, par5);
		}
        
    }
}

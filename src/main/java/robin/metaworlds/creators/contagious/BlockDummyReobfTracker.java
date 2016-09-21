package robin.metaworlds.creators.contagious;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockDummyReobfTracker extends Block {

	public static String canBlockStayMethodName = null;
	public static String onNeighborBlockChange = null;
	
	public BlockDummyReobfTracker(Material par2Material) {
		super(par2Material);
	}
	
	public void initialize()
	{
		canBlockStay(null, 0, 0, 0);
		onNeighborBlockChange(null, 0, 0, 0, null);
	}

	@Override
	public boolean canBlockStay(World par1World, int par2, int par3, int par4)
    {
        canBlockStayMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();		
        return true;
    }
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5)
	{
            onNeighborBlockChange = Thread.currentThread().getStackTrace()[1].getMethodName();
	}
}

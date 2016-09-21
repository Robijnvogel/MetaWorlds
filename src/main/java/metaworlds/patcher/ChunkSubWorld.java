package metaworlds.patcher;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkSubWorld extends Chunk 
{
	//Limits
	//interpretation: e.g.
	//collisionLimitZPosLineY = how far can I go from positive to negative Z direction until my collision-line (at the specified y-position) intersects with a block?
	//limits by Plane
	public short collisionLimitXPosPlane;
	public short collisionLimitXNegPlane;
	public short collisionLimitYPosPlane;
	public short collisionLimitYNegPlane;
	public short collisionLimitZPosPlane;
	public short collisionLimitZNegPlane;
	//limits by Line
	public byte[] collisionLimitXPosLineY;//16
	public byte[] collisionLimitXPosLineZ;//16
	public byte[] collisionLimitXNegLineY;
	public byte[] collisionLimitXNegLineZ;
	public byte[] collisionLimitYPosLineX;
	public byte[] collisionLimitYPosLineZ;
	public byte[] collisionLimitYNegLineX;
	public byte[] collisionLimitYNegLineZ;
	public byte[] collisionLimitZPosLineX;
	public byte[] collisionLimitZPosLineY;
	public byte[] collisionLimitZNegLineX;
	public byte[] collisionLimitZNegLineY;
	//limits by Point
	//Like height maps but 
	public byte[] collisionLimitsMapXPos;//256*16
	public byte[] collisionLimitsMapXNeg;
	public byte[] collisionLimitsMapYPos;//16*16
	public byte[] collisionLimitsMapYNeg;
	public byte[] collisionLimitsMapZPos;//256*16
	public byte[] collisionLimitsMapZNeg;
	
	public boolean isEmpty = true;
	
	public ChunkSubWorld(World par1World, int par2, int par3)
    {
		super(par1World, par2, par3);
    }
	
	public ChunkSubWorld(World par1World, Block[] par2blocks, int par3, int par4)
    {
		super(par1World, par2blocks, par3, par4);
    }
	
	public ChunkSubWorld(World world, Block[] par2blocks, byte[] metadata, int chunkX, int chunkZ)
    {
		super(world, par2blocks, metadata, chunkX, chunkZ);
    }
	
	@Override public boolean needsSaving(boolean par1)
    {
		if (this.hasEntities || this.isModified)
			this.isEmpty = false;
		
		if (this.isEmpty)
			return false;
		
		return super.needsSaving(par1);
    }
	
	@Override
	public void populateChunk(IChunkProvider par1IChunkProvider, IChunkProvider par2IChunkProvider, int par3, int par4)
	{
	    super.populateChunk(par1IChunkProvider, par2IChunkProvider, par3, par4);
	    
	    this.isTerrainPopulated = true;//TODO: check me!
        this.isLightPopulated = true;//TODO: check me!
	}
}

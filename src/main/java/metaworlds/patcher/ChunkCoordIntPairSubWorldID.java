package metaworlds.patcher;

import net.minecraft.world.ChunkCoordIntPair;

public class ChunkCoordIntPairSubWorldID extends ChunkCoordIntPair implements Comparable
{
	public int chunkSubWorldID;
	
	public ChunkCoordIntPairSubWorldID(int posX, int posZ, int subWorldID)
	{
		super(posX, posZ);
		this.chunkSubWorldID = subWorldID;
	}
	
	@Override public boolean equals(Object par1Obj)
    {
		if(par1Obj instanceof ChunkCoordIntPairSubWorldID)
		{
			ChunkCoordIntPairSubWorldID var2 = (ChunkCoordIntPairSubWorldID)par1Obj;
        	return var2.chunkXPos == this.chunkXPos && var2.chunkZPos == this.chunkZPos && var2.chunkSubWorldID == this.chunkSubWorldID;
		}
		else
			return false;
    }
	
	@Override public int hashCode()
    {
        long var1 = chunkXZ2Int(this.chunkXPos, this.chunkZPos);
        int var3 = (int)var1;
        int var4 = (int)(var1 >> 32);
        return var3 ^ var4 + Integer.reverse(chunkSubWorldID);
    }
	
	@Override public String toString()
    {
        return "[" + this.chunkXPos + ", " + this.chunkZPos + "] SubWorldID [" + this.chunkSubWorldID + "]";
    }

	@Override
	public int compareTo(Object arg0) {
		ChunkCoordIntPairSubWorldID argCoord = (ChunkCoordIntPairSubWorldID)arg0;
		return argCoord.chunkSubWorldID != this.chunkSubWorldID ? (this.chunkSubWorldID - argCoord.chunkSubWorldID) : (argCoord.chunkXPos != this.chunkXPos ? (argCoord.chunkXPos - this.chunkXPos) : (argCoord.chunkZPos != this.chunkZPos ? (argCoord.chunkZPos - this.chunkZPos) : 0));
	}
}

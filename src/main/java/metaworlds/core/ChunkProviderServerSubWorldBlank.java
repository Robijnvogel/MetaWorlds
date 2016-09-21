package metaworlds.core;

import java.util.List;

import metaworlds.patcher.ChunkSubWorld;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

public class ChunkProviderServerSubWorldBlank extends ChunkProviderServer {
	public ChunkProviderServerSubWorldBlank(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader, IChunkProvider par3IChunkProvider)
	{
		super(par1WorldServer, par2IChunkLoader, par3IChunkProvider);
	}
	
	@Override public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
    {
	    
    }
	
	@Override public Chunk provideChunk(int par1, int par2)
    {
		return new ChunkSubWorld(this.getWorld(), par1, par2);
    }
	
	@Override public boolean unloadQueuedChunks()
	{
		return false;
	}
	
	/**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    @Override public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return null;
    }
}

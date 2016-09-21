package metaworlds.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

public class ChunkProviderServerSubWorld extends ChunkProviderServer {
	
	public ChunkProviderServerSubWorld(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader, IChunkProvider par3IChunkProvider)
	{
		super(par1WorldServer, par2IChunkLoader, par3IChunkProvider);
	}
	
	//Important: this fixes villages etc. trying to spawn on subworlds
	public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
	{
	    
	}
}

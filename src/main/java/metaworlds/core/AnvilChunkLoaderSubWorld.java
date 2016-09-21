package metaworlds.core;

import java.io.File;
import java.lang.reflect.Method;

import metaworlds.patcher.ChunkSubWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.ForgeChunkManager;

public class AnvilChunkLoaderSubWorld extends AnvilChunkLoader {
	
	/*static Class[] cArg = new Class[2];
	static Method readChunkFromNBTMethod;
	
	static {
		try
		{
	    	cArg[0] = AnvilChunkLoader.class;
	    	cArg[1] = NBTTagCompound.class;
	    	readChunkFromNBTMethod = AnvilChunkLoader.class.getDeclaredMethod("readChunkFromNBT", cArg);
	    	readChunkFromNBTMethod.setAccessible(true);
		}
		catch(NoSuchMethodException e)
		{
			System.out.println(e.toString());
		}
	}*/

	public AnvilChunkLoaderSubWorld(File par1File) {
		super(par1File);
	}
	
	@Override
	protected void writeChunkToNBT(Chunk par1Chunk, World par2World, NBTTagCompound par3NBTTagCompound)
	{
		super.writeChunkToNBT(par1Chunk, par2World, par3NBTTagCompound);
		
		//write subworld specific chunk data here
	}

	@Override
	protected Chunk readChunkFromNBT(World par1World, NBTTagCompound par2NBTTagCompound)
	{
		ChunkSubWorld subWorldChunk = null;
		
		try
    	{
			subWorldChunk = (ChunkSubWorld)super.readChunkFromNBT(par1World, par2NBTTagCompound);
			//subWorldChunk = (ChunkSubWorld)readChunkFromNBTMethod.invoke(this, par1World, par2NBTTagCompound);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
		
		//read subworld specific chunk data here
		
		return subWorldChunk;
	}
}

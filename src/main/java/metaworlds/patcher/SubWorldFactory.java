package metaworlds.patcher;

import net.minecraft.world.World;

public interface SubWorldFactory 
{
	public World CreateSubWorld(World parentWorld, int newSubWorldID);
}

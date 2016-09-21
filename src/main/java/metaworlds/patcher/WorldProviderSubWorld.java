package metaworlds.patcher;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class WorldProviderSubWorld extends WorldProvider
{
	public World m_parentWorld;
	
	WorldProviderSubWorld(World parentWorld)
	{
		this.m_parentWorld = parentWorld;
	}
	
    /**
     * Returns the dimension's name, e.g. "The End", "Nether", or "Overworld".
     */
    public String getDimensionName()
    {
        return "SubWorld";
    }
}

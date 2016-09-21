package metaworlds.core;

import metaworlds.core.client.SubWorldClient;
import metaworlds.patcher.RenderGlobalSubWorld;
import metaworlds.patcher.SubWorldFactory;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class SubWorldClientFactory implements SubWorldFactory 
{
	public World CreateSubWorld(World parentWorld, int newSubWorldID)
	{
		return new SubWorldClient((WorldClient)parentWorld, newSubWorldID, ((WorldClient)parentWorld).getSendQueue(), new WorldSettings(0L, parentWorld.getWorldInfo().getGameType(), false, parentWorld.getWorldInfo().isHardcoreModeEnabled(), parentWorld.getWorldInfo().getTerrainType()), 
				((WorldClient)parentWorld).worldDimension, parentWorld.difficultySetting, parentWorld.theProfiler);
	}
}

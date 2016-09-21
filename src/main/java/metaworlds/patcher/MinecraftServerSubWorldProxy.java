package metaworlds.patcher;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;

public class MinecraftServerSubWorldProxy extends MinecraftServer {
    MinecraftServer realServer;
    WorldServer targetSubWorld;

    public MinecraftServerSubWorldProxy(MinecraftServer original)
    {
        super(original);
        
        this.realServer = original;
    }
    
    public void setWorld(WorldServer newWorld)
    {
        this.targetSubWorld = newWorld;
        this.setConfigurationManager(new ServerConfigurationManagerSubWorldProxy(this, newWorld));
    }
    
    public MinecraftServer getRealServer()
    {
        return this.realServer;
    }
    
    @Override
    public ServerConfigurationManager getConfigurationManager()
    {
        if (this.targetSubWorld == null)//on init
            return this.realServer.getConfigurationManager();
        
        return super.getConfigurationManager();
    }
    
    @Override()
    public boolean getCanSpawnAnimals()
    {
        return this.realServer.getCanSpawnAnimals();
    }

    @Override
    protected boolean startServer() throws IOException
    {
        return false;
    }

    @Override
    public boolean canStructuresSpawn()
    {
        return false;
    }

    @Override
    public GameType getGameType()
    {
        return this.realServer.getGameType();
    }

    @Override
    public EnumDifficulty func_147135_j()
    {
        return this.realServer.func_147135_j();
    }

    @Override
    public boolean isHardcore()
    {
        return this.realServer.isHardcore();
    }

    @Override
    public int getOpPermissionLevel()
    {
        return this.realServer.getOpPermissionLevel();
    }

    @Override
    public boolean isDedicatedServer()
    {
        return this.realServer.isDedicatedServer();
    }

    @Override
    public boolean isCommandBlockEnabled()
    {
        return this.realServer.isCommandBlockEnabled();
    }

    @Override
    public String shareToLAN(GameType var1, boolean var2)
    {
        return null;
    }

}

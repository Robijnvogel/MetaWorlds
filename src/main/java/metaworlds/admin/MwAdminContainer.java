package metaworlds.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import cpw.mods.fml.client.FMLClientHandler;
import metaworlds.api.SubWorld;
import metaworlds.core.MetaworldsMod;
import metaworlds.patcher.SubWorldInfoHolder;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class MwAdminContainer extends Container {
    protected EntityPlayerMP player;
    
    protected List<SaveGameInfo> saveList;//List of savegames
    
    protected Map<Integer, AdminSubWorldInfo> adminSubWorldInfos;
    
    public static List<SubWorldImporterThread> importThreads = new ArrayList<SubWorldImporterThread>();
    
    public MwAdminContainer(EntityPlayer playerPar)
    {
        this.player = (EntityPlayerMP)playerPar;
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return true;
    }
    
    public void loadAndSendSaves()
    {
        //List<String> savefileNames = new ArrayList<String>();
        
        this.saveList = new ArrayList<SaveGameInfo>();
        
        //Determine all saves
        if (player.mcServer instanceof IntegratedServer)
        {
            List anvilSaveList = null;
            try
            {
                anvilSaveList = player.mcServer.getActiveAnvilConverter().getSaveList();
            }
            catch (AnvilConverterException e)
            {
                e.printStackTrace();
            }
            
            for (Object curSaveObj : anvilSaveList)
            {
                SaveFormatComparator curSave = (SaveFormatComparator)curSaveObj;
                
                File curSaveDir = new File(FMLClientHandler.instance().getSavesDir(), curSave.getFileName());
                
                this.saveList.add(new SaveGameInfo(curSave.getFileName(), curSaveDir));
                //savefileNames.add(curSave.getFileName());
            }
        }
        else
        {
            String worldFileName = DimensionManager.getWorld(0).getSaveHandler().getWorldDirectoryName();
            this.saveList.add(new SaveGameInfo(worldFileName, DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory()));
            //savefileNames.add(worldFileName);
        }
        
        //Determine all subworlds of each save
        for (SaveGameInfo curSaveInfo : this.saveList)
        {
            File[] subFiles = curSaveInfo.saveDir.listFiles();
            
            curSaveInfo.subWorldsList.add(new SaveGameSubWorldInfo("Main world", ".", 0));
            
            for (File curFile : subFiles)
            {
                if (!curFile.isDirectory())
                    continue;
                
                String curFileName = curFile.getName();
                if (curFileName.matches("^SUBWORLD\\d+$"))
                {
                    int curSubWorldId = Integer.parseInt(curFileName.substring(8));
                    curSaveInfo.subWorldsList.add(new SaveGameSubWorldInfo("SubWorld " + curSubWorldId, curFileName, curSubWorldId));
                }
            }
        }
        
        MetaworldsMod.instance.networkHandler.sendTo(new MwAdminGuiInitPacket(this.saveList), this.player);
    }
    
    public void sendSubWorldInfos()
    {
        Collection<SubWorldInfoHolder> subWorldInfos = DimensionManager.getWorld(0).getWorldInfo().getSubWorldInfos();
        
        
        adminSubWorldInfos = new TreeMap<Integer, AdminSubWorldInfo>();
        
        for (SubWorldInfoHolder curSubWorldInfo : subWorldInfos)
        {
            adminSubWorldInfos.put(curSubWorldInfo.subWorldId, new AdminSubWorldInfo(curSubWorldInfo));
        }
        
        for (WorldServer curDimensionWorld : DimensionManager.getWorlds())
        {
            for (World curSubWorldObj : curDimensionWorld.getSubWorlds())
            {
                SubWorld curSubWorld = (SubWorld)curSubWorldObj;
                
                AdminSubWorldInfo curInfo = adminSubWorldInfos.get(curSubWorld.getSubWorldID());
                if (curInfo == null)
                {
                    adminSubWorldInfos.put(curSubWorld.getSubWorldID(), new AdminSubWorldInfo(curSubWorld.getSubWorldID(), true, curDimensionWorld.getDimension()));
                }
                else
                {
                    curInfo.isSpawned = true;
                    curInfo.dimensionId = curDimensionWorld.getDimension();
                }
            }
        }
        
        MetaworldsMod.instance.networkHandler.sendTo(new MwAdminGuiSubWorldInfosPacket(adminSubWorldInfos.values()), this.player);
    }
    
    public void teleportPlayerToSubWorld(int subWorldId)
    {
        SubWorld subWorld = (SubWorld)player.worldObj.getSubWorld(subWorldId);
        
        if (subWorld != null)
        {
            double bbCenterX = (subWorld.getMaxX() + subWorld.getMinX()) / 2.0d;
            double bbCenterY = subWorld.getMaxY();
            double bbCenterZ = (subWorld.getMaxZ() + subWorld.getMinZ()) / 2.0d;
            
            Vec3 transformedPos = subWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
            
            player.mountEntity((Entity)null);
            player.setPositionAndUpdate(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
        }
    }
    
    public void teleportSubWorldToPlayer(int subWorldId)
    {
        SubWorld subWorld = (SubWorld)player.worldObj.getSubWorld(subWorldId);
        
        if (subWorld != null)
        {
            double bbCenterX = (subWorld.getMaxX() + subWorld.getMinX()) / 2.0d;
            double bbCenterY = subWorld.getMinY();
            double bbCenterZ = (subWorld.getMaxZ() + subWorld.getMinZ()) / 2.0d;
            
            Vec3 transformedPos = subWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
            
            subWorld.setTranslation(subWorld.getTranslationX() + player.posX - transformedPos.xCoord, subWorld.getTranslationY() + player.posY + 2.0d - transformedPos.yCoord, subWorld.getTranslationZ() + player.posZ - transformedPos.zCoord);
        }
    }
    
    public void spawnSubWorld(int subWorldId)
    {
        AdminSubWorldInfo info = this.adminSubWorldInfos.get(subWorldId);
        
        if (player.worldObj.getSubWorld(subWorldId) != null)
        {
            //ERROR, already spawned in this dimension - send updated infos
            this.sendSubWorldInfos();
            
            return;
        }
        
        if (info != null && !info.isSpawned)
        {
            World restoredWorld = player.worldObj.CreateSubWorld(subWorldId);
            info.isSpawned = true;
            info.dimensionId = ((WorldServer)restoredWorld).getDimension();
            MetaworldsMod.instance.networkHandler.sendTo(new MwAdminGuiSubWorldInfosPacket(adminSubWorldInfos.values()), this.player);
        }
    }
    
    public void despawnSubWorld(int subWorldId)
    {
        AdminSubWorldInfo info = this.adminSubWorldInfos.get(subWorldId);
        
        if (info != null && info.isSpawned)
        {
            World targetSubWorld = DimensionManager.getWorld(info.dimensionId).getSubWorld(subWorldId);
            
            if (targetSubWorld != null)
                ((SubWorld)targetSubWorld).removeSubWorld();
            
            info.isSpawned = false;
            MetaworldsMod.instance.networkHandler.sendTo(new MwAdminGuiSubWorldInfosPacket(adminSubWorldInfos.values()), this.player);
        }
    }
    
    public void stopSubWorldMotion(int subWorldId)
    {
        AdminSubWorldInfo info = this.adminSubWorldInfos.get(subWorldId);
        
        if (info == null || !info.isSpawned)
            return;
        
        World subWorld = DimensionManager.getWorld(info.dimensionId).getSubWorld(subWorldId);
        
        if (subWorld == null)
        {
            //Infos apparently not up to date anymore
            this.sendSubWorldInfos();
            return;
        }
        
        SubWorld subWorldToStop = (SubWorld)subWorld;
        
        subWorldToStop.setMotion(0, 0, 0);
        subWorldToStop.setRotationYawSpeed(0);
        subWorldToStop.setRotationPitchSpeed(0);
        subWorldToStop.setRotationRollSpeed(0);
        subWorldToStop.setScaleChangeRate(0);
    }
    
    public void resetSubWorldScale(int subWorldId)
    {
        AdminSubWorldInfo info = this.adminSubWorldInfos.get(subWorldId);
        
        if (info == null || !info.isSpawned)
            return;
        
        World subWorld = DimensionManager.getWorld(info.dimensionId).getSubWorld(subWorldId);
        
        if (subWorld == null)
        {
            //Infos apparently not up to date anymore
            this.sendSubWorldInfos();
            return;
        }
        
        SubWorld subWorldToStop = (SubWorld)subWorld;
        
        subWorldToStop.setScaling(1.0d);
    }
    
    public void importSubWorld(int worldListIndex, int subWorldListIndex)
    {
        if (worldListIndex < 0 || worldListIndex >= this.saveList.size())
            return;
        
        SaveGameInfo saveInfo = this.saveList.get(worldListIndex);
        
        if (subWorldListIndex < 0 || subWorldListIndex >= saveInfo.subWorldsList.size())
            return;
        
        SaveGameSubWorldInfo subWorldInfo = saveInfo.subWorldsList.get(subWorldListIndex);
        
        WorldInfo saveWorldInfo = player.mcServer.getActiveAnvilConverter().getWorldInfo(saveInfo.worldFileName);
        SubWorldInfoHolder sourceSubWorldInfo = null;
        
        if (saveWorldInfo != null && subWorldInfo.subWorldId != 0)
        {
            sourceSubWorldInfo = saveWorldInfo.getSubWorldInfo(subWorldInfo.subWorldId);
            
            if (sourceSubWorldInfo == null)
                saveWorldInfo = null;
        }
        
        File subWorldDir = new File(saveInfo.saveDir, subWorldInfo.subWorldSaveDirName);
        
        int newSubWorldId = DimensionManager.getWorld(0).getWorldInfo().getNextSubWorldID();
        
        SubWorldImporterThread newImportThread = new SubWorldImporterThread(newSubWorldId, saveInfo.saveDir, subWorldDir, saveWorldInfo, sourceSubWorldInfo);
        newImportThread.start();
    }
    
    public static class SaveGameInfo
    {
        public String worldFileName;
        public File saveDir;
        
        List<SaveGameSubWorldInfo> subWorldsList = new ArrayList<SaveGameSubWorldInfo>();
        
        public SaveGameInfo(String parFileName, File parSaveDir)
        {
            this.worldFileName = parFileName;
            this.saveDir = parSaveDir;
        }
    }
    
    public static class SaveGameSubWorldInfo
    {
        public String subWorldName;
        public String subWorldSaveDirName;
        public int subWorldId;
        
        public SaveGameSubWorldInfo(String parSubWorldName, String parSaveDirName, int parSubWorldId)
        {
            this.subWorldName = parSubWorldName;
            this.subWorldSaveDirName = parSaveDirName;
            this.subWorldId = parSubWorldId;
        }
    }
    
    public static class AdminSubWorldInfo implements Comparable
    {
        public int subWorldId;
        public boolean isSpawned;
        public int dimensionId;
        
        public AdminSubWorldInfo(int parSubWorldId, boolean parIsSpawned, int parDimensionId)
        {
            this.subWorldId = parSubWorldId;
            this.isSpawned = parIsSpawned;
            this.dimensionId = parDimensionId;
        }
        
        public AdminSubWorldInfo(SubWorldInfoHolder parInfoHolder)
        {
            this(parInfoHolder.subWorldId, false, 0);
        }

        @Override
        public int compareTo(Object o)
        {
            return this.subWorldId - ((AdminSubWorldInfo)o).subWorldId;
        }
        
        @Override
        public String toString()
        {
            //String descriptor = "SubWorld " + this.subWorldId;
            
            return "SubWorld " + this.subWorldId;
        }
    }
}

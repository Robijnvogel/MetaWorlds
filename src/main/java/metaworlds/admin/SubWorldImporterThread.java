package metaworlds.admin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import metaworlds.patcher.SubWorldInfoHolder;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class SubWorldImporterThread extends Thread {
    
    protected int newSubWorldId;
    protected File sourceWorldDir;
    protected File sourceSubWorldDir;
    protected WorldInfo sourceWorldInfo;
    protected SubWorldInfoHolder sourceSubWorldInfo;
    
    private Map<Integer, Integer> blockIdReplacementMap = new HashMap<Integer, Integer>();
    
    private File targetSaveRegion;
    public SubWorldInfoHolder targetSubWorldInfo;
    
    protected boolean finished = false;
    
    public SubWorldImporterThread(int parNewSubWorldId, File parSourceWorldDir, File parSourceSubWorldDir, WorldInfo parWorldInfo, SubWorldInfoHolder parSubWorldInfo)
    {
        this.newSubWorldId = parNewSubWorldId;
        this.sourceWorldDir = parSourceWorldDir;
        this.sourceSubWorldDir = parSourceSubWorldDir;
        this.sourceWorldInfo = parWorldInfo;
        this.sourceSubWorldInfo = parSubWorldInfo;
    }
    
    @Override
    public void run()
    {
        synchronized(MwAdminContainer.importThreads)
        {
            MwAdminContainer.importThreads.add(this);
        }
        
        File newSubWorldDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), "SUBWORLD" + newSubWorldId);
        newSubWorldDir.mkdir();
        
        this.copySaveFiles(newSubWorldDir);
        
        this.generateSubWorldInfo();
        
        this.finished = true;
    }
    
    public boolean isFinished()
    {
        return this.finished;
    }
    
    private void copySaveFiles(File newSubWorldDir)
    {
        File sourceSaveData = new File(this.sourceSubWorldDir, "data");
        File sourceSaveRegion = new File(this.sourceSubWorldDir, "region");
        File sourceSaveForcedChunks = new File(this.sourceSubWorldDir, "forcedchunks.dat");
        
        if (sourceSaveData.exists() && sourceSaveData.isDirectory())
        {
            File targetSaveData = new File(newSubWorldDir, "data");
            
            try
            {
                FileUtils.copyDirectory(sourceSaveData, targetSaveData);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        targetSaveRegion = new File(newSubWorldDir, "region");
        if (sourceSaveRegion.exists() && sourceSaveRegion.isDirectory())
        {
            try
            {
                FileUtils.copyDirectory(sourceSaveRegion, targetSaveRegion);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
            targetSaveRegion.mkdir();
        
        if (sourceSaveForcedChunks.exists() && sourceSaveForcedChunks.isFile())
        {
            File targetSaveForcedChunks = new File(newSubWorldDir, "forcedchunks.dat");
            
            try
            {
                FileUtils.copyFile(sourceSaveForcedChunks, targetSaveForcedChunks);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private void generateSubWorldInfo()
    {
        if (this.sourceSubWorldInfo != null)
        {
            this.targetSubWorldInfo = new SubWorldInfoHolder(this.sourceSubWorldInfo);
            this.targetSubWorldInfo.subWorldId = this.newSubWorldId;
        }
        else
        {
            this.targetSubWorldInfo = new SubWorldInfoHolder(this.newSubWorldId);
        }
        
        //Check which blockIDs need to be replaced by which
        loadBockIdMappings();
        
        //TODO: remove entities from regions?
        loadRegions();
    }
    
    private void loadBockIdMappings()
    {
        if (this.sourceWorldInfo != null)
        {
            NBTTagCompound leveldat;
            try
            {
                leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(this.sourceWorldDir, "level.dat")));
            }
            catch (Exception e)
            {
                try
                {
                    leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(this.sourceWorldDir, "level.dat_old")));
                }
                catch (Exception e1)
                {
                    FMLLog.warning("There appears to be a problem loading a save for import.");
                    return;
                }
            }
            
            // alle tags durchgehen und nach subtags "ItemData" (Liste) suchen
            Set<String> nbtKeys = leveldat.func_150296_c();
            
            for (String curKey : nbtKeys)
            {
                NBTBase curTag = leveldat.getTag(curKey);
                
                if (curTag.getId() != 10)//type CompoundTag = 10
                    continue;
                
                if (!((NBTTagCompound)curTag).hasKey("ItemData", 9))
                    continue;
                
                addBlockIdMappings(((NBTTagCompound)curTag).getTagList("ItemData", 10));
            }
        }
    }
    
    private void addBlockIdMappings(NBTTagList itemDataTagList)
    {
        if (itemDataTagList.tagCount() == 0)
            return;
        
        for (int i = 0; i < itemDataTagList.tagCount(); i++)
        {
            NBTTagCompound dataTag = itemDataTagList.getCompoundTagAt(i);
            
            String itemName = dataTag.getString("K");
            char discriminator = itemName.charAt(0);
            itemName = itemName.substring(1);
            Integer refId = dataTag.getInteger("V");
            int currId;
            boolean isBlock = discriminator == '\u0001';
            if (isBlock)
            {
                currId = GameData.blockRegistry.getId(itemName);
            }
            else
            {
                currId = GameData.itemRegistry.getId(itemName);
            }

            if (isBlock)
            {
                if (currId == -1)
                    this.blockIdReplacementMap.put(refId, 0);
                else if (refId != currId)
                    this.blockIdReplacementMap.put(refId, currId);
            }
        }
    }
    
    private void loadRegions()
    {
        Pattern regionFilenameMatcher = Pattern.compile("r\\.-?\\d\\.-?\\d\\.mca");
        
        File[] regionFiles = this.targetSaveRegion.listFiles();
        
        Map<RegionFile, File> regions = new HashMap<RegionFile, File>(); 
        
        for (File curRegionFile : regionFiles)
        {
            if (!curRegionFile.isFile())
                continue;
            
            if (!regionFilenameMatcher.matcher(curRegionFile.getName()).matches())
                continue;
            
            regions.put(new RegionFile(curRegionFile), curRegionFile);
        }
        
        for (Map.Entry<RegionFile, File> curRegion : regions.entrySet())
        {
            String[] splits = curRegion.getValue().getName().split("\\.", -1);
            int regionX = Integer.parseInt(splits[1]);
            int regionZ = Integer.parseInt(splits[1]);
            checkRegionChunks(curRegion.getKey(), regionX, regionZ);
        }
    }
    
    private void checkRegionChunks(RegionFile region, int regionX, int regionZ)
    {
        for (int chunkInRegionX = 0; chunkInRegionX < 32; ++chunkInRegionX)
        {
            for (int chunkInRegionZ = 0; chunkInRegionZ < 32; ++chunkInRegionZ)
            {
                DataInputStream inputStream = region.getChunkDataInputStream(chunkInRegionX, chunkInRegionZ);
                
                if (inputStream == null)
                    continue;
                
                NBTTagCompound nbttagcompound = null;
                try
                {
                    nbttagcompound = CompressedStreamTools.read(inputStream);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                
                if (nbttagcompound != null && nbttagcompound.hasKey("Level", 10) && nbttagcompound.getCompoundTag("Level").hasKey("Sections", 9))
                {
                    ImportedChunk chunk = new ImportedChunk(nbttagcompound.getCompoundTag("Level"), this.blockIdReplacementMap);
                    chunk.updateNBT();
                    
                    updateBoundaries(chunk, chunkInRegionX * 16 + regionX * 512, chunkInRegionZ * 16 + regionZ * 512);//512 = 16 blocks/chunk * 32chunks/region
                    
                    DataOutputStream outputStream = region.getChunkDataOutputStream(chunkInRegionX, chunkInRegionZ);
                    
                    try
                    {
                        CompressedStreamTools.write(nbttagcompound, outputStream);
                        outputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void updateBoundaries(ImportedChunk chunk, int offsetX, int offsetZ)
    {
        if (chunk.minX == -1)
            return;
        
        SubWorldInfoHolder info = this.targetSubWorldInfo;
        
        if (info.minCoordinates.posX >= info.maxCoordinates.posX)
        {
            info.minCoordinates.posX = chunk.minX + offsetX;
            info.minCoordinates.posY = chunk.minY;
            info.minCoordinates.posZ = chunk.minZ + offsetZ;
            info.maxCoordinates.posX = chunk.maxX + offsetX;
            info.maxCoordinates.posY = chunk.maxY;
            info.maxCoordinates.posZ = chunk.maxZ + offsetZ;
        }
        else
        {
            //X
            if (info.minCoordinates.posX > chunk.minX + offsetX)
                info.minCoordinates.posX = chunk.minX + offsetX;
            
            if (info.maxCoordinates.posX < chunk.maxX + offsetX)
                info.maxCoordinates.posX = chunk.maxX + offsetX;
            
            //Y
            if (info.minCoordinates.posY > chunk.minY)
                info.minCoordinates.posY = chunk.minY;
            
            if (info.maxCoordinates.posY < chunk.maxY)
                info.maxCoordinates.posY = chunk.maxY;
            
            //Z
            if (info.minCoordinates.posZ > chunk.minZ + offsetZ)
                info.minCoordinates.posZ = chunk.minZ + offsetZ;
            
            if (info.maxCoordinates.posZ < chunk.maxZ + offsetZ)
                info.maxCoordinates.posZ = chunk.maxZ + offsetZ;
        }
    }
}

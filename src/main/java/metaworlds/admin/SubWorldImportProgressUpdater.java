package metaworlds.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import metaworlds.patcher.SubWorldInfoHolder;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class SubWorldImportProgressUpdater {

    private List<SubWorldImporterThread> finishedImports = new ArrayList<SubWorldImporterThread>();
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        synchronized(MwAdminContainer.importThreads)
        {
            for (ListIterator<SubWorldImporterThread> iter = MwAdminContainer.importThreads.listIterator(); iter.hasNext(); )
            {
                SubWorldImporterThread curThread = iter.next();
                
                if (curThread.isFinished())
                {
                    this.finishedImports.add(curThread);
                    iter.remove();
                }
            }
        }
        
        WorldInfo worldInfo = DimensionManager.getWorld(0).getWorldInfo();
        
        for (SubWorldImporterThread curFinishedImport : this.finishedImports)
        {
            worldInfo.updateSubWorldInfo(curFinishedImport.targetSubWorldInfo);
        }
        
        this.finishedImports.clear();
    }
}

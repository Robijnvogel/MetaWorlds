package metaworlds.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class PlayerTickHandler {
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase.equals(Phase.START))
        {
            if (event.player.isLosingTraction())
            {
                byte tractionTicks = event.player.getTractionLossTicks();
                
                if (tractionTicks >= event.player.tractionLossThreshold)
                {
                    event.player.setWorldBelowFeet(null);
                }
                else if (event.player.isOnLadder())
                {
                    event.player.setTractionTickCount((byte)0);
                }
                else
                {
                    event.player.setTractionTickCount((byte)(tractionTicks + 1));
                }
            }
        }
    }
}

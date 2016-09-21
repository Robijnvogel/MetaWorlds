package metaworlds.patcher;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class EntityPlayerIntermediateClass extends EntityPlayer {
    
    private final boolean isProxyPlayer = (this instanceof EntityPlayerProxy);

    public EntityPlayerIntermediateClass(World p_i45324_1_, GameProfile p_i45324_2_)
    {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Override
    public boolean isInBed()
    {
        if (super.isInBed())
            return true;
        
        if (!this.worldObj.isSubWorld())
        {
            for (EntityPlayerProxy curProxy : this.playerProxyMap.values())
            {
                if (((EntityPlayer)curProxy).isPlayerSleeping() && ((EntityPlayer)curProxy).isInBed())
                    return true;
            }
        }
        
        return false;
    }
    
    public void setSleeping(boolean newState)
    {
        this.sleeping = newState;
    }
    
    @Override
    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3)
    {
        super.wakeUpPlayer(par1, par2, par3);
        
        if (!this.isProxyPlayer)
        {
            for (EntityPlayerProxy curPlayerProxy : this.playerProxyMap.values())
            {
                ((EntityPlayer)curPlayerProxy).wakeUpPlayer(par1, par2, par3);
            }
        }
    }
    
    @Override
    public boolean isOnLadder()
    {
        if (this.isProxyPlayer)
            return ((EntityPlayerProxy)this).getRealPlayer().isOnLadder();
        else
        {
            if (super.isOnLadder())
                return true;
            
            for (EntityPlayerProxy curPlayerProxy : this.playerProxyMap.values())
            {
                if (((EntityPlayerIntermediateClass)curPlayerProxy).isOnLadderLocal())
                    return true;
            }
            
            return false;
        }
    }
    
    public boolean isOnLadderLocal()
    {
        return super.isOnLadder();
    }
    
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        if (this.worldObj.isSubWorld())
            return false;
        
        return super.shouldRenderInPass(pass);
    }
}

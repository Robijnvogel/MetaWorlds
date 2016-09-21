package metaworlds.boats;

import metaworlds.api.SubWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockFloatingWood extends Block {

    protected BlockFloatingWood()
    {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
    
    @Override
    public void onBlockAdded(World par1World, int par2, int par3, int par4) 
    {
        if (par1World.isSubWorld())
            return;
        
        World newWorld = par1World.CreateSubWorld();
        SubWorld newSubWorld = (SubWorld)newWorld;
        
        newSubWorld.setSubWorldType(1);
        
        newSubWorld.setTranslation(par2, newSubWorld.getTranslationY(), par4);
        newWorld.setBlock(0, par3, 0, this);
        par1World.setBlockToAir(par2, par3, par4);
        
        //Making it easier to spot:
        newSubWorld.setRotationYaw(45.0D);
    }
}

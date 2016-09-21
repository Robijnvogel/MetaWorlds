package metaworlds.joints;

import metaworlds.api.SubWorld;
import metaworlds.api.WorldSuperClass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockHingeJoint extends Block {
	public BlockHingeJoint()
    {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
	
	public void onBlockAdded(World par1World, int par2, int par3, int par4) 
	{
		if (par1World instanceof SubWorld)
			return;
		
		World newWorld = ((WorldSuperClass)par1World).CreateSubWorld();
		SubWorld newSubWorld = (SubWorld)newWorld;
		
		newSubWorld.setTranslation(par2, newSubWorld.getTranslationY(), par4);
		newWorld.setBlock(0, par3, 0, this);
		par1World.setBlockToAir(par2, par3, par4);
		
		//Making it easier to spot:
		newSubWorld.setRotationYaw(45.0D);
		newSubWorld.setRotationYawSpeed(1.0d);
		newSubWorld.setCenter(0.5d, par3 + 0.5d, 0.5d);
		newSubWorld.setRotationPitchSpeed(1.0d);
		newSubWorld.setRotationRollSpeed(1.0d);
	}
}

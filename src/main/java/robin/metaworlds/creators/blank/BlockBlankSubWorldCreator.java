package robin.metaworlds.creators.blank;

import robin.metaworlds.api.SubWorld;
import robin.metaworlds.api.WorldSuperClass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockBlankSubWorldCreator extends Block 
{
	public BlockBlankSubWorldCreator(Material material)
    {
        super(material);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
	
	public void registerIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("metaworldsblankcreatormod:" + this.getUnlocalizedName());
    }
	
	public void onBlockAdded(World par1World, int par2, int par3, int par4) 
	{
		if (((WorldSuperClass) par1World).isSubWorld())
			return;
		
		World newWorld = ((WorldSuperClass)par1World).CreateSubWorld();
		SubWorld newSubWorld = (SubWorld)newWorld;
		
		newSubWorld.setTranslation(par2, newSubWorld.getTranslationY(), par4);
		newWorld.setBlock(0, par3, 0, this);
		par1World.setBlockToAir(par2, par3, par4);
		
		//Making it easier to spot:
		newSubWorld.setRotationYaw(45.0D);
	}
}

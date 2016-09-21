package robin.metaworlds.controls.alignment;

import robin.metaworlds.api.SubWorld;
import robin.metaworlds.api.WorldSuperClass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockSubWorldAligner extends Block {
	public BlockSubWorldAligner(Material material)
    {
        super(material);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
	
	public void registerIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("metaworldscontrolsalignmod:" + this.getUnlocalizedName());
    }
	
	public void onBlockAdded(World par1World, int par2, int par3, int par4) 
	{
		if (!(par1World instanceof SubWorld))
			return;
		
		SubWorld subWorldPar = (SubWorld)par1World;
		subWorldPar.setRotationYaw((double)Math.round(subWorldPar.getRotationYaw() / 90.0D) * 90.0D);
		subWorldPar.setRotationPitch((double)Math.round(subWorldPar.getRotationPitch() / 90.0D) * 90.0D);
		subWorldPar.setRotationRoll((double)Math.round(subWorldPar.getRotationRoll() / 90.0D) * 90.0D);
		subWorldPar.setTranslation(Math.round(subWorldPar.getTranslationX()), Math.round(subWorldPar.getTranslationY()), Math.round(subWorldPar.getTranslationZ()));
		
		
		subWorldPar.setMotion(0,  0,  0);
		
		subWorldPar.setRotationYawSpeed(0);
		subWorldPar.setRotationPitchSpeed(0);
		subWorldPar.setRotationRollSpeed(0);
		
		subWorldPar.setScaleChangeRate(0);
	}
}

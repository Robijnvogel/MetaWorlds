package robin.metaworlds.controls.captain;

import robin.metaworlds.api.EntitySuperClass;
import robin.metaworlds.api.WorldSuperClass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockSubWorldController extends Block
{
    public BlockSubWorldController(Material material)
    {
        super(material);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
    
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("metaworldscontrolscaptainmod:" + this.getUnlocalizedName());
    }
    
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
    	if (par1World.isRemote)
    		return true;
    	
    	if (par5EntityPlayer.isRiding() && par5EntityPlayer.ridingEntity instanceof EntitySubWorldController)
    		return true;
    	
    	EntitySuperClass entityPlayerSuperClass = (EntitySuperClass)par5EntityPlayer;
    	
    	World entityParentWorld = par5EntityPlayer.worldObj;
    	//if (entityPlayerSuperClass.getWorldBelowFeet() != null)
    	//	entityParentWorld = entityPlayerSuperClass.getWorldBelowFeet();
    	
    	WorldSuperClass entityParentSubWorld = (WorldSuperClass)entityParentWorld;
    	
    	Vec3 transformedPos = entityParentSubWorld.transformToLocal(par5EntityPlayer);
    	EntitySubWorldController controllerEntity = new EntitySubWorldController(entityParentWorld, par1World, transformedPos.xCoord, transformedPos.yCoord + 0.6d, transformedPos.zCoord);
    	controllerEntity.setStartingYaw((float)((WorldSuperClass) par1World).getRotationYaw() + par5EntityPlayer.rotationYaw);
    	//controllerEntity.startingYaw = par1World.getRotationYaw() + par5EntityPlayer.rotationYaw;
    	controllerEntity.setControlledWorld(par1World);
    	
    	if (entityPlayerSuperClass.getWorldBelowFeet() != null)
    		controllerEntity.setWorldBelowFeet(entityPlayerSuperClass.getWorldBelowFeet());
    	else
    		controllerEntity.setWorldBelowFeet(entityParentWorld);
    	
    	if (!entityParentWorld.isRemote)
        {
    		entityParentWorld.spawnEntityInWorld(controllerEntity);
        }
    	//par5EntityPlayer.worldObj.addEntityToWorld(controllerEntity);
    	
    	controllerEntity.interactFirst(par5EntityPlayer);
    	
    	//controllerEntity.setWorldBelowFeet(entityParentWorld);
    	
    	//par5EntityPlayer.mountEntity(controllerEntity);
    	
    	return true;
    }
}

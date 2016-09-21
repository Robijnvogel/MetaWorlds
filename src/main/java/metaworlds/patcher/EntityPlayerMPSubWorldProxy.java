package metaworlds.patcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;

public class EntityPlayerMPSubWorldProxy extends EntityPlayerMP implements EntityPlayerProxy {

	private EntityPlayerMP realPlayer;
	
	public EntityPlayerMPSubWorldProxy(EntityPlayerMP originalPlayer, World targetSubWorld)
	{
		super(originalPlayer.mcServer, (WorldServer)targetSubWorld/*originalPlayer.worldObj*/, originalPlayer.getGameProfile(), new ItemInWorldManagerForProxy(targetSubWorld, originalPlayer.theItemInWorldManager)/*((ItemInWorldManagerSubWorldsHolder)originalPlayer.theItemInWorldManager).getForSubWorld(targetSubWorld.getSubWorldID())*/);
		this.realPlayer = originalPlayer;
		this.dimension = this.realPlayer.dimension;
		this.setEntityId(this.realPlayer.getEntityId());
		this.inventory = this.realPlayer.inventory;
		this.inventoryContainer = this.realPlayer.inventoryContainer;
		this.theItemInWorldManager.setGameType(this.realPlayer.theItemInWorldManager.getGameType());
		this.capabilities = this.realPlayer.capabilities;
		
		this.openContainer = this.inventoryContainer;
		
		this.preventEntitySpawning = false;
		
		this.realPlayer.playerProxyMap.put(targetSubWorld.getSubWorldID(), this);
		
		//The NetServerHandler assigns itself to the provided player object
		NetHandlerPlayServer netserverhandler = new NetHandlerPlayServer(this.mcServer, new NetworkManagerSubWorldProxy(originalPlayer.playerNetServerHandler.netManager, targetSubWorld.getSubWorldID(), false), this);
		
		//Set position to transformed coordinates
		Vec3 localPos = this.realPlayer.getLocalPos(this.worldObj);
		this.posX = localPos.xCoord;
		this.posY = localPos.yCoord;
		this.posZ = localPos.zCoord;
	}
	
	/*public EntityPlayerMPSubWorldProxy(MinecraftServer par1MinecraftServer,
			World par2World, String par3Str,
			ItemInWorldManager par4ItemInWorldManager) {
		super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
	}*/
	
	@Override
	public int hashCode()
	{
		return this.realPlayer.hashCode();
	}
	
	@Override
	public NBTTagCompound getEntityData()
	{
		return this.realPlayer.getEntityData();
	}
	
	@Override
	public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties)
	{
	    if (this.realPlayer == null)
	        return super.registerExtendedProperties(identifier, properties);
	    
		return this.realPlayer.registerExtendedProperties(identifier, properties);
	}
	
	@Override
	public IExtendedEntityProperties getExtendedProperties(String identifier)
	{
	    if (this.realPlayer == null)
	        return super.getExtendedProperties(identifier);
	    
		return this.realPlayer.getExtendedProperties(identifier);
	}
	
	@Override
	public NetHandlerPlayServer getNetHandlerProxy()
	{
		return this.playerNetServerHandler;
	}
	
	@Override
	public EntityPlayer getRealPlayer()
	{
		return this.realPlayer;
	}
	
	@Override
	public void travelToDimension(int par1)
	{
		this.realPlayer.travelToDimension(par1);
	}
	
	@Override
	public void setPosition(double par1, double par3, double par5)
	{
		super.setPosition(par1, par3, par5);
	}
	
	@Override
	public void moveEntity(double par1, double par3, double par5)
	{
		super.moveEntity(par1, par3, par5);
	}
	
	@Override
	public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8)
	{
		super.setPositionAndRotation(par1, par3, par5, par7, par8);
	}
	
	@Override
	public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8)
	{
		super.setLocationAndAngles(par1, par3, par5, par7, par8);
	}
	
	@Override
	public EntityPlayer.EnumStatus sleepInBedAt(int par1, int par2, int par3)
	{
	    EntityPlayer.EnumStatus result = super.sleepInBedAt(par1, par2, par3);
	    
	    if (result == EntityPlayer.EnumStatus.OK)
	    {
	        EntityPlayerIntermediateClass player = (EntityPlayerIntermediateClass)this.getRealPlayer();
	        player.setSleeping(true);
	        player.playerLocation = new ChunkCoordinates(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
	        
	        player.worldObj.updateAllPlayersSleepingFlag();
	    }
	    
	    
	    return result;
	}
}

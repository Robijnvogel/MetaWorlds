package metaworlds.core;

import metaworlds.api.SubWorld;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandTPWorlds extends CommandBase {

	@Override
	public String getCommandName() {
		return "tpworlds";
	}
	
	@Override
	public int getRequiredPermissionLevel()
    {
        return 3;
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/tpworlds Teleports all subworlds to you";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		
		if (icommandsender instanceof EntityPlayer)
		{
			EntityPlayer senderPlayer = (EntityPlayer)icommandsender;
			
			World baseWorld = senderPlayer.worldObj.getParentWorld();
			
			for (World curWorld : baseWorld.getSubWorlds())
			{
				SubWorld curSubWorld = (SubWorld)curWorld;
				
				double bbCenterX = (curSubWorld.getMaxX() + curSubWorld.getMinX()) / 2.0d;
				double bbCenterY = curSubWorld.getMaxY();//(curSubWorld.getMaxY() + curSubWorld.getMinY()) / 2.0d;
				double bbCenterZ = (curSubWorld.getMaxZ() + curSubWorld.getMinZ()) / 2.0d;
				
				Vec3 transformedPos = curSubWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
				
				//curSubWorld.setTranslation(curSubWorld.getTranslationX() + senderPlayer.posX - bbCenterX, curSubWorld.getTranslationY() + senderPlayer.posY - bbCenterY, curSubWorld.getTranslationZ() + senderPlayer.posZ - bbCenterZ);
				curSubWorld.setTranslation(curSubWorld.getTranslationX() + senderPlayer.posX - transformedPos.xCoord, curSubWorld.getTranslationY() + senderPlayer.posY - transformedPos.yCoord, curSubWorld.getTranslationZ() + senderPlayer.posZ - transformedPos.zCoord);
			}
		}
	}

}

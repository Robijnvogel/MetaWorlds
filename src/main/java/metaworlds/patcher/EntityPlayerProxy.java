package metaworlds.patcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

public interface EntityPlayerProxy {
	public INetHandler getNetHandlerProxy();
	public EntityPlayer getRealPlayer();
}

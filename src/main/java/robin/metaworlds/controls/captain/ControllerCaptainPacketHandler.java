package robin.metaworlds.controls.captain;

import java.util.logging.Logger;

import robin.metaworlds.controls.captain.ControllerKeyUpdatePacket.ProtocolException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import java.util.logging.Level;

public class ControllerCaptainPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet packet, Player player) {
		try {
            EntityPlayer entityPlayer = (EntityPlayer)player;
            ByteArrayDataInput in = ByteStreams.newDataInput(packet.readPacketData(new PacketBuffer()));
            int packetId = in.readUnsignedByte(); // Assuming your packetId is between 0 (inclusive) and 256 (exclusive). If you need more you need to change this
            ControllerKeyUpdatePacket updatePacket = ControllerKeyUpdatePacket.constructPacket(packetId);
            updatePacket.read(in);
            updatePacket.execute(entityPlayer, entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
	    } catch (ProtocolException e) {
	            if (player instanceof EntityPlayerMP) {
	                    ((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Protocol Exception!");
	                    Logger.getLogger("DemoMod").log(Level.WARNING, "Player {0} caused a Protocol Exception!", ((EntityPlayer)player).getDisplayName());
	            }
            }
	}
}

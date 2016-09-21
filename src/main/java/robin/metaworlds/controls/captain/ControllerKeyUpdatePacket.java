package robin.metaworlds.controls.captain;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public class ControllerKeyUpdatePacket {
	private boolean ctrlDown;
	
	public ControllerKeyUpdatePacket()
	{ }
	
	public ControllerKeyUpdatePacket(boolean isCtrlDown)
	{
		this.ctrlDown = isCtrlDown;
	}
	
	public void read(ByteArrayDataInput par1DataInput) throws ProtocolException
    {
		this.ctrlDown = par1DataInput.readBoolean();
    }
	
	public void write(ByteArrayDataOutput par1DataOutput)
    {
    	par1DataOutput.writeBoolean(this.ctrlDown);
    }
	
	public static final String CHANNEL = "mwcaptain";
	
	public final int getPacketId() {
        return 248;
	}
	
	public final Packet makePacket() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(getPacketId());
        write(out);
        return PacketDispatcher.getPacket(CHANNEL, out.toByteArray());
    }
	
	public static ControllerKeyUpdatePacket constructPacket(int packetId) throws ProtocolException, ReflectiveOperationException {
    	if (packetId == 248)
            return new ControllerKeyUpdatePacket();
    	else
    		return null;
    }
	
	public static class ProtocolException extends Exception {

        public ProtocolException() {
        }

        public ProtocolException(String message, Throwable cause) {
                super(message, cause);
        }

        public ProtocolException(String message) {
                super(message);
        }

        public ProtocolException(Throwable cause) {
                super(cause);
        }
    }
	
	public void execute(EntityPlayer player, Side side) throws ProtocolException
    {
    	if (side.isClient())
    		return;
    	
    	ControllerKeyServerStore keyStore = (ControllerKeyServerStore)player.getExtendedProperties("LCTRL");
    	if (keyStore == null)
    	{
    		keyStore = new ControllerKeyServerStore();
    		player.registerExtendedProperties("LCTRL", keyStore);
    	}
    	
    	keyStore.ctrlDown = this.ctrlDown;
    }
}

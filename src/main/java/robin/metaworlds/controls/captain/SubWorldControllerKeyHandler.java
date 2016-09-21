package robin.metaworlds.controls.captain;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class SubWorldControllerKeyHandler {
	
	public static boolean ctrl_down = false;

	public SubWorldControllerKeyHandler(KeyBinding[] keyBindings,
			boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	@Override
	public String getLabel() {
		return "SubWorldController KeyHandler";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
		if (kb.getKeyCode() == Keyboard.KEY_LCONTROL)
		{
			ctrl_down = true;
			PacketDispatcher.sendPacketToServer(new ControllerKeyUpdatePacket(ctrl_down).makePacket());
		}
		
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if (kb.getKeyCode() == Keyboard.KEY_LCONTROL)
		{
			ctrl_down = false;
			PacketDispatcher.sendPacketToServer(new ControllerKeyUpdatePacket(ctrl_down).makePacket());
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}

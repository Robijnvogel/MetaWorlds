package robin.metaworlds.controls.captain;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ControllerKeyServerStore implements IExtendedEntityProperties {

	public boolean ctrlDown = false;
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
	}

	@Override
	public void init(Entity entity, World world) {
	}

}

package metaworlds.api;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public interface EntitySuperClass {
	public World getWorldBelowFeet();
	
	//Beware: this is also called whenever the entity moves so 
	//using this manually is only useful for immobile entities
	public void setWorldBelowFeet(World newWorldBelowFeet);
	
	//Returns this entity's position in global coordinates
	public Vec3 getGlobalPos();
	//Return this entity's position in referenceWorld's coordinate system
	public Vec3 getLocalPos(World referenceWorld);
	
	public double getGlobalRotationYaw();
	
	//Workaround... returns false if this is not instanceof EntityLivingBase
	public boolean getIsJumping();
}

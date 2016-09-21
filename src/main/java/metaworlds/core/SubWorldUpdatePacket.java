package metaworlds.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import metaworlds.api.SubWorld;
import metaworlds.core.client.SubWorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class SubWorldUpdatePacket extends MetaWorldsPacket
{
    public int subWorldId;
    /**
     * Update flags:
     * 0x01: Include position + orientation
     * 0x02: Include motion + rotation speed (all assumed 0 if flag not present)
     * 0x04: Include center coordinates 
     * 0x08: Include boundaries
     * 0x10: Include sub-world type
     */
    public int flags;
    public int serverTick;
    public double positionX;
    public double positionY;
    public double positionZ;
    public double rotationYaw;
    public double rotationPitch;
    public double rotationRoll;
    public double scaling;
    
    public double centerX;
    public double centerY;
    public double centerZ;
    
    public double motionX;
    public double motionY;
    public double motionZ;
    public double rotationYawFrequency;
    public double rotationPitchFrequency;
    public double rotationRollFrequency;
    public double scaleChangeRate;
    
    //Boundaries
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    
    public int subWorldType;

    public SubWorldUpdatePacket() {}

    public SubWorldUpdatePacket(SubWorldServer par1SubWorldServer, int updateFlags)
    {
       // this(par1SubWorldServer.getSubWorldID(), MinecraftServer.getServer().getTickCounter(), par1SubWorldServer.getRotationYaw(), par1SubWorldServer.getTranslationX(), par1SubWorldServer.getTranslationY(), par1SubWorldServer.getTranslationZ(), par1SubWorldServer.getMotionX(), par1SubWorldServer.getMotionY(), par1SubWorldServer.getMotionZ(), par1SubWorldServer.getRotationYawSpeed());
    	
    	this.subWorldId = par1SubWorldServer.getSubWorldID();
    	this.flags = updateFlags;
        this.serverTick = MinecraftServer.getServer().getTickCounter();
        
        this.positionX = par1SubWorldServer.getTranslationX();
        this.positionY = par1SubWorldServer.getTranslationY();
        this.positionZ = par1SubWorldServer.getTranslationZ();
        this.rotationYaw = par1SubWorldServer.getRotationYaw();
        this.rotationPitch = par1SubWorldServer.getRotationPitch();
        this.rotationRoll = par1SubWorldServer.getRotationRoll();
        this.scaling = par1SubWorldServer.getScaling();
        
        this.centerX = par1SubWorldServer.getCenterX();
        this.centerY = par1SubWorldServer.getCenterY();
        this.centerZ = par1SubWorldServer.getCenterZ();
        
        this.motionX = par1SubWorldServer.getMotionX();
        this.motionY = par1SubWorldServer.getMotionY();
        this.motionZ = par1SubWorldServer.getMotionZ();
        this.rotationYawFrequency = par1SubWorldServer.getRotationYawSpeed();
        this.rotationPitchFrequency = par1SubWorldServer.getRotationPitchSpeed();
        this.rotationRollFrequency = par1SubWorldServer.getRotationRollSpeed();
        this.scaleChangeRate = par1SubWorldServer.getScaleChangeRate();
        
        this.minX = par1SubWorldServer.getMinX();
        this.minY = par1SubWorldServer.getMinY();
        this.minZ = par1SubWorldServer.getMinZ();
        this.maxX = par1SubWorldServer.getMaxX();
        this.maxY = par1SubWorldServer.getMaxY();
        this.maxZ = par1SubWorldServer.getMaxZ();
        
        this.subWorldType = par1SubWorldServer.getSubWorldType();
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    @Override
    public void read(ChannelHandlerContext ctx, ByteBuf par1DataInput)
    {
    	this.subWorldId = par1DataInput.readUnsignedShort();
    	this.flags = par1DataInput.readUnsignedByte();
    	this.serverTick = par1DataInput.readInt();
    	
    	if ((this.flags & 0x01) != 0)
    	{
        	this.positionX = par1DataInput.readFloat();
        	this.positionY = par1DataInput.readFloat();
        	this.positionZ = par1DataInput.readFloat();
    		this.rotationYaw = par1DataInput.readFloat();
    		this.rotationPitch = par1DataInput.readFloat();
    		this.rotationRoll = par1DataInput.readFloat();
    		this.scaling = par1DataInput.readFloat();
    	}
    	
    	if ((this.flags & 0x02) != 0)
    	{
    		this.motionX = par1DataInput.readFloat();
        	this.motionY = par1DataInput.readFloat();
        	this.motionZ = par1DataInput.readFloat();
        	this.rotationYawFrequency = par1DataInput.readFloat();
        	this.rotationPitchFrequency = par1DataInput.readFloat();
        	this.rotationRollFrequency = par1DataInput.readFloat();
        	this.scaleChangeRate = par1DataInput.readFloat();
    	}
    	
    	if ((this.flags & 0x04) != 0)
    	{
    		this.centerX = par1DataInput.readFloat();
    		this.centerY = par1DataInput.readFloat();
    		this.centerZ = par1DataInput.readFloat();
    	}
    	
    	if ((this.flags & 0x08) != 0)
    	{
    		this.minX = par1DataInput.readInt();
        	this.minY = par1DataInput.readInt();
        	this.minZ = par1DataInput.readInt();
        	this.maxX = par1DataInput.readInt();
        	this.maxY = par1DataInput.readInt();
        	this.maxZ = par1DataInput.readInt();
    	}
    	
    	if ((this.flags & 0x10) != 0)
    	{
    	    this.subWorldType = par1DataInput.readInt();
    	}
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    @Override
    public void write(ChannelHandlerContext ctx, ByteBuf par1DataOutput)
    {
    	par1DataOutput.writeShort(this.subWorldId);
    	par1DataOutput.writeByte(this.flags);
    	par1DataOutput.writeInt(this.serverTick);
    	
    	if ((this.flags & 0x01) != 0)
    	{
	    	par1DataOutput.writeFloat((float)this.positionX);
	    	par1DataOutput.writeFloat((float)this.positionY);
	    	par1DataOutput.writeFloat((float)this.positionZ);
	    	par1DataOutput.writeFloat((float)this.rotationYaw);
	    	par1DataOutput.writeFloat((float)this.rotationPitch);
	    	par1DataOutput.writeFloat((float)this.rotationRoll);
	    	par1DataOutput.writeFloat((float)this.scaling);
    	}
    	
    	if ((this.flags & 0x02) != 0)
    	{
    		par1DataOutput.writeFloat((float)this.motionX);
        	par1DataOutput.writeFloat((float)this.motionY);
        	par1DataOutput.writeFloat((float)this.motionZ);
        	par1DataOutput.writeFloat((float)this.rotationYawFrequency);
        	par1DataOutput.writeFloat((float)this.rotationPitchFrequency);
        	par1DataOutput.writeFloat((float)this.rotationRollFrequency);
        	par1DataOutput.writeFloat((float)this.scaleChangeRate);
    	}
    	
    	if ((this.flags & 0x04) != 0)
    	{
    		par1DataOutput.writeFloat((float)this.centerX);
    		par1DataOutput.writeFloat((float)this.centerY);
    		par1DataOutput.writeFloat((float)this.centerZ);
    	}
    	
    	if ((this.flags & 0x08) != 0)
    	{
    		par1DataOutput.writeInt(this.minX);
    		par1DataOutput.writeInt(this.minY);
    		par1DataOutput.writeInt(this.minZ);
    		par1DataOutput.writeInt(this.maxX);
    		par1DataOutput.writeInt(this.maxY);
    		par1DataOutput.writeInt(this.maxZ);
    	}
    	
    	if ((this.flags & 0x10) != 0)
    	{
    	    par1DataOutput.writeInt(this.subWorldType);
    	}
    }
    
    public boolean containsSameEntityIDAs(SubWorldUpdatePacket par1Packet)
    {
    	SubWorldUpdatePacket var2 = (SubWorldUpdatePacket)par1Packet;
        return var2.subWorldId == this.subWorldId;
    }
    
    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx)
    {
    	if (side.isServer())
    		return;
    	
    	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    	World targetWorld = player.worldObj.getSubWorld(this.subWorldId);
    	if (targetWorld == null)
    	{
    		//targetWorld = player.worldObj.CreateSubWorld(this.subWorldId);
    	}
    	
    	if (targetWorld.isSubWorld())
    	{
    		SubWorldClient targetSubWorld = (SubWorldClient)targetWorld;
    		
    		if (targetSubWorld.lastServerTickReceived < this.serverTick)
    		{
    		    if (targetSubWorld.getUpdatePacketToHandle() == null || targetSubWorld.getUpdatePacketToHandle().serverTick <= this.serverTick)
    		        targetSubWorld.setUpdatePacketToHandle(this);
    		    
    			/*float newTickDiff = this.serverTick - (float)targetSubWorld.localTickCounter;
    			
    			if (targetSubWorld.lastServerTickReceived == -1)
        			targetSubWorld.serverTickDiff = newTickDiff;
        		else if (targetSubWorld.localTickCounter < 8)
        			targetSubWorld.serverTickDiff = MathHelper.ceiling_float_int((float)(targetSubWorld.serverTickDiff * (float)targetSubWorld.localTickCounter + newTickDiff) / (float)(targetSubWorld.localTickCounter + 1));
        		else
        			targetSubWorld.serverTickDiff = MathHelper.ceiling_float_int((float)(targetSubWorld.serverTickDiff * 7.0f + newTickDiff) / 8.0f);
    			
    			targetSubWorld.lastServerTickReceived = this.serverTick;
    			
    			if ((this.flags & 0x04) != 0)
    				targetSubWorld.setCenter(this.centerX, this.centerY, this.centerZ);
    			
    			if ((this.flags & 0x01) != 0)
    				targetSubWorld.UpdatePositionAndRotation(this.positionX, this.positionY, this.positionZ, this.rotationYaw, this.rotationPitch, this.rotationRoll, this.scaling);
    			
    			double oldScaleChangeRate = targetSubWorld.getScaleChangeRate();
    			double oldRotationYawFreq = targetSubWorld.getRotationYawSpeed();
    			double oldRotationPitchFreq = targetSubWorld.getRotationPitchSpeed();
    			double oldRotationRollFreq = targetSubWorld.getRotationRollSpeed();
    			double oldMotionX = targetSubWorld.getMotionX();
    			double oldMotionY = targetSubWorld.getMotionY();
    			double oldMotionZ = targetSubWorld.getMotionZ();
    			//Interpolate between last and current speed for keepup step to make it move smoother
    			targetSubWorld.setScaleChangeRate((this.scaleChangeRate + oldScaleChangeRate) * 0.5D);
        		targetSubWorld.setRotationYawSpeed((this.rotationYawFrequency + oldRotationYawFreq) * 0.5D);
        		targetSubWorld.setRotationPitchSpeed((this.rotationPitchFrequency + oldRotationPitchFreq) * 0.5D);
        		targetSubWorld.setRotationRollSpeed((this.rotationRollFrequency + oldRotationRollFreq) * 0.5D);
        		targetSubWorld.setMotion((this.motionX + oldMotionX) * 0.5D, (this.motionY + oldMotionY) * 0.5D, (this.motionZ + oldMotionZ) * 0.5D);
        		
        		//As Subworlds will still update within this tick, counter that by adding offset -1
        		targetSubWorld.tickPosition((int)(targetSubWorld.serverTickDiff - newTickDiff) - 1);
        		
        		//Set actual new velocity
        		targetSubWorld.setScaleChangeRate(this.scaleChangeRate);
        		targetSubWorld.setRotationYawSpeed(this.rotationYawFrequency);
        		targetSubWorld.setRotationPitchSpeed(this.rotationPitchFrequency);
        		targetSubWorld.setRotationRollSpeed(this.rotationRollFrequency);
        		targetSubWorld.setMotion(this.motionX, this.motionY, this.motionZ);
        		
        		if ((this.flags & 0x08) != 0)
        			targetSubWorld.setBoundaries(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);*/
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void executeOnTick()
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World targetWorld = player.worldObj.getSubWorld(this.subWorldId);
        if (targetWorld == null)
        {
            //targetWorld = player.worldObj.CreateSubWorld(this.subWorldId);
        }
        
        if (targetWorld.isSubWorld())
        {
            SubWorldClient targetSubWorld = (SubWorldClient)targetWorld;

            float newTickDiff = this.serverTick - (float)targetSubWorld.localTickCounter;
            
            if (targetSubWorld.lastServerTickReceived == -1)
                targetSubWorld.serverTickDiff = newTickDiff;
            else if (targetSubWorld.localTickCounter < 8)
                targetSubWorld.serverTickDiff = MathHelper.ceiling_float_int((float)(targetSubWorld.serverTickDiff * (float)targetSubWorld.localTickCounter + newTickDiff) / (float)(targetSubWorld.localTickCounter + 1));
            else
                targetSubWorld.serverTickDiff = MathHelper.ceiling_float_int((float)(targetSubWorld.serverTickDiff * 7.0f + newTickDiff) / 8.0f);
            
            targetSubWorld.lastServerTickReceived = this.serverTick;
            
            if ((this.flags & 0x04) != 0)
                targetSubWorld.setCenter(this.centerX, this.centerY, this.centerZ);
            
            if ((this.flags & 0x01) != 0)
                targetSubWorld.UpdatePositionAndRotation(this.positionX, this.positionY, this.positionZ, this.rotationYaw, this.rotationPitch, this.rotationRoll, this.scaling);
            
            double oldScaleChangeRate = targetSubWorld.getScaleChangeRate();
            double oldRotationYawFreq = targetSubWorld.getRotationYawSpeed();
            double oldRotationPitchFreq = targetSubWorld.getRotationPitchSpeed();
            double oldRotationRollFreq = targetSubWorld.getRotationRollSpeed();
            double oldMotionX = targetSubWorld.getMotionX();
            double oldMotionY = targetSubWorld.getMotionY();
            double oldMotionZ = targetSubWorld.getMotionZ();
            //Interpolate between last and current speed for keepup step to make it move smoother
            targetSubWorld.setScaleChangeRate((this.scaleChangeRate + oldScaleChangeRate) * 0.5D);
            targetSubWorld.setRotationYawSpeed((this.rotationYawFrequency + oldRotationYawFreq) * 0.5D);
            targetSubWorld.setRotationPitchSpeed((this.rotationPitchFrequency + oldRotationPitchFreq) * 0.5D);
            targetSubWorld.setRotationRollSpeed((this.rotationRollFrequency + oldRotationRollFreq) * 0.5D);
            targetSubWorld.setMotion((this.motionX + oldMotionX) * 0.5D, (this.motionY + oldMotionY) * 0.5D, (this.motionZ + oldMotionZ) * 0.5D);
            
            //As Subworlds will still update within this tick, counter that by adding offset -1
            targetSubWorld.tickPosition((int)(targetSubWorld.serverTickDiff - newTickDiff) - 1);
            
            //Set actual new velocity
            targetSubWorld.setScaleChangeRate(this.scaleChangeRate);
            targetSubWorld.setRotationYawSpeed(this.rotationYawFrequency);
            targetSubWorld.setRotationPitchSpeed(this.rotationPitchFrequency);
            targetSubWorld.setRotationRollSpeed(this.rotationRollFrequency);
            targetSubWorld.setMotion(this.motionX, this.motionY, this.motionZ);
            
            if ((this.flags & 0x08) != 0)
                targetSubWorld.setBoundaries(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            
            if ((this.flags & 0x10) != 0)
            {
                targetSubWorld.setSubWorldType(this.subWorldType);
            }
        }
    }
}

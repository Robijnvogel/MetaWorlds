package robin.metaworlds.creators.contagious;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import robin.metaworlds.api.SubWorld;
import robin.metaworlds.api.WorldSuperClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class BlockContagiousSubWorldCreator extends Block {

    public static Map<Integer, Boolean> blockVolatilityMap = new TreeMap<Integer, Boolean>();

    public static boolean isBusy = false;//Prevent recursion

    public BlockContagiousSubWorldCreator(Material material) {
        super(material);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void registerIcons(IIconRegister par1IconRegister) {
        this.blockIcon = par1IconRegister.registerIcon("metaworldscontagiouscreatormod:" + this.getUnlocalizedName());
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (isBusy) {
            return;
        }

        isBusy = true;

        List<BlockCoord3> blocksToTake = new ArrayList<BlockCoord3>();
        //Take in intelligent order so e.g. torches don't fall off the wall in the process
        List<BlockCoord3> blocksToTakeVolatile = new ArrayList<BlockCoord3>();

        HashSet<BlockCoord3> prevMargin = new HashSet<BlockCoord3>();
        HashSet<BlockCoord3> margin = new HashSet<BlockCoord3>();
        HashSet<BlockCoord3> newMargin = new HashSet<BlockCoord3>();

        blocksToTake.add(new BlockCoord3(par2, par3, par4));
        margin.add(new BlockCoord3(par2, par3, par4));

        boolean isValid = true;
        do {
            isValid = expandAtMargin(par1World, blocksToTake, blocksToTakeVolatile, prevMargin, margin, newMargin);
            if (!isValid) {
                break;
            }

            //Prepare for next loop
            HashSet<BlockCoord3> tmp = prevMargin;
            prevMargin = margin;
            margin = newMargin;
            newMargin = tmp;
            newMargin.clear();
        } while (margin.size() > 0);

        if (isValid && par1World instanceof WorldSuperClass) {
            WorldSuperClass superWorld = (WorldSuperClass) par1World;
            World newWorld = superWorld.CreateSubWorld();
            SubWorld newSubWorld = (SubWorld) newWorld;

            //newSubWorld.setTranslation(par2, newSubWorld.getTranslationY(), par4);
            //1. Add non-volatile blocks
            for (BlockCoord3 curCoord : blocksToTake) {
                Block block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                int blockMetadata = par1World.getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                newWorld.setBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, block, blockMetadata, 3);
            }

            //2. Add volatile blocks
            //3. Remove volatile blocks
            for (BlockCoord3 curCoord : blocksToTakeVolatile) {
                Block block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                int blockMetadata = par1World.getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                newWorld.setBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, block, blockMetadata, 3);
            }

            for (BlockCoord3 curCoord : blocksToTakeVolatile) {
                par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
            }

            //4. Remove non-volatile blocks
            for (BlockCoord3 curCoord : blocksToTake) {
                par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
            }

            newSubWorld.setCenter(par1World.getSpawnPoint().posX, par1World.getSpawnPoint().posY, par1World.getSpawnPoint().posZ);
            newSubWorld.setTranslation(superWorld.getTranslationX(), superWorld.getTranslationY(), superWorld.getTranslationZ());
            newSubWorld.setRotationYaw(superWorld.getRotationYaw());
            newSubWorld.setRotationPitch(superWorld.getRotationPitch());
            newSubWorld.setRotationRoll(superWorld.getRotationRoll());
            newSubWorld.setScaling(superWorld.getScaling());

            newSubWorld.setCenter((double) par2 + 0.5d, (double) par3 + 0.5d, (double) par4 + 0.5d);
        }
        //else if (par1World.isRemote)
        //{
        //	Minecraft.getMinecraft().thePlayer.sendChatToPlayer(ChatMessageComponent.createFromText("SubWorld touches bedrock!"));
        //}

        isBusy = false;
    }

    public boolean expandAtMargin(World par1World, List<BlockCoord3> blockList, List<BlockCoord3> volatileBlockList, HashSet<BlockCoord3> prevMarginList, HashSet<BlockCoord3> marginList, HashSet<BlockCoord3> newMarginList) {
        for (BlockCoord3 curCoord : marginList) {
            for (int direction = 0; direction < 6; ++direction) {
                BlockCoord3 newCoords = new BlockCoord3(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);

                switch (direction / 2) {
                    case 0:
                        newCoords.blockPosY += (direction % 2) * 2 - 1;
                        break;
                    case 1:
                        newCoords.blockPosX += (direction % 2) * 2 - 1;
                        break;
                    case 2:
                        newCoords.blockPosZ += (direction % 2) * 2 - 1;
                        break;
                }

                //TODO: connect diagonally
                /*
				 * On diagonal connections ignore if isBlockReplaceable
				 * or if block instanceof BlockFlower
				 * also don't connect diagonally FROM those blocks
                 */
                //Prevents loops and improves performance compared to old implementation
                if (prevMarginList.contains(newCoords) || marginList.contains(newCoords)) {
                    continue;
                }

                Block curBlock = par1World.getBlock(newCoords.blockPosX, newCoords.blockPosY, newCoords.blockPosZ);
                int curBlockId = Block.getIdFromBlock(curBlock);
                
                if (curBlock instanceof BlockBed) {
                    return false;
                }

                //Only take sources
                if (curBlockId == 0 || curBlock instanceof BlockDynamicLiquid ) {
                    continue;
                }

                //Only take tops of waterfalls
                if (curBlock instanceof BlockStaticLiquid && Block.getIdFromBlock(par1World.getBlock(newCoords.blockPosX, newCoords.blockPosY + 1, newCoords.blockPosZ)) == curBlockId) {
                    continue;
                }

                if (newMarginList.add(newCoords)) {
                    Boolean isVolatile = blockVolatilityMap.get(curBlockId);

                    if (isVolatile == null) {
                        try {
                            if (Block.getBlockById(curBlockId).getClass().getMethod(BlockDummyReobfTracker.canBlockStayMethodName, World.class, int.class, int.class, int.class).getDeclaringClass().equals(Block.class)
                                    && Block.getBlockById(curBlockId).getClass().getMethod(BlockDummyReobfTracker.onNeighborBlockChange, World.class, int.class, int.class, int.class, int.class).getDeclaringClass().equals(Block.class)) {
                                isVolatile = false;
                            } else {
                                isVolatile = true;
                            }
                        } catch (SecurityException e) {
                        } catch (NoSuchMethodException ex) {
                            Logger.getLogger(BlockContagiousSubWorldCreator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        blockVolatilityMap.put(curBlockId, isVolatile);
                    }

                    if (isVolatile) {
                        volatileBlockList.add(newCoords);
                    } else {
                        blockList.add(newCoords);
                    }
                }
            }
        }

        return true;
    }

    public class BlockCoord3 {

        int blockPosX;
        int blockPosY;
        int blockPosZ;

        BlockCoord3(int x, int y, int z) {
            this.blockPosX = x;
            this.blockPosY = y;
            this.blockPosZ = z;
        }

        public boolean equals(Object par1Obj) {
            if (!(par1Obj instanceof BlockCoord3)) {
                return false;
            } else {
                BlockCoord3 targetCoord = (BlockCoord3) par1Obj;
                return targetCoord.blockPosX == this.blockPosX && targetCoord.blockPosY == this.blockPosY && targetCoord.blockPosZ == this.blockPosZ;
            }
        }

        public int hashCode() {
            return this.blockPosY + (this.blockPosX + (this.blockPosZ << 12)) << 8;
        }
    }
}

package metaworlds.patcher;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;

public class OBBPool
{
    /**
     * Maximum number of times the pool can be "cleaned" before the list is shrunk
     */
    private final int maxNumCleans;

    /**
     * Number of Pool entries to remove when cleanPool is called maxNumCleans times.
     */
    private final int numEntriesToRemove;

    /** List of AABB stored in this Pool */
    private final List listOBB = new ArrayList();

    /** Next index to use when adding a Pool Entry. */
    private int nextPoolIndex;

    /**
     * Largest index reached by this Pool (can be reset to 0 upon calling cleanPool)
     */
    private int maxPoolIndex;

    /** Number of times this Pool has been cleaned */
    private int numCleans;

    public OBBPool(int par1, int par2)
    {
        this.maxNumCleans = par1;
        this.numEntriesToRemove = par2;
    }

    /**
     * Creates a new AABB, or reuses one that's no longer in use. Parameters: minX, minY, minZ, maxX, maxY, maxZ. AABBs
     * returned from this function should only be used for one frame or tick, as after that they will be reused.
     */
    public OrientedBB getOBB(AxisAlignedBB sourceBB)
    {
        OrientedBB var13;

        if (this.nextPoolIndex >= this.listOBB.size())
        {
            var13 = new OrientedBB(sourceBB.minX, sourceBB.minY, sourceBB.minZ, sourceBB.maxX, sourceBB.maxY, sourceBB.maxZ);
            this.listOBB.add(var13);
        }
        else
        {
            var13 = (OrientedBB)this.listOBB.get(this.nextPoolIndex);
            var13.fromAABB(sourceBB);
        }

        ++this.nextPoolIndex;
        return var13;
    }

    /**
     * Marks the pool as "empty", starting over when adding new entries. If this is called maxNumCleans times, the list
     * size is reduced
     */
    public void cleanPool()
    {
        if (this.nextPoolIndex > this.maxPoolIndex)
        {
            this.maxPoolIndex = this.nextPoolIndex;
        }

        if (this.numCleans++ == this.maxNumCleans)
        {
            int var1 = Math.max(this.maxPoolIndex, this.listOBB.size() - this.numEntriesToRemove);

            while (this.listOBB.size() > var1)
            {
                this.listOBB.remove(var1);
            }

            this.maxPoolIndex = 0;
            this.numCleans = 0;
        }

        this.nextPoolIndex = 0;
    }

    /**
     * Clears the OBBPool
     */
    public void clearPool()
    {
        this.nextPoolIndex = 0;
        this.listOBB.clear();
    }

    public int getlistAABBsize()
    {
        return this.listOBB.size();
    }

    public int getnextPoolIndex()
    {
        return this.nextPoolIndex;
    }
}

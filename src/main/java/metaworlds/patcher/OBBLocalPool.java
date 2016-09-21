package metaworlds.patcher;

final class OBBLocalPool extends ThreadLocal
{
    protected OBBPool createNewDefaultPool()
    {
        return new OBBPool(300, 200);
    }

    protected Object initialValue()
    {
        return this.createNewDefaultPool();
    }
}

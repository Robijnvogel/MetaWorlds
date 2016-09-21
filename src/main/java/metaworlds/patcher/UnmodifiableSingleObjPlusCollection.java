package metaworlds.patcher;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import metaworlds.core.CompositeUnmodifiableCollection.CUCIterator;

public class UnmodifiableSingleObjPlusCollection<E> extends AbstractCollection<E> {

	public class SingleObjPlusCollIterator<T> implements Iterator<T>
	{
		T singleObj;
		Iterator<T> curIter;
		boolean isAtZero = true;
		
		public SingleObjPlusCollIterator(UnmodifiableSingleObjPlusCollection<T> parentCollection)
		{
			this.singleObj = parentCollection.mSingleObj;
			this.curIter = parentCollection.mPlusCollection.iterator();
		}

		@Override
		public boolean hasNext() {
			return isAtZero || curIter.hasNext();
		}

		@Override
		public T next() {
			if (isAtZero)
			{
				isAtZero = false;
				return singleObj;
			}
			else
				return curIter.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private E mSingleObj;
	private Collection<E> mPlusCollection;
	
	public UnmodifiableSingleObjPlusCollection(E singleObject, Collection<E> plusCollection)
	{
		this.mSingleObj = singleObject;
		this.mPlusCollection = plusCollection;
	}
	
	@Override
    public int size() {
        return mPlusCollection.size() + 1;
    }

	@Override
	public Iterator<E> iterator() {
		return new SingleObjPlusCollIterator(this);
	}
}

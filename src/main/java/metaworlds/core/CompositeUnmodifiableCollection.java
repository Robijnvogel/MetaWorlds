package metaworlds.core;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class CompositeUnmodifiableCollection<E> extends AbstractCollection<E> {
	
	public class CUCIterator<T> implements Iterator<T>
	{
		Iterator<T> iterFirst;
		Iterator<T> iterSecond;
		
		Iterator<T> curIter;
		
		public CUCIterator(CompositeUnmodifiableCollection<T> parentCollection)
		{
			iterFirst = parentCollection.collection1.iterator();
			iterSecond = parentCollection.collection2.iterator();
			
			curIter = iterFirst;
		}

		@Override
		public boolean hasNext() {
			return iterFirst.hasNext() || iterSecond.hasNext();
		}

		@Override
		public T next() {
			if (iterFirst.hasNext())
				return iterFirst.next();
			else
				return iterSecond.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

    private final Collection<E> collection1;
    private final Collection<E> collection2;

    public CompositeUnmodifiableCollection(Collection<E> list1, Collection<E> list2) {
        this.collection1 = list1;
        this.collection2 = list2;
    }

    @Override
    public int size() {
        return collection1.size() + collection2.size();
    }

	@Override
	public Iterator<E> iterator() {
		return new CUCIterator(this);
	}
}

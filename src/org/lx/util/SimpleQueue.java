package org.lx.util;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.lx.arch.SimpleSemaphore;

public class SimpleQueue extends AbstractQueue implements java.io.Serializable
{

	/**
	 * Serialization ID. This class relies on default serialization even for the
	 * items array, which is default-serialized, even if it is empty. Otherwise
	 * it could not be declared final, which is necessary here.
	 */
	private static final long serialVersionUID = -7078708147450251998L;

	/** The queued items */
	private final Object[] items;

	/** items index for next take, poll or remove */
	private int takeIndex;

	/** items index for next put, offer, or add. */
	private int putIndex;

	/** Number of items in the queue */
	private int count;

	private SimpleSemaphore notFull;

	private SimpleSemaphore notEmpty;

	// Internal helper methods
	public SimpleQueue(int capacity)
	{
		this.items = new Object[capacity];
		notFull = new SimpleSemaphore();
		notEmpty = new SimpleSemaphore();
	}

	/**
	 * Circularly increment i.
	 */
	final int inc(int i)
	{
		return (++i == items.length) ? 0 : i;
	}

	/**
	 * Inserts element at current put position, advances, and signals. Call only
	 * when holding lock.
	 */
	private synchronized void insert(Object x)
	{
		items[putIndex] = x;
		putIndex = inc(putIndex);
		++count;
		notEmpty.release();
	}

	/**
	 * Extracts element at current take position, advances, and signals.
	 */
	private synchronized Object extract()
	{
		Object x = items[takeIndex];
		items[takeIndex] = null;
		takeIndex = inc(takeIndex);
		--count;
		notFull.release();
		return x;
	}

	/**
	 * Utility for remove and iterator.remove: Delete item at position i. Call
	 * only when holding lock.
	 */
	synchronized void removeAt(int i)
	{
		// if removing front item, just advance
		if (i == takeIndex)
		{
			items[takeIndex] = null;
			takeIndex = inc(takeIndex);
		} else
		{
			// slide over all others up through putIndex.
			for (;;)
			{
				int nexti = inc(i);
				if (nexti != putIndex)
				{
					items[i] = items[nexti];
					i = nexti;
				} else
				{
					items[i] = null;
					putIndex = i;
					break;
				}
			}
		}
		--count;
		notFull.release();
	}

	/**
	 * Inserts the specified element at the tail of this queue if it is possible
	 * to do so immediately without exceeding the queue's capacity, returning
	 * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
	 * if this queue is full.
	 * @param e
	 *        the element to add
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws IllegalStateException
	 *         if this queue is full
	 * @throws NullPointerException
	 *         if the specified element is null
	 */
	public synchronized boolean add(Object e)
	{
		return super.add(e);
	}

	/**
	 * Inserts the specified element at the tail of this queue if it is possible
	 * to do so immediately without exceeding the queue's capacity, returning
	 * <tt>true</tt> upon success and <tt>false</tt> if this queue is full. This
	 * method is generally preferable to method {@link #add}, which can fail to
	 * insert an element only by throwing an exception.
	 * @throws NullPointerException
	 *         if the specified element is null
	 */
	public synchronized boolean offer(Object e)
	{
		if (e == null)
			throw new NullPointerException();
		if (count == items.length)
			return false;
		else
		{
			insert(e);
			return true;
		}
	}

	/**
	 * Inserts the specified element at the tail of this queue, waiting for
	 * space to become available if the queue is full.
	 * @throws InterruptedException
	 *         {@inheritDoc}
	 * @throws NullPointerException
	 *         {@inheritDoc}
	 */
	public synchronized void put(Object e) throws InterruptedException
	{
		if (e == null)
			throw new NullPointerException();
		while (count == items.length)
			notFull.acquire();
		insert(e);
	}

	public synchronized Object poll()
	{
		return count == 0 ? null : extract();
	}

	public synchronized Object take() throws InterruptedException
	{
		while (count == 0)
			notEmpty.acquire();
		return extract();
	}

	public synchronized Object peek()
	{
		return (count == 0) ? null : items[takeIndex];
	}

	public synchronized Object peek(Object o)
	{
		Object retval = null;
		do
		{
			if (o == null)
			{
				break;
			}
			int i = takeIndex;
			int k = 0;
			while (k++ < count)
			{
				if (o.equals(items[i]))
					break;
				i = inc(i);
			}
			retval = items[i];
		} while (false);
		return retval;
	}

	// this doc comment is overridden to remove the reference to collections
	// greater in size than Integer.MAX_VALUE
	/**
	 * Returns the number of elements in this queue.
	 * @return the number of elements in this queue
	 */
	public synchronized int size()
	{
		return count;
	}

	// this doc comment is a modified copy of the inherited doc comment,
	// without the reference to unlimited queues.
	/**
	 * Returns the number of additional elements that this queue can ideally (in
	 * the absence of memory or resource constraints) accept without blocking.
	 * This is always equal to the initial capacity of this queue less the
	 * current <tt>size</tt> of this queue. <p>Note that you <em>cannot</em>
	 * always tell if an attempt to insert an element will succeed by inspecting
	 * <tt>remainingCapacity</tt> because it may be the case that another thread
	 * is about to insert or remove an element.
	 */
	public synchronized int remainingCapacity()
	{
		return items.length - count;
	}

	/**
	 * Removes a single instance of the specified element from this queue, if it
	 * is present. More formally, removes an element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt>, if this queue contains one or more such elements.
	 * Returns <tt>true</tt> if this queue contained the specified element (or
	 * equivalently, if this queue changed as a result of the call).
	 * @param o
	 *        element to be removed from this queue, if present
	 * @return <tt>true</tt> if this queue changed as a result of the call
	 */
	public synchronized boolean remove(Object o)
	{
		if (o == null)
			return false;
		int i = takeIndex;
		int k = 0;
		for (;;)
		{
			if (k++ >= count)
				return false;
			if (o.equals(items[i]))
			{
				removeAt(i);
				return true;
			}
			i = inc(i);
		}

	}

	/**
	 * Returns <tt>true</tt> if this queue contains the specified element. More
	 * formally, returns <tt>true</tt> if and only if this queue contains at
	 * least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
	 * @param o
	 *        object to be checked for containment in this queue
	 * @return <tt>true</tt> if this queue contains the specified element
	 */
	public synchronized boolean contains(Object o)
	{
		if (o == null)
			return false;
		int i = takeIndex;
		int k = 0;
		while (k++ < count)
		{
			if (o.equals(items[i]))
				return true;
			i = inc(i);
		}
		return false;
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence. <p>The returned array will be "safe" in that no references to
	 * it are maintained by this queue. (In other words, this method must
	 * allocate a new array). The caller is thus free to modify the returned
	 * array. <p>This method acts as bridge between array-based and
	 * collection-based APIs.
	 * @return an array containing all of the elements in this queue
	 */
	public Object[] toArray()
	{
		Object[] a = new Object[count];
		int k = 0;
		int i = takeIndex;
		while (k < count)
		{
			a[k++] = items[i];
			i = inc(i);
		}
		return a;
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence; the runtime type of the returned array is that of the specified
	 * array. If the queue fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this queue. <p>If this queue fits in the
	 * specified array with room to spare (i.e., the array has more elements
	 * than this queue), the element in the array immediately following the end
	 * of the queue is set to <tt>null</tt>. <p>Like the {@link #toArray()}
	 * method, this method acts as bridge between array-based and
	 * collection-based APIs. Further, this method allows precise control over
	 * the runtime type of the output array, and may, under certain
	 * circumstances, be used to save allocation costs. <p>Suppose <tt>x</tt> is
	 * a queue known to contain only strings. The following code can be used to
	 * dump the queue into a newly allocated array of <tt>String</tt>: <pre>
	 * String[] y = x.toArray(new String[0]);</pre> Note that <tt>toArray(new
	 * Object[0])</tt> is identical in function to <tt>toArray()</tt>.
	 * @param a
	 *        the array into which the elements of the queue are to be stored,
	 *        if it is big enough; otherwise, a new array of the same runtime
	 *        type is allocated for this purpose
	 * @return an array containing all of the elements in this queue
	 * @throws ArrayStoreException
	 *         if the runtime type of the specified array is not a supertype of
	 *         the runtime type of every element in this queue
	 * @throws NullPointerException
	 *         if the specified array is null
	 */
	public synchronized Object[] toArray(Object[] a)
	{
		if (a.length < count)
			a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass()
					.getComponentType(), count);

		int k = 0;
		int i = takeIndex;
		while (k < count)
		{
			a[k++] = items[i];
			i = inc(i);
		}
		if (a.length > count)
			a[count] = null;
		return a;
	}

	public synchronized String toString()
	{
		return super.toString();
	}

	/**
	 * Atomically removes all of the elements from this queue. The queue will be
	 * empty after this call returns.
	 */
	public synchronized void clear()
	{
		final Object[] items = this.items;
		int i = takeIndex;
		int k = count;
		while (k-- > 0)
		{
			items[i] = null;
			i = inc(i);
		}
		count = 0;
		putIndex = 0;
		takeIndex = 0;
		notFull.releaseAll();
	}

	/**
	 * Returns an iterator over the elements in this queue in proper sequence.
	 * The returned <tt>Iterator</tt> is a "weakly consistent" iterator that
	 * will never throw {@link ConcurrentModificationException}, and guarantees
	 * to traverse elements as they existed upon construction of the iterator,
	 * and may (but is not guaranteed to) reflect any modifications subsequent
	 * to construction.
	 * @return an iterator over the elements in this queue in proper sequence
	 */
	public Iterator iterator()
	{
		return new Itr();
	}

	/**
	 * Iterator for ArrayBlockingQueue
	 */
	private class Itr implements Iterator
	{
		/**
		 * Index of element to be returned by next, or a negative number if no
		 * such.
		 */
		private int nextIndex;

		/**
		 * nextItem holds on to item fields because once we claim that an
		 * element exists in hasNext(), we must return it in the following
		 * next() call even if it was in the process of being removed when
		 * hasNext() was called.
		 */
		private Object nextItem;

		/**
		 * Index of element returned by most recent call to next. Reset to -1 if
		 * this element is deleted by a call to remove.
		 */
		private int lastRet;

		Itr()
		{
			synchronized (SimpleQueue.this)
			{
				lastRet = -1;
				if (count == 0)
					nextIndex = -1;
				else
				{
					nextIndex = takeIndex;
					nextItem = items[takeIndex];
				}
			}
		}

		public boolean hasNext()
		{
			synchronized (SimpleQueue.this)
			{
				/*
				 * No sync. We can return true by mistake here only if this
				 * iterator passed across threads, which we don't support
				 * anyway.
				 */
				return nextIndex >= 0;
			}
		}

		/**
		 * Checks whether nextIndex is valid; if so setting nextItem. Stops
		 * iterator when either hits putIndex or sees null item.
		 */
		private void checkNext()
		{
			synchronized (SimpleQueue.this)
			{
				if (nextIndex == putIndex)
				{
					nextIndex = -1;
					nextItem = null;
				} else
				{
					nextItem = items[nextIndex];
					if (nextItem == null)
						nextIndex = -1;
				}
			}
		}

		public Object next()
		{
			synchronized (SimpleQueue.this)
			{
				if (nextIndex < 0)
					throw new NoSuchElementException();
				lastRet = nextIndex;
				Object x = nextItem;
				nextIndex = inc(nextIndex);
				checkNext();
				return x;
			}
		}

		public void remove()
		{
			synchronized (SimpleQueue.this)
			{

				int i = lastRet;
				if (i == -1)
					throw new IllegalStateException();
				lastRet = -1;

				int ti = takeIndex;
				removeAt(i);
				// back up cursor (reset to front if was first element)
				nextIndex = (i == ti) ? takeIndex : i;
				checkNext();
			}
		}
	}
}

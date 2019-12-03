package nachos.cop4610;

import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * This class defines the slave key value servers. Each individual KVServer 
 * would be a fully functioning Key-Value server. For Project 3, you would 
 * implement this class. For Project 4, you will have a Master Key-Value server 
 * and multiple of these slave Key-Value servers, each of them catering to a 
 * different part of the key namespace.
 *
 */
public class KVServer implements KeyValueInterface {
	private KVStore dataStore = null;
	private KVCache dataCache = null;
	
	private static final int MAX_KEY_SIZE = 256;
	private static final int MAX_VAL_SIZE = 256 * 1024;
	
	/**
	 * @param numSets number of sets in the data Cache.
	 */
	public KVServer(int numSets, int maxElemsPerSet) {
		dataStore = new KVStore();
		dataCache = new KVCache(numSets, maxElemsPerSet);

		AutoGrader.registerKVServer(dataStore, dataCache);
	}
	
	public void put(String key, String value) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerPutStarted(key, value);

		WriteLock cacheWriteLock = dataCache.getWriteLock(key);
		try {
			cacheWriteLock.lock();
			synchronized(dataStore) {
				try {
					dataStore.put(key, value);
					dataCache.put(key, value);
				} catch (KVException e) {
					throw new KVException(new KVMessage("resp", "IO Error"));
				}
			}
		} finally {
			// Must be called before returning
			AutoGrader.agKVServerPutFinished(key, value);
			unlock(cacheWriteLock);
		}

	}
	
	public String get (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerGetStarted(key);

		WriteLock cacheWriteLock = dataCache.getWriteLock(key);
		String result = null;
		try {
			cacheWriteLock.lock();
			result = dataCache.get(key);
			if (result == null) {
				synchronized (dataStore) {
					result = storeGet(key);
					dataCache.put(key, result);
				}
			}
		} finally {
			// Must be called before returning
			AutoGrader.agKVServerGetFinished(key);
			unlock(cacheWriteLock);
		}
		return result;
	}
	
	public void del (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerDelStarted(key);

		WriteLock cacheWriteLock = dataCache.getWriteLock(key);
		try {
			cacheWriteLock.lock();
			synchronized (dataStore) {
				storeGet(key); // will throw the right error if no key
				try {
					dataStore.del(key);
					dataCache.del(key);
				} catch (KVException e) {
					throw new KVException(new KVMessage("resp", "IO Error"));
				}
			}
		} finally {
			// Must be called before returning
			AutoGrader.agKVServerDelFinished(key);
			unlock(cacheWriteLock);
		}
	}

	public KVCache getCache() {
		return dataCache;
	}

	public KVStore getStore() {
		return dataStore;
	}
	
	private String storeGet(String key) throws KVException {
		String value;
		try {
			value = dataStore.get(key);
			if (value == null) {
				throw new KVException(new KVMessage("resp", "Does not exist"));
			}
		} catch (KVException e) {
			if (e.getMsg().getMessage().contains("does not exist in store")) {
				throw new KVException(new KVMessage("resp", "Does not exist"));
			} else {
				throw new KVException(new KVMessage("resp", "IO Error"));
			}

		}
		return value;
	}
	
	private void unlock(WriteLock lock) {
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
		}
	}
}

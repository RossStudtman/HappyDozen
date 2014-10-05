package rocks.happydozen.utility;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

/**
 * <h1>MyLruCache is basically a wrapper for a LruCache that caches bitmaps 
 * into memory.</h1>
 * 
 * <p>RetainFragment is an inner class that saves a concrete instance of
 * MyLruCache across configuration changes like device rotation.</p>
 */
@SuppressLint("NewApi")
public class MyLruCache {
	/**
	 * The memory cache for bitmaps.
	 */	
	private LruCache<String, Bitmap> mMemoryCache;	
	
	/**
	 * Constructor for creating memory cache for bitmaps.
	 */
	public MyLruCache(){	
			
			// Get amount of available memory in kilobytes.
			final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
			
			// Use 1/4th of available memory for cache.
			final int cacheSize = maxMemory / 4;	
			
			// Create cache.
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

				/**
				 * I imagine this is called internally by the LruCache code, once
				 * for each item in the cache. Default implementation returns 1 but
				 * by overriding it we return the kilobyte size of the entry; likely
				 * a running sum is kept to ensure the maxiumum size of the
				 * cache has not been eclipsed.
				 */
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					
					// Cache measured in kilobytes instead of number of items.
					return bitmap.getByteCount() / 1024;
				}
			};	
	}
	
	/**
	 * getInstance returns a {@link MyLruCache} instance. A {@link RetainFragment} is
	 * used to retain the MyLruCache across configuration changes like device rotation.
	 * 
	 * @param fm is a FragmentManager, used by RetainFragment.
	 * @return an existing MyLruCache or create a new one.
	 */
	public static MyLruCache getInstance(FragmentManager fm){
		
		// Get a RetainFragment (either a saved one or make a new one)
		final RetainFragment mRetainFRAGMENT = RetainFragment.findOrCreateRetainFragment(fm);
		
		// See if RetainedFragment holds a MyLruCache object
		MyLruCache mMemoryCache = mRetainFRAGMENT.mRetainedCACHE;
		
		// If no MyLruCache object, create one.
		if(mMemoryCache == null){
			
			mMemoryCache = new MyLruCache();
			
			// Assign newly created MyLruCache to RetainedFragment field.
			mRetainFRAGMENT.mRetainedCACHE = mMemoryCache;
		}
		
		return mMemoryCache;		
		
	}
	

	/**
	 * Add a Bitmap to the LruCache.
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		
		if (getBitmapFromMemoryCache(key) == null) {
			
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * Get a Bitmap from LruCache.
	 * 
	 * @param key is the key to the LruCache (a Map.
	 * 
	 * @return 
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}	
}

/**
 * <h1>RetainFragment preserves a {@link MyLruCache} when parent
 * activity is destroyed, as with device rotation</h1>
 * 
 * <p>RetainFragment works because it has set <code>setRetainInstance</code> 
 * to true.</p> 
 * 
 * @author Ross Studtman
 *
 */
class RetainFragment extends Fragment {

	private static final String TAG = "RetainFragment";
	
	/**
	 * Bitmap memory cache that survives device rotation (activity destruction).
	 */
	//public LruCache<String, Bitmap> mRetainedCACHE;
	public MyLruCache mRetainedCACHE;

	/**
	 * Default constructor per Fragment docs.
	 */
	public RetainFragment() {
	}

	public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		
		RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
		
		if (fragment == null) {
			
			fragment = new RetainFragment();
			fm.beginTransaction().add(fragment, TAG).commit();
		}
		
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

}

package rocks.happydozen.utility;

import java.lang.ref.WeakReference;

import rocks.happydozen.R;
import rocks.happydozen.activities.MainActivity;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

/**
 * <h1>ImageHandler handles bitmap creation in a worker thread.</h1>
 * <p>ImageHandler uses {@link MyLruCache} to manage a bitmap memory cache.</p>
 * 
 * <p>ImageHandler's primary concerns are:</p>
 * <ul>
 * 		<li>Given an image URI determine if the image is in memory cache.</li>
 * 		<li>Create bitmap from image URI and assign to View (ImageView, likely but not necessarily)</li>
 * 		<li>Bitmap creation off main thread.</li>
 * 		<li>Bitmap creation performed with limited memory availability in mind:
 * 			<ul>
 * 				<li>Use BitmapFactory.Options to decode image file's attributes.</li>
 * 				<li>Given image's W & H, compare to View's W & H, then scale down image.</li>
 * 				<li>Save scaled down image to LruCache and assign to View.</li>
 * 			</ul>
 * 		</li>
 * </ul>
 * 
 * <h2>Understanding ImageHandler on first run through:</h2> 

 * <ol>
 * 		<li>An Activity has created an instance of ImageHandler.
 * 			<ul><li>ImageHandler's constructor looks for instance of {@link MyLruCache}
 * 				<ul><li>A {@link RetainFragment} preserves MyLruCache on configuration changes.</li></ul>
 * 			</li></ul>
 * 		</li>
 * 		<li>call to loadBitmap:
 * 			<ul>
 * 				<li>Check if image URI is null, if so assign default image and return.</li>
 * 				<li>Attempt to get bitmap from memory cache.</li>
 * 				<li>If not in memory cache:
 * 					<ul>
 * 					<li>call <code>cancelPotentialWork</code>
 *	 					<ul>
 *							<li>call <code>getBitmapWorkerTask</code>
 *								<ul>
 *									<li>if View's drawable is-a {@link AsyncDrawable}, then cast
 *										the drawable as an AsyncDrawable and return the
 *										{@link BitmapWorkerTask} associated with this drawable.</li>
 *									<li>if View is not-a AsyncDrawable, return null</li>
 *								</ul>
 *							</li>
 *							
 *							<li>If there is a {@link BitmapWorkerTask} associated with the View,
 *								get the image URI stored as a field in the {@link BitmapWorkerTask} object
 *								and compare to the image URI passed to <code>loadBitmap</code>:
 *									<ul>
 *										<li>if they are the same URIs don't cancel work in progress, return <code>false</code></li>
 *										<li>if they are different URIs then cancel the task currently running and return <code>true</code></li>
 *										<li>if there is no {@link BitmapWorkerTask} return <code>true</code></li>
 *									</ul>
 *							</li>
 *						</ul>
 *						<li>If there is not a current BitmapWorkerTask running create new {@link BitmapWorkerTask}.
 *							<ul>
 *								<li>Constructor instantiates a weak reference to the ImageView.</li>
 *							</ul>
 *						</li>
 *						<li>And create a new {@link AsyncDrawable} (supplying default drawable and BitmapWorkerTask object):
 *							<ul>
 *								<li>Constructor creates a weak reference to the {@link BitmapWorkerTask} and
 *									creates a drawable from bitmap argument.</li>
 *							</ul>
 *						</li>
 *						<li>Assign {@link AsyncDrawable} to ImageView.</li>
 *						<li>Execute the {@link BitmapWorkerTask} (supplying image URI)
 *							<ul>
 *								<li><code>doInBackground</code> calls <code>decodeSampledBitmapFromUri</code>
 *									<ul>
 *										<li>Use BitmapFactory.Options to request image data.</li>
 *										<li>Send image data to <code>calculateInSampleSize</code> to determine
 *											by what factor to reduce image size.</li>
 *										<li><code>decodeFile</code> returns a bitmap of appropriate size.</li>
 *										<li>return bitmap to <code>onPostExecute</code></li>
 *									</ul>
 *								</li>
 *								<li><code>onPostExecute</code> does the following with the bitmap sent to it:
 *									<ul>
 *										<li>if this task is canceled return null.</li>
 *										<li>if the weak reference to the ImageView is not null and the bitmap is
 *											not null:
 *											<ul>
 *												<li>Add bitmap to memory cache.</li>
 *												<li>Get ImageView from weak reference.</li>
 *												<li>Get BitmapWorkerTask from weak reference</li>
 *												<li>If current task is the same as the task in the weak reference, and
 *													ImageView from weak reference is not null: <b>assign the bitmap
 *													to the ImageView</b></li>
 *											</ul>
 *										</li>
 *									</ul>
 *								</li>
 *							</ul>
 *						</li>
 * 					</li>
 * 					</ul>
 * 				</li>
 * 			</ul>
 * 		</li>
 * </ol>
 * 		
 * @author Ross Studtman
 *
 */
public class ImageHandler {
	
	private MyLruCache mMyLruCache;
	private Resources resources;

	
	public ImageHandler(Context context, FragmentManager fm){

		mMyLruCache = MyLruCache.getInstance(fm);
		
		resources = context.getResources();
	}
	
	/**
	 * Creates a scaled-down bitmap from an image URI and does so on a background thread.
	 * 
	 * @param imageUri The URI of the image; probably returned from the Provider.
	 * @param imageView The view that this bitmap should be placed into.
	 * @param widthOfView The width of the view the bitmap is placed into.
	 * @param heightOfView The height of the view the bitmap is placed into.
	 */
	public void loadBitmap(String imageUri, ImageView imageView, int widthOfView, int heightOfView) {
		
		// Attempt to get bitmap from memory.
		final String imageKey = imageUri;
		
		/*
		 * There was a bug - and I have no idea how it occurred - that resulted in the database
		 * not having a value for imageUri. This made it impossible to edit the collection
		 * because when loadBitmap() was called to load images into listviews the
		 * getBitmapFromMemoryCache() would throw a null point exception. 
		 * 
		 * So, in case that should happen again, put a default image in its place.
		 */
		if(imageUri == null){
			imageView.setImageResource(R.drawable.deleted);
			return;
		}
		
		final Bitmap bitmap = mMyLruCache.getBitmapFromMemoryCache(imageKey);
		
		// if bitmap is in memory then view now
		if(bitmap != null){
			
			imageView.setImageBitmap(bitmap);
		}
		// else no bitmap in memory so continue as before
		else{
			
			if(cancelPotentialWork(imageUri, imageView)){
				
				final BitmapWorkerTask task = new BitmapWorkerTask(imageView, widthOfView, heightOfView);
				
				final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, MainActivity.defaultBitmap, task);
				imageView.setImageDrawable(asyncDrawable);
				
				task.execute(imageUri);
				
			}// else there is already a BitmapWorkerTask underway with the same URI so do nothing.		
		}		 
	}	
	
	/**
	 * BitmapWorkerTask is a subclass of Asynctask for the purpose of loading
	 * images off of the UI thread. 
	 */
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>{
		private final WeakReference<ImageView> imageViewReference;
		private String imageUri;
		private int imageViewWidth;
		private int imageViewHeight;
		
		// Constructor.
		public BitmapWorkerTask(ImageView imageView, int width, int height){
			imageViewWidth = width;
			imageViewHeight = height;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
	
		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {			
			
			imageUri = params[0];
			
			final Bitmap bitmap =  decodeSampledBitmapFromUri(imageUri, imageViewWidth, imageViewHeight);
	
			return bitmap;
		}
	
		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			
			if(isCancelled()){
				bitmap = null;
			}
			
			if(imageViewReference != null && bitmap != null){
				
				// add this bitmap to LruCache map
				mMyLruCache.addBitmapToMemoryCache(imageUri, bitmap);
				
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask task = getBitmapWorkerTask(imageView);
				
				if(this == task && imageView != null){
					
					//Log.d("ROSS", "ImageUTILs bitmap kilobyte count: " +bitmap.getByteCount() / 1024);
					
					imageView.setImageBitmap(bitmap);
					
				}// else do nothing.
			}// else do nothing.
		}		
	}

	//////////////////////////////////////////////////////////////////////////////
	/*
	 * 		The following two methods and class:
	 * 
	 * 			cancelPotentialWork
	 * 			getBitmapWorkerTask
	 * 			AsyncDrawable
	 * 
	 * 		...are for handling CONCURRENCY issues with listviews.
	 * 		That is, when user scrolls the asynctask that is getting the image
	 * 		may not return before the "row" of the listview is actually gone; 
	 * 		and other complexities. So these three handle that mess. And provide 
	 * 		a default image while the worker thread is pulling up the down-sized image.
	 */
	//////////////////////////////////////////////////////////////////////////////
	
	/**
	 * <p>cancelPotentialWork checks to see if there is a BitmapWorkerTask already
	 * created for this particular URI and imageview; if there is try to cancel it.</p>
	 * 
	 *<p>Returning <code>true</code> informs the calling method to proceed with building
	 * a new task with the appropriate image URI.</p>
	 * 
	 * <p>return <code>true</code> if there is no task currently underway.</p>
	 * <p>return <code>true</code> if there is a task but the URI of the task and
	 * the URI passed to this method do not match. Also, cancel the task.</p> 
	 * <p>return <code>false</code> if there is a task and the URI of that task matches
	 * the URI sent to this method, i.e., a task for this URI exists.</p>
	 * 
	 * <p>return <code>false</code> if ImageView parameter is null or is not an
	 * <code>instanceof</code> AsyncDrawable. The checking of which is handled by
	 * <code>getBitmapWorkerTask(imageView)</code></p> 
	 */
	public boolean cancelPotentialWork(String bitmapUri, ImageView imageView){
		
		final BitmapWorkerTask task = getBitmapWorkerTask(imageView);
		
		if(task !=null){
			final String taskUri = task.imageUri; 
			if(taskUri != bitmapUri){
				// Cancel previous task
				task.cancel(true);
			}else{
				// The same work is already in progress
				return false;
			}
		}// else do nothing.		
		
		// No task associated with the ImageButton, or an existing task was cancelled
		return true;
	}

	/**
	 * <h1>This method answers the question "Is there a task running that is
	 * associated with this view?"</h1>
	 * 
	 * This method first looks for if the view is null and then checks to see if the 
	 * view is an instanceof an AsyncDrawable -- we need it to be an instance of an
	 * AsyncDrawable because AsyncDrawable was assigned to the view and it
	 * contains the (weak)reference to the task that spawned it.
	 * 
	 * @return a <code>WeakReference</code> for a BitmapWorkerTask if the following two
	 * are true: 1) there is an imageview, and 2) the imageView is an 
	 * <code>instanceof</code> AsyncDrawable; else return null.
	 * 
	 */
	private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
		
		if(imageView !=null){
			
			final Drawable drawable = imageView.getDrawable();
			
			if(drawable instanceof AsyncDrawable){
				
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
				
			}// else do nothing.
		}// else do nothing.
		return null;
	}
	
	/**
	 * <h1>AsyncDrawable is a sublcass of {@link BitmapDrawable}.</h1>
	 * 
	 * Creates a drawable from the Bitmap passed in as a parameter. And
	 * creates a weak reference to the task passed in as a parameter.
	 * 
	 * <p>In layman's terms: what this class does is provide a way for
	 * the ImageView to remember the last task that was assigned to it. And
	 * we do that by tucking this information into the "drawable surface"
	 * that is assigned to the ImageView. At some point this created
	 * "bitmap surface" will be assigned to the ImageView and when that is 
	 * done the ImageView will have a reference to the last task that
	 * was assigned to it.</p>
	 * 
	 * <p>Notice how clever it is to store a (weak)reference to a task
	 * within the view attempting to be populated with a bitmap image.
	 * That (weak)reference is stored into the view by extending the
	 * BitmapDrawable class and when we associate this drawable with our
	 * view then we will have effectively stored a (weak)reference to
	 * our task within our view. What this allows us to do is check if
	 * the view is related to the (async)task that has completed.</p>
	 * 
	 * @param res Resources object that can pass display metrics of the device
	 * to the bitmap being created.
	 * 
	 * @param bitmap is the bitmap to be drawn.
	 * 
	 * @param task is a BitmapWorkerTask that is passed in so that a 
	 * <code>WeakReference</code> can be attributed to it.
	 * 
	 */
	class AsyncDrawable extends BitmapDrawable{
		private final WeakReference<BitmapWorkerTask> bitmapWorkerReference;
		
		/*
		 * Constructor
		 */
		public AsyncDrawable( Resources res, Bitmap bitmap, BitmapWorkerTask task){
			super(res, bitmap);
			bitmapWorkerReference = new WeakReference<BitmapWorkerTask>(task);
		}
		
		/**
		 * Returns a weak reference to a BitmapWorkerTask.
		 */
		public BitmapWorkerTask getBitmapWorkerTask(){
			return bitmapWorkerReference.get();
		}
		
	}
	
	/**
	 * <h1>Create a scaled down bitmap of an image.</h1>
	 * 
	 * @param path is the Uri string obtained from the Provider.
	 * @param viewWidth is the width of the ImageView.
	 * @param viewHeight is the height of the ImageView.
	 * @return a scaled down bitmap of the user's gallery image.
	 */
	public Bitmap decodeSampledBitmapFromUri(String path, int viewWidth, int viewHeight){
		
		// Get the image dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		// Get scaling factor
		options.inSampleSize = calculateInSampleSize(options, viewWidth, viewHeight);
		
		
		// Decode bitmap with scaling factor
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(path, options);
	}
	
	/**
	 * <h1>Calculate the int value to be used for down-scaling an image.</h1>
	 * 
	 * @param options
	 * @param viewWidth is the ImageView width.
	 * @param viewHeight is the ImageView height.
	 * @return an int representing the down-scale factor.
	 */
	public int calculateInSampleSize(BitmapFactory.Options options, int viewWidth, int viewHeight){
		
		// Get size of image.
		final int imageHeight = options.outHeight;
		final int imageWidth = options.outWidth;
		int inSampleSize = 1;
		
		// Commented out to test the code immediately below.
		
		if(imageHeight > viewHeight || imageWidth > viewWidth){
			// Calculate ratios of height and width.
			final int heightRatio = Math.round((float) imageHeight / (float)viewHeight);
			final int widthRatio = Math.round((float) imageWidth / (float)viewWidth);
			
			// Select the larger of the above two ratios; to save memory.
			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		}

		
		// Alternate testing because documentation at http://developer.android.com/reference/android/graphics/BitmapFactory.Options.html#inSampleSize
		// states: Note: the decoder uses a final value based on powers of 2, any other value will be rounded down to the nearest power of 2.
		// Hence why not produce code that produces only powers of 2 for inSampleSize? (as the documentation has).
		// So try this:
		
/*
	    if (imageHeight > viewHeight || imageWidth > viewWidth) {

	    	// Do inSampleSize calculations based on a half-sized image.
	    	// Ensures width or height of image is larger than view's corresponding dimension. 
	        final int halfImageHeight = imageHeight / 2;
	        final int halfImageWidth = imageWidth / 2;

	        // Calculate the largest inSampleSize (that is power of 2) for whichever is greater: image width or image height.
	        while ((halfImageHeight / inSampleSize) > viewHeight || (halfImageWidth / inSampleSize) > viewWidth) {
	            inSampleSize *= 2;
	        }
	    }
*/  		
		
	   // Log.d("ROSS", "calculateInSampleSize size: " +inSampleSize);
		return inSampleSize;
	}
	
	/**
	 * Currently does nothing. 
	 * ...but, maybe it should? If the activity ends, is the reference to the
	 * Cache gone? Maybe instead of waiting for garbage collection you just
	 * go ahead and flush the cache when the activity stops? That seems wise.
	 * (...but unimplemented at this time).
	 * 
	 */
	public void flushLruCache(){
		if(mMyLruCache != null){

		}
	}
	
}

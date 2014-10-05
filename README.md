#<a name="top"></a>**Happy Dozen**

>Organizes Gallery photos into collections containing user’s favorite 12 images.<hr/>

*A non-trivial app containing Activities, Fragments, ViewPager, Content Provider backed by SQLite database, Loaders, subclassed Adapters, AsyncTasks, bitmap creation, and more.*

##Table of Contents

* [Marketing](#marketing)
* [Beta](#beta)
* [Bugs](#bugs)
* [Future Improvements](#improvements)
* [Notable Implementations: list](#implementations)
* [Activities](#activities)
* [Fragments](#fragments)
* [Adapters](#adapters)
* [Other](#other)




##<a name="marketing"></a>MARKETING
([top](#top))

No more wasted time looking for favorite photos. They are now easy and simple to find. 

How does Happy Dozen bring the joy? Happy Dozen organizes Gallery photos into collections containing the user’s favorite 12 images. By politely restricting collections of images/photos to a dozen (12) you never have to wonder where your favorites are. 

Happy Dozen does not change or alter your images in any way. Happy Dozen respects your device's memory. Photos and images remain where the device stores them. 

**Happy Dozen is simple but effective:**

  * You give your collection a name and add images (from Android's Gallery) to the collection. 
  * Give each image a simple title. 
  * Done! Save your collection and it is added to the list displayed on app start up. 

**Need to edit a collection?**

*	Press and hold on a collection displayed at app start up. 
*	You can now edit the collection name, add an image (if fewer than 12), or delete the entire collection. 
*	Select an image from the list and you can change the image or its title, or delete it from the collection.

That's it. There's not much to it because there doesn't need to be. Save your favorites and make them easy to find. 

Happy Dozen supports selecting images from Android's *Gallery*. In future updates we hope to broaden where images can be pulled from.

##<a name="beta"></a>BETA
([top](#top))

`Boolean first-person = true`

One of the most important things I took away from this project: upfront planning is better than rear-end refactoring. I think every developer needs one of these kinds of projects under their belt, a project they have had to come back and retool or refactor in some way, a project they have had to try to understand after 6 months of not seeing it. You do that, you see flaws. But I cannot be too harsh, this project was a first for me in many ways - my first fairly complex solo project, my first Android, my first using several of the coding techniques learned along the way. When I started I didn't know what an Observer pattern was! 

Concurrent to this build process was a learning curve. The code is heavily commented more in a learning style than a professional style. 

I like to take pride in the work I do. I am proud to have created my first app and that it works. Also, that it has a genuine reason for existing, it fulfills a need. But the app falls short of having a flawless GUI and having crisp, clean, beautiful code that makes it a joy to read and a breeze to understand.

Fundamentally I am a "user comes first" thinker. Code is generally written to be used by a user; if the user has trouble using it then it has partially failed its reason for existence. This app could see improvements to fulfill this objective.

##<a name="bugs"></a>BUGS
([top](#top))

In EditActivity, but not AddActivity, if user attempts to add an image the 
implicit intent to allow the user to select an image opens. If device is
rotated (a configuration change) during the implicit intent the image does
is not shown in the AddImageFragment's ImageView and when user inputs a title
and hits save button (checkmark icon) the app will crash. However, and oddly,
if the phone is rotated twice (or any even number) the app will not crash.

Time constraints didn't allow this bug to be fixed. However, the following has
been discovered:

* The bug is related to the LoaderManager calling onLoaderReset, which
nulls out the Cursor. 
  * It is the null Cursor causing the problem.
	
*Could the Cursor be saved with device rotation?* Suppose so, but that
would seem to violate the reason for a Cursor Manager: to handle the
opening and closing and saving of Cursors. And why the oddity of even vs. odd rotations? 
	
##<a name="improvements"></a>FUTURE IMPROVEMENTS:
([top](#top))

- UI improvements (watch users use app, note things that can be improved).
- Allow user to send an entire collection to Facebook (etc.).
- Create a more robust menu bar system throughout app.
- Dynamically resize ImageViews throughout app so looks good on all devices.
- Create different layouts for radically different sized devices, ie phones and tablets.
- Organize Strings.xml (this would have best been accomplished during app creation).
- Implement preferences to allow user to use a password-image on the entire app and/or
for each collection.
- For larger devices, in ListView that displays at app start up, in a listview row, show tiny thumbnails of images from the collection.

##<a name="implementations"></a>NOTABLE IMPLEMENTATIONS:
([top](#top))

- Multiple activities
- Multiple fragments
- ListFragments
- Dynamic creation and swapping of fragments
- Bean for seeding adapter
- AsyncTasks
- Fragment interfaces for MVC, Observer Pattern
- Subclassed BaseAdapter
- Subclassed CursorAdapter
- SimpleCursorAdapter
- Holder pattern used in subclassed Adapters.
- SQLite database
- Subclassed ContentProvider
- Loader and LoaderManager
- ViewPager & Subclass FragmentStatePagerAdapter
- URI to Bitmap creation off UI thread
- BitmapFactory.Options for resizing bitmaps for memory management
- Custom LruCache - hold recent images in memory cache.	
- XML custom colors
- XML drawables, icons for each pixel density
- XML custom styles
- Alternate XML layout for landscape (EditActivity only)


##<a name="activities"></a>ACTIVITIES:
([top](#top))

- AddActivity
- AddImage
- EditActivity
- GalleryActivity
- MainActivity

###**AddActivity:** Add a collection of images.

	Uses the following custom fragments:
		- AddActivity_Frag1
		- AddActivity_Frag2 - dynamically added
		- AddActivity_Frag3 - dynamically added
		- ListViewFragment (extends ListFragment)
			- custom adapter: AddCollectionAdapter (extends BaseAdapter)
		- RetainedFragment - non-GUI fragment
			
		AddActivity_Frag1, 2, 3: Button clicks routed through activity.
		
	Checks savedInstanceState for dynamically added fragments. 
			
	Nested Class: ImageBean - an object for holding image details.
	
	RetainedFragment - retains a List<ImageBean> across configuration changes. 
	
	AsyncTask to bulk insert image details to custom content provider (CollectionsContentProvider).
	
	Uses custom ImageHandler class for handling bitmap creation and memory management.
	
	Starts an AddImage activity (for result) when user wants to select an image from the Gallery.
		- onResult: use intent data returned to build an ImageBean object, then store in List<ImageBean>
		  so when activity finishes a bulk insert can be used to store the details of each object
		  in the List.
	
	
###**AddImage:** Add a single image to a collection.

	Basically a wrapper for AddImageFragment fragment. 
	

###**EditActivity:** Edit collection and image name; change images; delete collection or image.

	Results of user's edits dynamically shown in framents and activity's TextViews and ImageViews.
	
	Uses the following custom fragments:
		- ListViewFragment
			- custom adapter: EditCursorAdapter (extends CursorAdapter)
			- this fragment hard coded into XML via <fragment> tag.			
		- EditCollectionFragment
		- EditImageFragment - dynamically created
		
	Dynamically creates AddImage activity.
	
	Uses a Loader and loader manager callbacks.
		- onCreateLoader queries custom Provider (CollectionContentProvider) via CursorLoader.
	
	Implements custom callbacks for fragment listeners.
	
	Uses AsyncTask to update Provider.
	
	Uses custom ImageHandler class for adding bitmaps to LruCache (ListView images.)
	
	Uses an alternate XML layout (in layout-land folder) for when device is in landscape.
	
	
###**GalleryActivity:** Displays a collection of images.

	Activity is viewed full screen.
		- hides title bar
		- hides status bar
	
	Uses the following custom fragments:
		- ImageFragment 
	
	Uses custom ImageHandler for creating bitmaps and managing memory.
		- ImageFragments, through activity's Imagehandler, check if image has been previously created.
	
	On creation uses AsyncTask to query provider for cursor that holds collection's images. 
	
	Use ViewPager to show ImageFragments.
	
	Implement Loader & LoaderManager & loader callbacks.
	
	Subclass FragmentStatePagerAdapter.
	
	
	
###**MainActivity:** Displays list of collections; user can add or edit collections

	Responsible for starting the following activities:
		
		- AddActivity
		- EditActivity
		- GalleryActivity

	Uses XML ListView.
	
	ListView employs "long click" (registerForContextMenu); sets divider height and empty view.
		
		- Uses SimpleCursorAdapter
		
			- Layout defined in XML (main_lv_row.xml)
			- Uses Provider to obtain title of collection and # of images in collection.
	
	Uses a Loader and loader manager callbacks to manage data supplied to SimpleCursorAdapter.
	
		- onCreateLoader queries Provider via CursorLoader.
		
	Creates Options Menu (Add Collection).
	Creates Context Menu (see ListView note above).
	
	

		
		
##<a name="fragments"></a>FRAGMENTS:
([top](#top))

- AddActivity_Frag1, AddActivity_Frag2, AddActivity_Frag3
- AddImageFragment
- EditCollectionsFragment
- EditImageFragment
- ImageFragment
- ListViewFragment


###**AddActivity_Frag1:** Used in AddActivity

	Uses an AsyncTask to determine if user's given title already exists
	in the database.
	
###AddActivity_Frag2: Used in AddActivity
###AddActivity_Frag3: Used in AddActivity

	The only thing mildly interesting about these is their Buttons send their click
	event to the activity. And, perhaps, in how their parent activity's onCreate
	determines if they have been saved from device rotation (activity destroyed and
	recreated).

	
###**AddImageFragment:** Used in AddImage activity.

	Allows user to add an image and set a title for the image.
	Then creates an intent, stores the image URI and title,
	sends the inent via setResult back to the activity that
	started AddImage activity (either AddActivity or EditActivity).

	Uses an implcit Intent to allow user to browse for an image.
	
		- Could not find a technique for only allowing the user to use Gallery.
		  ("Photos" is also an option given the user. Happy Dozen, at this time,
		  is not coded to handle obtaining images from outside the device).
		  
	onActivityResult:
	
		- To determine if the image was selected from the Gallery uses:
		
			if(galleryImageUri.toString().startsWith("content://media")){...}			
			else, inform user Happy Dozen currently works with Gallery images:
			
				AlertDialogBuilder --> set title, text, view, and PositiveButton.
			
		- Use the returned Intent to obtain URI for selected image.
			
	Uses BitmapFactory to decode selected image into ImageView.
	
	Use EditText to obtain user's title.
	
	Create Intent and return image details via setResult().
		- AddImage activity (which created this fragment) was 
		started via startActivityForResult().
		  

###**EditCollectionsFragment:** Used in EditActivity

	Programmatically added to EditActivity's upper GUI.
	
	Inner/Nested class: DeletCollectionTask (extends AsyncTask)
	
	3 Buttons allow user to:
		
		- Add an image to the collection
		
			- informs activity to create an AddImageFragment
			
		- Change the collection's title
		
			- Uses an AlertDialogBuilder: the setPositiveButton creates
			  an anonymous onClickListener class. The onClick listener
			  creates an AsyncTask to update the database through a
			  ContentResolver.
			  
		- Delete the collection
		
			- Uses AlertDialogBuilder, setPositiveButton uses anonymous class
			  to set onClick listener to create a DeleteCollectionTask to 
			  asynchronously delete the collection from the database.
			  
				- onPostExecute finishes the parent EditActivity directly.
		
	

###**EditImageFragment:** Used in EditActivity

	3 Buttons, 1 ImageButton allow user to:
	
		- Delete image
		- Change image
		- Change image name
		- Finish editing

	Delete image: uses AlertDialogBuilder.
	
		- setPositiveButton uses anonymous onClick listener, which uses
		  an AsyncTask to getContentResolver to delete image from collection.
		  
		- onPostExecute popBackStack().
		
	Change image: user clicked ImageButton
	
		- uses implicit intent to allow user to select an image.
	
		- onActivityResult: only accepts images selected from Gallery.
		
			- uses AsyncTask to update image URI in database, via Provider.
			
	Change image title: use AsyncTask to update Provider.
	
	Finish - terminates the fragment.
			
	

###**ImageFragment:** Used in GallerActivity's ViewPager.

	Uses a ViewTreeObserver to set a onPreDraw listener that obtains the height
	and width of the ImageView. These dimensions are then passed to GallerActivity's
	compositionally added ImageHandler for managing the creation and storage of
	bitmaps off the UI thread.

	
###**ListViewFragment:** extends ListFragment, used by AddActivity and EditActivity.

	Creates a listener (ListFragListener).
	
	onListItemClick uses listener callback to send a cursor to the listening activity.
	
		- EditActivity imlements the listener
		
			EditActivity receives a Cursor for the item clicked.
			(EditActivity assigns a CursorAdapter (EditCursorAdapter) to this ListFragment.)



##<a name="adapters"></a>**ADAPTERS**
([top](#top))

- SimpleCursorAdapter: MainActivity's ListView		
- BaseAdapter (AddCollectionAdapter): AddActivity's ListViewFragment		
- CursorAdapter (EditCursorAdapter): EditActivity's ListViewFragment        
- FragmentStatePageAdapt: GalleryActivity
	
####**AddCollectionAdapter - custom BaseAdapter**

	Uses Holder Pattern.
	Uses activity's custom object collection (List<ImageBean>) to load XML ImageView and TextView with data.
	Uses activity's compositionally added ImageHandler for creating bitmaps off the UI thread.
	
	
####**EditCursorAdapter - custom CursorAdapter**

	Uses activity's compositionally added ImageHandler for creating and managing bitmaps.
	
	Future improvements:
	
		- Assess whether this adapter is necessary
	
##<a name="other"></a>OTHER
([top](#top))


###**ImageHandler:** Handles creating bitmaps off the main thread.

	Compositionally adds MyLruCache for saving bitmaps to memory.
	
	Two inner classes:
	
		- BitmapWorkerTask (extends AsyncTask)
		
			- Holds a weak reference to ImageView
			- performs bitmap calculations off main thread.
			
		- AsyncDrawable (extends BitmapDrawable)
		
			- Holds a weak reference to a BitmapWorkerTask.
			- Creates a default bitmap image that can be assigned to the ImageView.
			
	Uses BitmapFactor.Options to obtain image details that can be used to scale down
	the image to an appropriate size given memory constraints of app.
	
	Uses AsyncTask
			creating bitmaps off UI thread,
			using LruCache for saving bitmaps to memory
			
			
###**MyLruCache:**

	Obtain memory allocation of app.
	
	Create LruCache for saving bitmaps to memory.
	
	Inner class: 
	
		- RetainFragment 

	
###**CollectionsDatabaseHelper - sublcass of SQLiteOpenHelper**

	onCreate and onUpgrade call static methods of
	the table-to-be-created's class.
	

###**CollectionsTable**

	The philosophy was adopted that each table should be its
	own class.
	
	The sublcassed SQLiteOpenHelper will call through to 
	CollectionTable's onCreate and onUpgrade methods.
	
	onUpgrade uses SQL to:
	
		- create a backup table
		- create a new table
		- copy old data into new table
		- drop backup table.
		
		
###**CollectionsContentProvider - subclass of ContentProvider**

	UriMatcher matches URI's for queries asking for:
	
		- A single image
		- A collection of images
		- All of the collections, grouped by collection name, 
		  and number of images in the collection.
		  
	Implements ContentProvider methods:
	
		- query
		- delete
		- insert
		- update
		  
		  
	


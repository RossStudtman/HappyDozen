package rocks.happydozen.interfaces;

/**
 * <h1>A simple interface for allowing fragments to communicate
 * with their Activities.</h1>
 * 
 * <p>This listener is used by AddActivity_Frag1, AddActivity_Frag2;
 * and is implemented by AddActivity.</p>
 * 
 * 
 * @author Ross Studtman
 *
 */
public interface UpperFragmentsListener {
	public void upperFragmentListener(String fragTag);
}

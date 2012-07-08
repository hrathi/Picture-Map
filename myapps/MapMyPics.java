package myapps;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.ui.UiApplication;

public class MapMyPics extends UiApplication {
	
	public static void main(String[] args) 
	{
		if (args != null && args.length > 0)
        	{
	            if (args[0].equals("startup"))
	            {
	                // Register an ApplicationMenuItem on device startup.
	                ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();
	                ApplicationDescriptor ad_startup = ApplicationDescriptor.currentApplicationDescriptor();
	                ApplicationDescriptor ad_gui = new ApplicationDescriptor(ad_startup , "View by Location", new String[]{"gui"});
	                amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_BROWSE , new MapsMenuItem() , ad_gui);                
	            }
	            else if (args[0].equals("gui"))
	            {               
	                // App was invoked by our ApplicationMenuItem. Call default
	                // constructor for GUI version of the application.
	                MapMyPics app = new MapMyPics();
	                
	                // Make the currently running thread the application's event
	                // dispatch thread and begin processing events.
	                app.enterEventDispatcher();                
	            }
	        }
	}
	
	private static class MapsMenuItem extends ApplicationMenuItem
	{
		// Constructor
	        private MapsMenuItem()
	        {
	            // Create a new ApplicationMenuItem instance with relative menu 
	            // position of 20. Lower numbers correspond to higher placement 
	            // in the menu.
	            super(0x100000);
	        }
	        
	        /**
	         * Returns the name to display in a menu.
	         * @return The name to display.
	         */
	        public String toString()
	        {
	            return "View by Location";
	        }        
	        
	        /**         
	         * Views the map in a MapMenuItemScreen.
	         * @see ApplicationMenuItem#run(Object)
	         */
	        public Object run(Object context)
	        {
	            UiApplication app = UiApplication.getUiApplication();
	            app.pushScreen( new PicturesMapScreen() );
	            app.requestForeground();
	    		
	    		return null;
	        }	
	}
}
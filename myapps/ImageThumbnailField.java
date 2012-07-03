package myapps;

import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.rim.device.api.content.BlackBerryContentHandler;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;

public class ImageThumbnailField extends BitmapField {
	String _path;
	
	public ImageThumbnailField(Bitmap b, String path) {
		super(b, Field.FOCUSABLE | Field.HIGHLIGHT_FOCUS | Field.HIGHLIGHT_SELECT );
		//setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
		setPadding(new XYEdges(2,2,2,2));
		
		_path = path;
	}
	
    protected boolean navigationClick(int status, int time) 
    {
    	try {
    		Invocation media = new Invocation(_path);
    		media.setResponseRequired(false);
    		media.setID(BlackBerryContentHandler.ID_MEDIA_CONTENT_HANDLER);
    		media.setArgs(new String[]{BlackBerryContentHandler.MEDIA_ARGUMENT_VIEW_PICTURES});
    		Registry.getRegistry("myapps.ImageThumbnailField").invoke(media);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return true;
    }
    
    public boolean isSelectable() {
    	return true;
    }
}

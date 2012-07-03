package myapps;

import net.rim.device.api.lbs.maps.model.MapPoint;
import net.rim.device.api.lbs.maps.model.Mappable;
import net.rim.device.api.lbs.maps.ui.MapAction;
import net.rim.device.api.ui.XYPoint;

public class ImageMapAction extends MapAction {
	private boolean _panMode = false;
	
	public void setPanMode(boolean value) {
		_panMode = value;
	}
	
	protected boolean performSetCenter(MapPoint newCenter) {
		_panMode = true;
		return super.performSetCenter(newCenter);
	}
	
	protected boolean allowSetFocusMappable(final Mappable target) {
    	if( _panMode ) { 
    		_panMode = false;
    		return false ;	
    	}
    	return super.allowSetFocusMappable(target);	
	}
	
    protected boolean allowSetFocusMappableByPoint( final XYPoint target ) {
    	if( _panMode ) { 
    		_panMode = false;
    		return false ;	
    	}
    	return super.allowSetFocusMappableByPoint(target);
    }
    
    protected boolean allowSetFocusMappableByPoint( final MapPoint target ) {
    	if( _panMode ) { 
    		_panMode = false;
    		return false ;	
    	}
    	return super.allowSetFocusMappable(target);
    }

	
    protected boolean allowNavigateNextPrev(boolean forward) {
    	return true;
    }
    
	protected boolean performNavigateNext() {
		return super.performNavigateNext();
	}
	
}

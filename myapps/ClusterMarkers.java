package myapps;

import net.rim.device.api.lbs.maps.LatLonRectangle;
import net.rim.device.api.lbs.maps.model.DynamicMappable;
import net.rim.device.api.lbs.maps.model.MappableChangeEvent;
import net.rim.device.api.lbs.maps.model.MappableEventManager;
import net.rim.device.api.lbs.maps.model.geospatial.GsElement;
import net.rim.device.api.lbs.maps.model.geospatial.GsImage;
import net.rim.device.api.lbs.maps.model.geospatial.GsRoot;
import net.rim.device.api.ui.XYPoint;

public class ClusterMarkers extends GsImage implements DynamicMappable {
	public static final int SUPER_CLUSTER_MIN_VALUE = 200;
	GsRoot _markers; 
	GsImage _firstMarker;
	public MappableEventManager _eventManager;
	GsImage _oldState;
	
	
	public ClusterMarkers(double lat, double lon) {
		super();
		_markers = new GsRoot();
		setLat(lat);
		setLon(lon);
		setAnchorOffset(new XYPoint(15,35));
		_eventManager = new MappableEventManager();
		_oldState = new GsImage();
	}
	
	public void addElement(GsImage m) {
		_markers.addElement(m);
		
		if( _markers.size() == 1 ) {
			_firstMarker = m;
		}
	}
	
	public String getFirstThumbnailUri() {
		if( _firstMarker != null ) {
			return _firstMarker.getName();
		}
		return "";
	}
	
	public GsElement elementAt(int index) {
		return (GsElement)_markers.getElementAt(index);
	}
	
	public LatLonRectangle getBoundingBox() {
		return _firstMarker.getBoundingBox();
	}
	
	public boolean isClusterable() {
		return false;
	}
	
	public String getName() {
		if( _markers.size() >= SUPER_CLUSTER_MIN_VALUE ) {
			return _markers.size() + " Photos";
		}
		return null;
	}
	
	public String getDescription() {
		if( _markers.size() > 0 ) {
			return _markers.getElementAt(0).getDescription();
		}
		return "";
	}
	
	public int getSize() {
		return _markers.size();
	}
	
	public XYPoint getAnchorOffset() {
		return super.getAnchorOffset();
	}
	
	public boolean isSuperCluster() {
		return _markers.size() >= SUPER_CLUSTER_MIN_VALUE;
	}

	public MappableEventManager getEventManager() {
		return _eventManager;
	}
	
	public GsImage getFirstMarker() {
		return _firstMarker;
	}
	
	public GsRoot getMarkers() {
		return _markers;
	}
	
	public void setIconUri(String iconUri) {
		_oldState.setIconUri(getIconUri());
		super.setIconUri(iconUri);
	}
	
	public void update() {
		MappableChangeEvent event = new MappableChangeEvent();
        event.setOldState(_oldState);
        event.setNewState(this);
        _eventManager.triggerEvent(event);
	}
}

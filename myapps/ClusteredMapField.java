package myapps;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.lbs.maps.LatLonRectangle;
import net.rim.device.api.lbs.maps.MapDimensions;
import net.rim.device.api.lbs.maps.model.MappableChangeEvent;
import net.rim.device.api.lbs.maps.model.geospatial.GsImage;
import net.rim.device.api.lbs.maps.ui.MapField;
import net.rim.device.api.lbs.maps.view.Style;
import net.rim.device.api.lbs.maps.view.StyleSet;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;

public class ClusteredMapField extends MapField {
	Vector _gsFolder = new Vector();
	StyleSet _styles;
	
	public ClusteredMapField() {
		super();
		_styles = getDrawingStyles();
		setDimensions(new MapDimensions(Display.getWidth(), Display.getHeight()));
	}
	
	public void updateGsFolder(GsImage a) {
		MapDimensions currentDimensions = getDimensions();
		
		// TODO: we should really update mappables in the current dimensions
		//if( a.getBoundingBox().intersects(currentDimensions.getBoundingBox())) {
			getModel().add(a, "all", false);
			setOuterBox(a);
			
			for( Enumeration e = _gsFolder.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if( o instanceof ClusterMarkers ) {
					ClusterMarkers cluster = (ClusterMarkers)o;
					LatLonRectangle c = cluster.getBoundingBox();
					if( c.intersects(a.getBoundingBox()) ) {
						cluster.addElement(a);
						return;
					}
				}
			}
			
			ClusterMarkers cm = new ClusterMarkers(a.getLat(), a.getLon());
			cm.addElement(a);
			getModel().add(cm, "lead", true);
			_gsFolder.addElement(cm);
		//}
	}
	
	private void setOuterBox(GsImage g) {
		LatLonRectangle gRect = g.getBoundingBox();
		double expandedDim = (getDimensions().getBoundingBox().getWidth()/100000.0)/20.0; 
		gRect.setLeft(g.getLon() - expandedDim);
		gRect.setBottom(g.getLat() - expandedDim);
		gRect.setRight(g.getLon() + expandedDim);
		gRect.setTop(g.getLat() + expandedDim);
		
		/*
		MapPoint[] pts = new MapPoint[5];
		pts[0] = new MapPoint(gRect.getTop(), gRect.getLeft());
		pts[1] = new MapPoint(gRect.getTop(), gRect.getRight());
		pts[2] = new MapPoint(gRect.getBottom(), gRect.getRight());
		pts[3] = new MapPoint(gRect.getBottom(), gRect.getLeft());
		pts[4] = new MapPoint(gRect.getTop(), gRect.getLeft());

		MapPolyLine line = new MapPolyLine(pts) {
			public boolean isClusterable() {
				return false;
			}
		};
		
		getModel().add(line, "line", true);
		*/
	}

	public void eventOccurred(MappableChangeEvent arg0) {
		// TODO Auto-generated method stub
		return;	
	}

	public void resetPoints() {
		_gsFolder = new Vector();
		getModel().removeAll();
	}	
	
	public void setStyle() {
		Style classStyle = new Style();
		classStyle.setLabelFillColor( Color.BLACK );
		classStyle.setLabelFontColor( Color.WHITE );
		classStyle.setEdgeColor(Color.RED);
		classStyle.setEdgeSize(3);
		_styles.addClassBasedStyle( ClusterMarkers.class, classStyle );
	}
	
	public int getPreferredHeight() {
		if( getScreen() != null ) {
			return getScreen().getVisibleHeight();
		}
		return super.getPreferredHeight();
    }

    public int getPreferredWidth() {
    	if( getScreen() != null ) {
    		return getScreen().getVisibleWidth();
    	}
    	return super.getPreferredWidth();
    }
    
	public void updateClusterCounters() {
		for(int i = _gsFolder.size() - 1; i >= 0 ; i--) {
			ClusterMarkers cm = (ClusterMarkers)_gsFolder.elementAt(i);
			int clusterSize = cm.getSize();
			if( clusterSize >= ClusterMarkers.SUPER_CLUSTER_MIN_VALUE ) {
				cm.setIconUri("res://img/zoom.png");
				cm.setThumbnail(PicturesMapScreen.getScaledBitmapImage(cm.getFirstThumbnailUri(), 8, 60, 60));
				cm.setName(cm.getName());
			}
			else {
				cm.setIconUri("res://img/red/number_" + clusterSize + ".png");
			}
		}
	}
}



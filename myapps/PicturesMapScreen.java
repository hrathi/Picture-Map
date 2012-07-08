package myapps;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.lbs.maps.MapConstants;
import net.rim.device.api.lbs.maps.model.Mappable;
import net.rim.device.api.lbs.maps.ui.MapAction;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.input.InputSettings;
import net.rim.device.api.ui.input.TouchscreenSettings;
import net.rim.device.api.util.SimpleSortingVector;

class PicturesMapScreen extends FullScreen implements FieldChangeListener {
	ClusteredMapField _mapField;
	ImageMapAction _newActions;
	public SimpleSortingVector _geoImageVector;
	Screen _progressScreen;

	public PicturesMapScreen() {
		super(Screen.DEFAULT_CLOSE | Screen.DEFAULT_MENU | Manager.NO_VERTICAL_SCROLL) ;
		_newActions = new ImageMapAction();
		_geoImageVector = new SimpleSortingVector();
		//_geoImageVector.setSortComparator(new MappableComparator());
		
		_mapField = new ClusteredMapField();
		_mapField.addChangeListener(this);
		_mapField.setAutoUpdate(true);
		_mapField.setAction(_newActions);		
		_mapField.getAction().disableOperationMode( MapConstants.MODE_SHARED_FOCUS );
		_mapField.setStyle();
		
		// Activate pinch gesturing
		if (Touchscreen.isSupported()) {
			InputSettings is = TouchscreenSettings.createEmptySet();
			is.set(TouchscreenSettings.DETECT_PINCH, 1);
			this.addInputSettings(is);
		}
		
		add(_mapField);
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {			
			public void run() {
				UiApplication.getUiApplication().pushScreen(new ProgressIndicatorScreen(PicturesMapScreen.this));
			}
		});
	}
	
	public void setImageVector(SimpleSortingVector vector) {
		_geoImageVector = vector;
		update();
	}
	
	boolean superClusterDown = false;
	ClusterMarkers _upMarker = null;
	
	public void fieldChanged(Field field, int actionId) {
		switch (actionId)	 {
		case MapAction.ACTION_FOCUSED_CHANGE:
		{
			if( field == _mapField ) {
				Mappable m = _mapField.getFocusedMappable();
				if( m instanceof ClusterMarkers ) {
					_upMarker = (ClusterMarkers)m;
					if( !_upMarker.isSuperCluster() ) {
			            String uri = _upMarker.getIconUri();
			            _upMarker.setIconUri("res://img/green" + uri.substring(uri.lastIndexOf('/')));
			            _upMarker.update();
		            }
		            
		            if( superClusterDown ) {
	    				superClusterDown = false;
	    				UiApplication.getUiApplication().pushScreen(new PictureViewerScreen(_upMarker._markers));
	    			}
		            else if( _upMarker.isSuperCluster() ) {
		            	superClusterDown = true;
		            }	
				}
			}
			break;
		}
		case MapAction.ACTION_ZOOM_CHANGE:
			update();
			break;
		}
	}
	
	
	protected final boolean touchEvent(TouchEvent message) {
		if( message.getEvent() == TouchEvent.UP ) {
			if( processUnclick(true) ) 
				return true;
		}
		
		return super.touchEvent(message);
	}
	
	protected boolean navigationUnclick(int status, int time) {
		processUnclick(true);
		return true;
	}
	
	
	
	private boolean processUnclick(final boolean invokeScreen) {
		if( _upMarker != null && !_upMarker.isSuperCluster() ) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				
				public void run() {
					if( _upMarker != null ) {
						String uri = _upMarker.getIconUri();
			            _upMarker.setIconUri("res://img/red" + uri.substring(uri.lastIndexOf('/')));
			            _upMarker.update();
			            _mapField.update();
			            UiApplication.getUiApplication().pushScreen(new PictureViewerScreen(_upMarker._markers));
			            _upMarker = null;
					}
				}
			}, 1500, false);
			
			return true;
		}
		return false;
	}
		
	
	public static Bitmap getScaledBitmapImage(String imagePath, int scaleFactor, int width, int height) {
		FileConnection connection=null;
        byte[] byteArray=null;
        Bitmap bitmap=null;
        try
        {
            connection=(FileConnection)Connector.open(imagePath);
            if(connection.exists())
            {
                byteArray=new byte[(int)connection.fileSize()];
                InputStream inputStream=connection.openInputStream();
                inputStream.read(byteArray);
                inputStream.close();
                bitmap = new Bitmap(width, height);
                Bitmap b = Bitmap.createBitmapFromBytes(byteArray, 0, -1, scaleFactor);
                b.scaleInto(bitmap, Bitmap.FILTER_BILINEAR, Bitmap.SCALE_TO_FILL);
            }
            connection.close();
        }
        catch(Exception e) {
            System.out.println("Exception "+e.toString());
        }
        return bitmap;
		
	}

   private void update() {
	   _mapField.resetPoints();
	   
	   int mapPoints = _geoImageVector.size();
	   for (int i = 0; i < mapPoints; i++) {
		   GeoImage a = (GeoImage)_geoImageVector.elementAt(i);
		   _mapField.updateGsFolder(a);
	   }	
	   
	   _mapField.updateClusterCounters();
    }
}

package myapps;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.location.Coordinates;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.progressindicator.ProgressIndicatorController;
import net.rim.device.api.ui.component.progressindicator.ProgressIndicatorModel;
import net.rim.device.api.ui.component.progressindicator.ProgressIndicatorView;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.Persistable;
import net.rim.device.api.util.SimpleSortingVector;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.wrapper.File;

class ProgressIndicatorScreen extends PopupScreen {
	ProgressIndicatorView view = new ProgressIndicatorView(0);
	ProgressIndicatorModel model = new ProgressIndicatorModel(1, 100, 1);
	ProgressIndicatorController controller = new ProgressIndicatorController();

	ProgressThread _progressThread;
	private Vector _jpegFileNames;
	private SimpleSortingVector _imgVector;
	private PicturesMapScreen _mapScreen;
	private PersistentObject _store;
	private Hashtable _coordinates;
	private LabelField _geoCodedImageCount;

	static final long PIC_NAMES_PERSISTENT_STORE_ID = 0xf2cc90250b17fbdfL;

	public ProgressIndicatorScreen(PicturesMapScreen mapScreen) {
		super(new VerticalFieldManager());
		_mapScreen = mapScreen;

		_imgVector = new SimpleSortingVector();
		_geoCodedImageCount = new LabelField();

		model.setController(controller);
		view.setModel(model);
		view.setController(controller);
		controller.setModel(model);
		controller.setView(view);

		view.setLabel("Retrieving images...");
		view.createProgressBar(Field.FIELD_HCENTER);

		HorizontalFieldManager hm = new HorizontalFieldManager();
		hm.add(new LabelField("Images with GPS information:"));
		hm.add(_geoCodedImageCount);

		view.add(hm);
		add(view);

		// Retrieve the persistent object for this application
		_store = PersistentStore.getPersistentObject(PIC_NAMES_PERSISTENT_STORE_ID);

		synchronized (_store) {
			// If the PersistentObject is empty, initialize it
			if (_store.getContents() == null) {
				_store.setContents(new Hashtable());
			}
		}

		// Retrieve the saved Meeting objects from the persistent store
		_coordinates = (Hashtable) _store.getContents();

		_progressThread = new ProgressThread();
		_progressThread.start();
	}

	public void done() {
		_mapScreen.setImageVector(_imgVector);
		persist();

		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				ProgressIndicatorScreen.this.close();
			}
		});
	}

	public void persist() {
		// Synchronize on the PersistentObject so that no other object can
		// acquire the lock before we finish the commit operation.
		synchronized (_store) {
			_store.setContents(_coordinates);
			PersistentObject.commit(_store);
		}
	}

	// A thread that simulates the processing of data
	class ProgressThread extends Thread {
		public void run() {
			_jpegFileNames = getPictureFileNames();
			int numFiles = _jpegFileNames.size();

			ProgressIndicatorScreen.this.model.setValueMax(numFiles + 1);
			for (int i = 0; i < numFiles; ++i) {
				String fileName = (String) _jpegFileNames.elementAt(i);

				if (_coordinates.containsKey(fileName)) {
					Object newCoord = _coordinates.get(fileName);
					if (newCoord instanceof PersitableCoordinates) {
						GeoImage gsImg = new GeoImage(fileName, ((PersitableCoordinates) newCoord).getLat(),
								((PersitableCoordinates) newCoord).getLon(), fileName);
						_imgVector.addElement(gsImg);
					}
				} else {
					Coordinates newCoord = getJPEGCoordinates(fileName);
					if (newCoord instanceof Coordinates) {
						GeoImage gsImg = new GeoImage(fileName, newCoord.getLatitude(), newCoord.getLongitude(),
								fileName);
						_imgVector.addElement(gsImg);
						_coordinates.put(fileName,
								new PersitableCoordinates(newCoord.getLatitude(), newCoord.getLongitude()));
					} else {
						_coordinates.put(fileName, "");
					}
				}
				ProgressIndicatorScreen.this.model.setValue(i + 1);

				if (i % 10 == 0 || i < 10) {
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							_geoCodedImageCount.setText("" + _imgVector.size());
						}
					});
				}

			}

			done();

		}

		private Vector getPictureFileNames() {
			FileConnection fc = null;
			String root = null;
			Vector jpegFileNames = new Vector();

			Enumeration e = FileSystemRegistry.listRoots();
			while (e.hasMoreElements()) {
				root = (String) e.nextElement();
				String prefixPath = null;
				if (root.equalsIgnoreCase("sdcard/")) {
					try {
						prefixPath = "file:///SDCard/BlackBerry/camera/";
						fc = (FileConnection) Connector.open(prefixPath, Connector.READ);
						if (fc != null) {
							addFileNames(jpegFileNames, prefixPath, fc.list());
						}
					} catch (Exception ioex) {
						Dialog.alert(ioex.toString());
					}
				} else if (root.equalsIgnoreCase("store/")) {
					try {
						prefixPath = "file:///store/home/user/pictures/";
						fc = (FileConnection) Connector.open(prefixPath, Connector.READ);
						if (fc != null) {
							addFileNames(jpegFileNames, prefixPath, fc.list());
						}
					} catch (Exception ioex) {
						Dialog.alert(ioex.toString());
					}
				}
			}

			return jpegFileNames;
		}

		private void addFileNames(Vector names, String prefixPath, Enumeration e) {
			while (e.hasMoreElements()) {
				String s = (String) e.nextElement();

				if (s.toLowerCase().endsWith("jpg")) {
					names.addElement(prefixPath + s);
				}
			}
		}

		private Coordinates getJPEGCoordinates(String fileName) {
			File jpegFile = new File(fileName);
			Metadata metaData = new Metadata();
			try {
				metaData = JpegMetadataReader.readMetadata(jpegFile);
			} catch (JpegProcessingException je) {
				System.err.println("error 1a");
				// close();
			}

			String strLat = metaData.getLatitude();
			String strLon = metaData.getLongitude();

			if (strLat != null && strLon != null) {
				double lat = Double.parseDouble(strLat);
				double lon = Double.parseDouble(strLon);

				jpegFile.setLat(lat);
				jpegFile.setLon(lon);
				return new Coordinates(jpegFile.getLat(), jpegFile.getLon(), Float.NaN);
			}

			return null;

		}

	}
}

class PersitableCoordinates implements Persistable {

	private double _lat;
	private double _lon;

	public PersitableCoordinates(double lat, double lon) {
		_lat = lat;
		_lon = lon;
	}

	public double getLat() {
		return _lat;
	}

	public double getLon() {
		return _lon;
	}
}
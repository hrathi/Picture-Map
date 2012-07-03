package myapps;

import net.rim.device.api.lbs.maps.model.geospatial.GsElement;
import net.rim.device.api.lbs.maps.model.geospatial.GsRoot;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.BorderFactory;

public class PictureViewerScreen extends MainScreen {

	GsRoot picElements;
	final FlowFieldManager flow = new FlowFieldManager(Field.USE_ALL_WIDTH | Field.USE_ALL_HEIGHT);
	
	public PictureViewerScreen(GsRoot gsRoot)
	{
		picElements = gsRoot;
		//this.setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));

		flow.setPadding(2, 2, 2, 2);
		flow.setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
		flow.setBorder(BorderFactory.createRoundedBorder(new XYEdges(2,2,2,2)));

		add(flow);
		
		LoadPicturesThread thread = new LoadPicturesThread();
		thread.start();
	}
	
	class LoadPicturesThread extends Thread { 
		public void run() {
			int allFiles = picElements.size();
			for (int i = 0; i < allFiles; i++) {
				GsElement element = (GsElement) picElements.getElementAt(i);
				String name = element.getName();
				ImageThumbnailField b = new ImageThumbnailField( PicturesMapScreen.getScaledBitmapImage(name, 15, 100, 100), name);	

				synchronized (UiApplication.getEventLock()) {
					flow.add(b);
				}

				if(i == 0) {
					b.setFocus();
				}
			}
		}
	}
}



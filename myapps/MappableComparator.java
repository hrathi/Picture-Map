package myapps;

import net.rim.device.api.lbs.maps.model.MapPoint;
import net.rim.device.api.util.Comparator;

public class MappableComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		if( arg0 instanceof MapPoint && arg1 instanceof MapPoint ) {
			double lat1 = ((GeoImage)arg0).getLat();
			double lat2 = ((GeoImage)arg1).getLat();
			if( lat1 > lat2) {
				return 1;
			}
			else if( lat1 < lat2 ) {
				return -1;
			}
			else {
				return 0;
			}
		}
		return 0;
	}

}

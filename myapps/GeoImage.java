package myapps;

import net.rim.device.api.lbs.maps.model.geospatial.GsImage;

public class GeoImage extends GsImage {
	private String _thumbnailUri;

	public GeoImage(String thumbnailUri, double lat, double lon, String caption) {
		super();
		setLat(lat);
		setLon(lon);
		setName(caption);
		_thumbnailUri = thumbnailUri;		
	}
	
	public String getThumbnailUri() {
		return _thumbnailUri;
	}
	
	public boolean isClusterable() {
		return false;
	}
}

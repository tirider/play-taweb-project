package models.beans;

public class Photo 
{
	private String imgThumbnailUrl;
	private String imgLargeUrl;

	public Photo(String imgThumbnailUrl, String imgLargeUrl) {
		this.imgThumbnailUrl = imgThumbnailUrl;
		this.imgLargeUrl = imgLargeUrl;
	}

	public String getImgThumbnailUrl() {
		return imgThumbnailUrl;
	}

	public void setImgThumbnailUrl(String imgThumbnailUrl) {
		this.imgThumbnailUrl = imgThumbnailUrl;
	}

	public String getImgLargeUrl() {
		return imgLargeUrl;
	}

	public void setImgLargeUrl(String imgLargeUrl) {
		this.imgLargeUrl = imgLargeUrl;
	}
}

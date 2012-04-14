package nzdis.tobaccofree;

public class ObservationStat {

	private String topText,bottomText;

	public ObservationStat(){};
	
	public ObservationStat(String top,String bottom){
		topText = top;
		bottomText = bottom;
	}
	
	public String getTopText() {
		return topText;
	}

	public void setTopText(String topText) {
		this.topText = topText;
	}

	public String getBottomText() {
		return bottomText;
	}

	public void setBottomText(String bottomText) {
		this.bottomText = bottomText;
	}
	
	
}

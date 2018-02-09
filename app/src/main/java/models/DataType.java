package models;

public class DataType {
	public String time;
	public boolean music;
	public boolean speak;
	public double averagedb;
	public double srfV;
	public double zcrV;
	public double sustainS;
	public double sustainT;

	public void getData(DataType data) {
		this.speak = Boolean.getBoolean(data.getSpeak());
		this.music = Boolean.getBoolean(data.getSpeak());
		this.averagedb = Double.valueOf(data.getAveragedb());
		this.srfV = Double.valueOf(data.getSRF());
		this.zcrV = Double.valueOf(data.getZCR());
		this.sustainS = Double.valueOf(data.getSustainS());
		this.sustainT = Double.valueOf(data.getSustainT());

	}

	public String getSpeak() {
		return Boolean.toString(this.speak);
	}

	public String getMusic() {
		return Boolean.toString(this.music);
	}

	public String getAveragedb() {
		return Double.toString(this.averagedb);
	}

	public String getSRF() {
		return Double.toString(this.srfV);
	}

	public String getZCR() {
		return Double.toString(this.zcrV);
	}

	public String getSustainS() {
		return Double.toString(this.sustainS);
	}

	public String getSustainT() {
		return Double.toString(this.sustainT);
	}
	public void clear() {
		// TODO Auto-generated method stub
		time = null;
	}

}

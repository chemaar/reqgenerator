package es.uc3m.inf.kr.reqgenerator;

public class MetricMapping {

	public double precision = 1.0;
	public double recall = 1.0;
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	@Override
	public String toString() {
		return "MetricMapping [precision=" + precision + ", recall=" + recall
				+ "]";
	}
	
}

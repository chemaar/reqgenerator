package es.uc3m.inf.kr.reqgenerator.to;

public class MappingRequirementTO {

	public String from;
	public String to;
	public float confidence;
	public float normalizedConfidence;
	@Override
	public String toString() {
		return "MappingRequirementTO [from=" + from + ", to=" + to
				+ ", confidence=" + confidence + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(confidence);
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + Float.floatToIntBits(normalizedConfidence);
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingRequirementTO other = (MappingRequirementTO) obj;
		if (Float.floatToIntBits(confidence) != Float
				.floatToIntBits(other.confidence))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (Float.floatToIntBits(normalizedConfidence) != Float
				.floatToIntBits(other.normalizedConfidence))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
	
	
}

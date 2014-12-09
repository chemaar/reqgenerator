package es.uc3m.inf.kr.reqgenerator;

import java.util.Comparator;

import es.uc3m.inf.kr.reqgenerator.to.MappingRequirementTO;

public class MappingComparator implements Comparator<MappingRequirementTO>{

	public int compare(MappingRequirementTO o1, MappingRequirementTO o2) {
		return Float.compare(o2.confidence, o1.confidence);
	}
	
}
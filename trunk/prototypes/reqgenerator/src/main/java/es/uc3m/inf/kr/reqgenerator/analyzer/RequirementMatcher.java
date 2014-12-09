package es.uc3m.inf.kr.reqgenerator.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moldeas.common.loader.resources.FilesResourceLoader;

import es.uc3m.inf.kr.reqgenerator.CPV2008DAOImpl;
import es.uc3m.inf.kr.reqgenerator.MappingComparator;
import es.uc3m.inf.kr.reqgenerator.MetricMapping;
import es.uc3m.inf.kr.reqgenerator.NaiveRequirementsGenerator;
import es.uc3m.inf.kr.reqgenerator.to.ListMappingRequirementTO;
import es.uc3m.inf.kr.reqgenerator.to.MappingRequirementTO;
import es.uc3m.inf.kr.reqgenerator.to.PSCTO;
import es.uc3m.inf.kr.reqgenerator.to.RequirementTO;

public class RequirementMatcher {

	public static void main(String []args) throws Exception{
		Map<String, MetricMapping> resultsText = new HashMap<String, MetricMapping>();
		Map<String, MetricMapping> resultsPattern = new HashMap<String, MetricMapping>();
		MappingComparator mc = new MappingComparator();
		//Load mappings
		Map<String,ListMappingRequirementTO> expectedMappings = new HashMap<String,ListMappingRequirementTO>();

		//Load domain ontologies
		FilesResourceLoader resourceLoader = new FilesResourceLoader(
				new String[]{
						"cpv/cpv-2008.ttl"
					//	,"cpv/cpv-2003.ttl"
				}
				);
		CPV2008DAOImpl daoCPV2008 = new CPV2008DAOImpl(resourceLoader);

		//For every test
		for(int test = 1; test<=NaiveRequirementsGenerator.MAX_STAKEHOLDER_SPECIFICATIONS;test++){
			//Load stakeholder requirements
			String testId = "t"+test;
			String mappingId = "m"+test;
			List<RequirementTO> stakeholderRequirements = loadRequirements("tests//"+testId+"-stakeholder.txt","Stakeholder");
			//Load systems requirements
			List<RequirementTO> systemRequirements = loadRequirements("tests//"+testId+"-system.txt","System");
			//System.out.println("Loaded stakeholder requirements: "+stakeholderRequirements.size()+" system: "+systemRequirements.size());
			//Load mappings
			BufferedReader fileReader = new BufferedReader(new FileReader(new File("tests//"+mappingId+"-mappings.txt")));
			String line = fileReader.readLine();
			while(line!=null){
				String fields[] = line.split("#");
				String from = fields[0];
				String to = fields[1];
				ListMappingRequirementTO mapping = expectedMappings.get(from);
				if(mapping!=null){
					mapping.to.add(to);
				}else{
					mapping = new ListMappingRequirementTO();
					mapping.from = from;
					mapping.to.add(to);
					expectedMappings.put(from, mapping);
				}
				line = fileReader.readLine();
			}
			fileReader.close();

			//Index systems requirements
			RequirementIndexer indexer = new RequirementIndexer(systemRequirements);
			//For every stakeholder requirement
			for(RequirementTO stakeholderRequirement:stakeholderRequirements){
				//System.out.println("Processing "+stakeholderRequirement.id);
				List<MappingRequirementTO> extractedMappings = createMappingsFromText(indexer,
						stakeholderRequirement.id,stakeholderRequirement.text,mc);
				if(extractedMappings.size()>0){
//					Collections.sort(extractedMappings, mc);
//					float max = extractedMappings.get(0).confidence;
//					float min = extractedMappings.get(extractedMappings.size()-1).confidence;	
//					//System.out.println("Mappings created: "+extractedMappings.size()+" max confidence: "+max+" min: "+min);
//					for(MappingRequirementTO mapping:extractedMappings){
//						mapping.normalizedConfidence = (float)mapping.confidence-min/(max-min);
//					}
//					
					resultsText.put(stakeholderRequirement.id, 
							calculateMetrics(expectedMappings.get(stakeholderRequirement.id),extractedMappings,10));
				}
				//Repeat for pattern-based
				//Exploit properties
				PSCTO pscTO = daoCPV2008.cpv2008.get(stakeholderRequirement.cpvElement);
				Set<MappingRequirementTO> allMappings = new HashSet<MappingRequirementTO>();
				if(pscTO==null){//It is not in the domain vocabulary
					extractedMappings = createMappingsFromText(indexer,
							stakeholderRequirement.id,stakeholderRequirement.text,mc);
					resultsPattern.put(stakeholderRequirement.id, 
							calculateMetrics(expectedMappings.get(stakeholderRequirement.id),allMappings,10));
				}else{
					List<PSCTO> relatedConcepts = NaiveRequirementsGenerator.collect(pscTO);
					List<PSCTO> exactMatches = pscTO.getExactMatches();
					for(PSCTO exactMatch:exactMatches){
						PSCTO match = daoCPV2008.cpv2008.get(exactMatch.getUri());
						if(match!=null) relatedConcepts.add(match);
					}
					for(int relation = 0; relation<20 && relation<relatedConcepts.size();relation++){
					//for(int relation = 0;  relation<relatedConcepts.size();relation++){
						PSCTO conceptRelation = relatedConcepts.get(relation);
						List<MappingRequirementTO> createMappings = indexer.createMappings(
										stakeholderRequirement.id,
										conceptRelation.getPrefLabel());
						allMappings.addAll(createMappings);
						//System.out.println("Mapping "+conceptRelation.getPrefLabel()+" has created: "+createMappings.size()+" mappings.");
						
					}
					resultsPattern.put(stakeholderRequirement.id, 
							calculateMetrics(expectedMappings.get(stakeholderRequirement.id),allMappings,10));
					relatedConcepts.clear();
					allMappings.clear();
				}
				
			}
			//Text
			MetricMapping aggregateTextResults = aggregateMetrics(resultsText.values());
			resultsText.clear();

			//Patterns
			MetricMapping aggregatePatternResults = aggregateMetrics(resultsPattern.values());
			resultsPattern.clear();
			
			System.out.println(testId+";"+stakeholderRequirements.size()+";"+systemRequirements.size()+";"+
					aggregateTextResults.precision+";"+aggregateTextResults.recall+";"+
					aggregatePatternResults.precision+";"+aggregatePatternResults.recall);
		}//End for every test

		//Generate query
		//Search
		//Calculate p, r
		//Generate expanded query
		//Search
		//Calculate p,r
	}

	
	public static List<MappingRequirementTO> createMappingsFromText(RequirementIndexer indexer, String from, String textRequirement,MappingComparator mc){
		List<MappingRequirementTO> extractedMappings = indexer.createMappings(from, textRequirement);
		if(extractedMappings.size()>0){
			Collections.sort(extractedMappings, mc);
			float max = extractedMappings.get(0).confidence;
			float min = extractedMappings.get(extractedMappings.size()-1).confidence;	
			//System.out.println("Mappings created: "+extractedMappings.size()+" max confidence: "+max+" min: "+min);
			for(MappingRequirementTO mapping:extractedMappings){
				mapping.normalizedConfidence = (float)mapping.confidence-min/(max-min);
			}

		}
		return extractedMappings;
	}
	
	public static MetricMapping aggregateMetrics(Collection<MetricMapping> metrics){
		double p = 0.0;
		double r = 0.0;
		for(MetricMapping metric:metrics){
			p += metric.precision;
			r += metric.recall;
		}
		p = (double) p/(metrics.size()>0?metrics.size():1);
		r = (double) r/(metrics.size()>0?metrics.size():1);
		MetricMapping mm = new MetricMapping();
		mm.precision = p;
		mm.recall = r;
		return mm;

	}
	
	public static MetricMapping calculateMetrics(
			ListMappingRequirementTO expectedMappings,
			Collection<MappingRequirementTO> resultsMappings, int take){
		MetricMapping metrics = new MetricMapping();
		if(expectedMappings!=null){
			int countTp = 0;
			int countFp = 0;
			int countFn = 0;
			Set<String> resultTo = new HashSet<String>();
			for(MappingRequirementTO mapping:resultsMappings){
				resultTo.add(mapping.to);
				if(expectedMappings.to.contains(mapping.to)){
					countTp++;
				}
			}
			for(String to:expectedMappings.to){
				if(!resultTo.contains(to)){
					countFn++;
				}
			}
			countFp = resultTo.size()-countTp;
			double precision = (double) countTp/(countTp+countFp);
			double recall = (double) countTp/(countTp+countFn);
			//System.out.println("Expected: "+expectedMappings.to.size()+"Result:"+resultTo.size()+"TP:"+countTp+" FP:"+countFp+" FN: "+countFn+" P:"+precision+" R:"+recall);
			
			metrics.precision = Double.compare(Double.NaN, precision)==0?0:precision;
			metrics.recall = recall;

		}
		return metrics;

	}

	private static List<RequirementTO> loadRequirements(String fileStakeholder,
			String type) throws IOException {
		List<RequirementTO> requirements = new LinkedList<RequirementTO>();
		BufferedReader fileReader = new BufferedReader(new FileReader(new File(fileStakeholder)));
		String line = fileReader.readLine();
		while(line!=null){
			String fields[] = line.split("#");
			String id = fields[0];
			String text = fields[1];
			RequirementTO requirement = new RequirementTO(id,text, type);
			if(fields.length==3)
				requirement.cpvElement = fields[2];
			requirements.add(requirement);
			line = fileReader.readLine();
		}
		fileReader.close();
		return requirements;
	}




}

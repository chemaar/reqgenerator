package es.uc3m.inf.kr.reqgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.moldeas.common.loader.resources.FilesResourceLoader;

import es.uc3m.inf.kr.reqgenerator.to.PSCTO;

public class NaiveRequirementsGenerator {

	public static final int MAX_STAKEHOLDER_SPECIFICATIONS = 30;

	public static void main(String []args) throws FileNotFoundException{
		Random rand = new Random();
		
		//1-Load patterns
		String []stakeholderActions = new String []{
				"buy"	
		};
		String []stakholderBoilerPlates = new String[]{
				"The %s %s may %s."	
		};
		String []systemActions = new String []{
				"buy"	
		};
		String []systemBoilerPlates = new String[]{
				"The %s %s may %s."	
		};
		//2-Load domain ontologies
		FilesResourceLoader resourceLoader = new FilesResourceLoader(
				new String[]{
						"cpv/cpv-2008.ttl"
						,"cpv/cpv-2003.ttl"
				}
				);
		CPV2008DAOImpl daoCPV2008 = new CPV2008DAOImpl(resourceLoader);
		//3-Generate stakeholder requirements
		//3.1-Select all divisions and groups
		Map<String, PSCTO> allElements = daoCPV2008.cpv2008;
		List<PSCTO> divisions = new LinkedList<PSCTO>(daoCPV2008.getDivisions().values());
		List<PSCTO> groups =  new LinkedList<PSCTO>(daoCPV2008.getGroups().values());
		List<PSCTO> classes = new LinkedList<PSCTO>(daoCPV2008.getClasses().values());
		List<PSCTO> categories =  new LinkedList<PSCTO>(daoCPV2008.getCategories().values());
		List<PSCTO> cpvElementsForStakeHolderRequirements = new LinkedList<PSCTO>(divisions);//add divisions
		cpvElementsForStakeHolderRequirements.addAll(groups);
		List<PSCTO> cpvElementsForSystemRequirements = new LinkedList<PSCTO>(classes);
		cpvElementsForSystemRequirements.addAll(categories);
		int nElementsForStakeholders = cpvElementsForStakeHolderRequirements.size();	
		int minElementsForStakeholders = 0 + nElementsForStakeholders/2 ;
		int i = 0;
		int k = 0;
		Set<String> stakeholderRequirements = new HashSet<String>();
		for(int currentSpecification = 1; currentSpecification<=MAX_STAKEHOLDER_SPECIFICATIONS; currentSpecification++){
			//3.2-Select a number between min (0.5 max) and max (number total of divisions and groups)
			//3.3- Select between min and max divisions or groups a division or group
			int requiredRequirements = rand.nextInt((nElementsForStakeholders - minElementsForStakeholders) + 1) + minElementsForStakeholders;
			i = 0;
			int nsystemrequirement = 0;
			System.out.println("Generation Stakeholder specification: "+currentSpecification+" with "+requiredRequirements);
			PrintWriter stakeholderSpec = new PrintWriter(new File("tests//t"+currentSpecification+"-stakeholder.txt"));
			PrintWriter systemSpec = new PrintWriter(new File("tests//t"+currentSpecification+"-system.txt"));
			PrintWriter mappings = new PrintWriter(new File("tests//m"+currentSpecification+"-mappings.txt"));
			Set<String> systemRequirements = new HashSet<String>();
			while(i<requiredRequirements){
				int position = rand.nextInt((nElementsForStakeholders-1 - 0) + 1) + 0;
				//3.3.1 Get element at position i
				PSCTO pscTO = cpvElementsForStakeHolderRequirements.get(position);
				String description =pscTO.getPrefLabel();
				String typeLabel =pscTO.getTypeLabel();
				String boilerPlate = getRandomElement(stakholderBoilerPlates,rand);
				String action = getRandomElement(stakeholderActions,rand);
				String textStakholderRequirement = String.format(boilerPlate, description, typeLabel, action); 
				if(!stakeholderRequirements.contains(pscTO.getUri())){
					//3.3.3 Pick up a pattern
					//3.3.4 Generate requirement
					stakeholderSpec.println("R"+i+"#"+textStakholderRequirement+"#"+pscTO.getUri());
					stakeholderRequirements.add(textStakholderRequirement);
					//For each class, category or mapping or this PSCTO generate a system requirement
					List<PSCTO> allNarrowers = collect(pscTO);
					//System.out.println("First level: "+pscTO.getNarrowers().size()+"  all narrowers: "+allNarrowers.size());
					int nElementsForSystemRequirements = allNarrowers.size();	
					int minElementsForSystemRequirements = 0 + nElementsForSystemRequirements/2 ;
					int requiredSystemRequirements = rand.nextInt((nElementsForSystemRequirements - minElementsForSystemRequirements) + 1) + minElementsForSystemRequirements;
					k = 0;
					System.out.println("Generation System specification for requirement  R"+i+" with "+requiredSystemRequirements);
					//while(k<requiredSystemRequirements){
					for(k =0;k<allNarrowers.size() && k<requiredSystemRequirements;k++){
						int narrowerPosition = rand.nextInt((nElementsForSystemRequirements-1 - 0) + 1) + 0;
						PSCTO narrower = allNarrowers.get(narrowerPosition);
						String narrowerDescription =narrower.getPrefLabel();
						String narrowerTypeLabel =narrower.getTypeLabel();
						String boilerPlateSystem = getRandomElement(systemBoilerPlates,rand);
						String actionSystem = getRandomElement(systemActions,rand);
						String textSystemRequirement = String.format(boilerPlateSystem, narrowerDescription, narrowerTypeLabel, actionSystem); 
						//3.3.4 Generate requirement
						if(!systemRequirements.contains(textSystemRequirement)){
							systemSpec.println("SR"+nsystemrequirement+"#"+textSystemRequirement);
							systemRequirements.add(textSystemRequirement);
							//k++;
							nsystemrequirement++;
							mappings.println("R"+i+"#"+"SR"+nsystemrequirement);
						}
						
					}
					//System requirements with matches
					//FIXME: ALL VOCABULARIES
					List<PSCTO> exactMatches = pscTO.getExactMatches();
					for(int m =0;m<exactMatches.size() && m<10;m++){
						PSCTO match = daoCPV2008.cpv2008.get(exactMatches.get(m).getUri());
						String exactMatchDescription =match.getPrefLabel();
						String exactMatchTypeLabel =match.getTypeLabel();
						String boilerPlateSystem = getRandomElement(systemBoilerPlates,rand);
						String actionSystem = getRandomElement(systemActions,rand);
						String textSystemRequirement = String.format(boilerPlateSystem, exactMatchDescription, exactMatchTypeLabel, actionSystem); 
						//3.3.4 Generate requirement
						if(!systemRequirements.contains(textSystemRequirement)){
							systemSpec.println("SR"+nsystemrequirement+"#"+textSystemRequirement);
							systemRequirements.add(textSystemRequirement);
							nsystemrequirement++;
							mappings.println("R"+i+"#"+"SR"+nsystemrequirement);
						}
					}
					
					//systemRequirements.clear();
					//stakeholderRequirements.add(pscTO.getUri());
					i++;
				}
			}
			mappings.close();
			systemSpec.close();
			stakeholderSpec.close();
			System.out.println("End generation of stakeholder specification "+currentSpecification);
		}

	}


	public static List<PSCTO> collect(PSCTO pscTO){
		List<PSCTO> currentNarrowers = pscTO.getNarrowers();
		List<PSCTO> narrowers = new LinkedList<PSCTO>(currentNarrowers);
		if(currentNarrowers!=null && currentNarrowers.size()>0){
			for(PSCTO narrower: currentNarrowers){
				narrowers.addAll(collect(narrower));
			}
		}
		return narrowers;
	}

	public static String getRandomElement(String []values,Random rand){
		int min = 0;
		int max = values.length-1;
		return values[rand.nextInt((min - max) + 1) + min];
	}





}

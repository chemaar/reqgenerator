package es.uc3m.inf.kr.reqgenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.moldeas.common.exceptions.MoldeasModelException;
import org.moldeas.common.loader.JenaRDFModelWrapper;
import org.moldeas.common.loader.resources.ResourceLoader;
import org.moldeas.common.utils.PSCConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

import es.uc3m.inf.kr.reqgenerator.to.PSCTO;

public class CPV2008DAOImpl {
	protected static Logger logger = Logger.getLogger(CPV2008DAOImpl.class);
	public static final String MAPPER_FIELD_PREF_LABEL = "prefLabel";

	public  Map<String,PSCTO> cpv2008 = new HashMap<String, PSCTO>();
	private Map<String,PSCTO> divisions = new HashMap<String, PSCTO>();
	private Map<String,PSCTO> groups = new HashMap<String, PSCTO>();;
	private Map<String,PSCTO> classes = new HashMap<String, PSCTO>();;
	private Map<String,PSCTO> categories = new HashMap<String, PSCTO>();;
	
	protected final static String SOURCE = "cpv/cpv-2008.ttl";

	public CPV2008DAOImpl(ResourceLoader loader){
		try{
			this.cpv2008 = loadPSCTOs(loader);
			//Generate narrowers
			fillNarrowers(getGroups().values(),this.cpv2008);
			fillNarrowers(getClasses().values(),this.cpv2008);
			fillNarrowers(getCategories().values(),this.cpv2008);
		}catch(Exception e){
			throw new MoldeasModelException(e);
		}
	}

	private Map<String,PSCTO> loadPSCTOs(ResourceLoader loader) {
		Map<String,PSCTO> cpvPSCTOs = new HashMap<String,PSCTO>();
		JenaRDFModelWrapper rdfModel = new JenaRDFModelWrapper(loader,"TURTLE");
		Model model = (Model) rdfModel.getModel();		
		ResIterator it = model.listResourcesWithProperty(model.getProperty(PSCConstants.SKOS_prefLabel));
		
		while (it.hasNext()){
			PSCTO current = new PSCTO();
			Resource r = it.next();
			StmtIterator iter1 = model.listStatements(
					new SimpleSelector(r, DC.identifier, (RDFNode) null) {
						public boolean selects(Statement s){	
							return !(s.getLiteral().getString().matches("^[A-Z]+.*$"));
						}
						});	
			while(iter1.hasNext()){	
				StmtIterator iter = model.listStatements(
						new SimpleSelector(r, model.getProperty(PSCConstants.SKOS_prefLabel), (RDFNode) null) {
							public boolean selects(Statement s)
							{return s.getLiteral().getLanguage().equalsIgnoreCase("en");}
						});	
				
				
				while (iter.hasNext()){
					current.setUri(r.getURI());
					current.setPrefLabel(iter.next().getString());
				}
				
				current.setType(r.getPropertyResourceValue(RDF.type).getURI());
			
				while (iter.hasNext()){
					current.setUri(r.getURI());
					current.setPrefLabel(iter.next().getString());
				}
				
				NodeIterator iterBroaders = model.listObjectsOfProperty(r, model.getProperty(PSCConstants.SKOS_Broader_Transitive));
				while (iterBroaders.hasNext()){
					current.getBroaders().add(new PSCTO(iterBroaders.next().asResource().getURI()));
				}
				
				NodeIterator iterExactMatches = model.listObjectsOfProperty(r, model.getProperty(PSCConstants.SKOS_EXACT_MATCH));
				while (iterExactMatches.hasNext()){
					current.getExactMatches().add(new PSCTO(iterExactMatches.next().asResource().getURI()));
				}
				
				logger.debug("Loaded "+current);
				
				if(current.getType().endsWith(PSCConstants.HTTP_PURL_ORG_WESO_CPV_DEF_DIVISION)){
					current.setTypeLabel("division");
					this.divisions.put(current.getUri(),current);
				}else if(current.getType().endsWith(PSCConstants.HTTP_PURL_ORG_WESO_CPV_DEF_GROUP)){
					current.setTypeLabel("group");
					this.groups.put(current.getUri(),current);
				}else if(current.getType().endsWith(PSCConstants.HTTP_PURL_ORG_WESO_CPV_DEF_CLASS)){
					current.setTypeLabel("class");
					this.classes.put(current.getUri(),current);
				}else{
					current.setTypeLabel("category");
					this.categories.put(current.getUri(),current);
				}
				
				cpvPSCTOs.put(current.getUri(), current);
				iter1.next();
			}
		}		
		return cpvPSCTOs;
	}
	private void fillNarrowers(Collection<PSCTO> innerLevel, Map<String,PSCTO> upperLevel){
		for(PSCTO element:innerLevel){
			for(PSCTO broader:element.getBroaders()){
				//System.out.println("Searching for broader: "+broader.getUri());
				PSCTO broaderTO = upperLevel.get(broader.getUri());
				if(broaderTO!=null){
				//	System.out.println("Adding narrower "+element.getPrefLabel()+ "to: "+broaderTO.getPrefLabel());
					broaderTO.getNarrowers().add(element);
				}
			}
		}
	}


	
	public Map<String, PSCTO> getDivisions() {
		return divisions;
	}

	public void setDivisions(Map<String, PSCTO> divisions) {
		this.divisions = divisions;
	}

	public Map<String, PSCTO> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, PSCTO> groups) {
		this.groups = groups;
	}

	public Map<String, PSCTO> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, PSCTO> classes) {
		this.classes = classes;
	}

	public Map<String, PSCTO> getCategories() {
		return categories;
	}

	public void setCategories(Map<String, PSCTO> categories) {
		this.categories = categories;
	}
	
	
}

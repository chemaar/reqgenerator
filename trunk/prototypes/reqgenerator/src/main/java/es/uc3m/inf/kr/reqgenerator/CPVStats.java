package es.uc3m.inf.kr.reqgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.moldeas.common.loader.JenaRDFModelWrapper;
import org.moldeas.common.loader.resources.FilesResourceLoader;
import org.moldeas.common.utils.PSCConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CPVStats {

	public static void main(String []args) throws FileNotFoundException{
		FilesResourceLoader resourceLoader = new FilesResourceLoader(
				new String[]{
						"cpv/cpv-2008.ttl"
				}
				);
		PrintWriter pw = new PrintWriter(new File("cpv-2008.txt"));
		JenaRDFModelWrapper rdfModel = new JenaRDFModelWrapper(resourceLoader,"TURTLE");
		Model model = (Model) rdfModel.getModel();		
		ResIterator it = model.listResourcesWithProperty(model.getProperty(PSCConstants.SKOS_Broader_Transitive));
		int broaders = 0;
		while (it.hasNext()){
			broaders++;
			it.next();
		}
		ResIterator it2 = model.listResourcesWithProperty(model.getProperty(PSCConstants.SKOS_prefLabel));

		while (it2.hasNext()){
			Resource r = it2.next();
			StmtIterator iter = model.listStatements(
					new SimpleSelector(r, model.getProperty(PSCConstants.SKOS_prefLabel), (RDFNode) null) {
						public boolean selects(Statement s)
						{return s.getLiteral().getLanguage().equalsIgnoreCase("en");}
					});	
			while (iter.hasNext()){
				pw.println(iter.next().getString());

			}
		}

pw.close();
		System.out.println("Broaders: "+broaders);
	}

}

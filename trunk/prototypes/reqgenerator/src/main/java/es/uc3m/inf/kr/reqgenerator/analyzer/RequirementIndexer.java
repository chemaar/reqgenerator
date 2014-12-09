package es.uc3m.inf.kr.reqgenerator.analyzer;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FuzzyLikeThisQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import es.uc3m.inf.kr.reqgenerator.to.MappingRequirementTO;
import es.uc3m.inf.kr.reqgenerator.to.RequirementTO;

public class RequirementIndexer {
	private static final int MAX_MATCHES = 20;
	private static final String ID_REQUIREMENT = "id";
	private static final String TEST_REQUIREMENT = "text";
	RAMDirectory idx;
	static Analyzer standardAnalyzer = new RequirementAnalyzer();

	public RequirementIndexer(Collection<RequirementTO> requirementTOs) throws CorruptIndexException, LockObtainFailedException, IOException{
		this.idx = new RAMDirectory();
		this.indexRequirements(requirementTOs);
	}
	
	private void indexRequirements(Collection<RequirementTO> requirementTOs) throws CorruptIndexException, LockObtainFailedException, IOException {
		boolean create = true;
		IndexDeletionPolicy deletionPolicy = 
				new KeepOnlyLastCommitDeletionPolicy(); 
		IndexWriter indexWriter = 
				new IndexWriter(idx,standardAnalyzer,create,
						deletionPolicy,IndexWriter.MaxFieldLength.UNLIMITED);
		for(RequirementTO requirementTO:requirementTOs){
			Field idField = new Field(ID_REQUIREMENT,requirementTO.id,Field.Store.YES,Field.Index.NOT_ANALYZED);
			Field subjectField = new Field(TEST_REQUIREMENT,requirementTO.text,Field.Store.YES,Field.Index.ANALYZED);
			Document doc = new Document();
			doc.add(idField);
			doc.add(subjectField);
			indexWriter.addDocument(doc);
		}
		indexWriter.optimize();
		indexWriter.close();
	}

	public static Query createQueryFromString(String q) throws ParseException {		
		QueryParser parser = new QueryParser(TEST_REQUIREMENT,standardAnalyzer);
		parser.setDefaultOperator(QueryParser.Operator.OR);
		Query query = parser.parse(QueryParser.escape(q));
		return query;

	}

	public static Query createFuzzyQueryFromString(String q) throws ParseException {		
		FuzzyLikeThisQuery flt=new FuzzyLikeThisQuery(50,standardAnalyzer); 
		flt.addTerms(q, TEST_REQUIREMENT, 0.75f,FuzzyQuery.defaultPrefixLength); 
		return flt;
	}


	public List<MappingRequirementTO> createMappings(RequirementTO requirement){
		return createMappings(requirement.id, requirement.text);
	}

	public List<MappingRequirementTO> createMappings(String from, String textRequirement){
		List<MappingRequirementTO> mappings = new LinkedList<MappingRequirementTO>();		
		//FIXME: to optimize indexsearcher no delegate call
		try {
			IndexSearcher indexSearcher = new IndexSearcher(this.idx);		
			ScoreDoc[] scoreDocs = fetchSearchResults(
					createQueryFromString(cleanRequirement(textRequirement)), indexSearcher, MAX_MATCHES);
			//If no result then fuzzy query
			if (scoreDocs.length == 0){
				scoreDocs = fetchSearchResults(
						createFuzzyQueryFromString(cleanRequirement(textRequirement)), indexSearcher, 3);
			}
			for(int i = 0; i<scoreDocs.length;i++){
				Document doc = indexSearcher.doc(scoreDocs[i].doc);
				String idTo = doc.getField(ID_REQUIREMENT).stringValue();
				MappingRequirementTO mapping = new MappingRequirementTO();
				mapping.from = from;
				mapping.to = idTo;
				mapping.confidence = scoreDocs[i].score;
				mappings.add(mapping);

			}
		} catch (Exception e) {
			System.err.println(e);
		}finally{

		}

		return mappings;
	}
	
	public static ScoreDoc[] fetchSearchResults(Query query, Searcher indexSearcher, int n ){
		try{
			TopScoreDocCollector collector = TopScoreDocCollector.create(n, true);
			indexSearcher.search(query, collector);
			ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;
			return scoreDocs;
		}catch(IOException e){
			e.printStackTrace();
		}
		return new ScoreDoc[0];
	}

	public static String cleanRequirement(String q){
		String value = q.replaceAll("-", "");
		value = value.replaceAll("á", "a");
		value = value.replaceAll("é", "e");
		value = value.replaceAll("í", "i");
		value = value.replaceAll("ó", "o");
		value = value.replaceAll("ú", "u");
		value = value.replaceAll("/", "");
		value = value.replaceAll("'", "");
		value = value.replaceAll("&", " ");
		value = value.replaceAll("\r", " ");
		value = value.replaceAll("\n", " ");
		value = value.replaceAll("\n\r", " ");
		value = value.replaceAll("\u0085", " ");
		value = value.replaceAll("\u2028", " ");
		value = value.replaceAll("\u2029", " ");
		value = value.replaceAll("\\W", " ").replaceAll("\\d", "");
		return value;
	}

}

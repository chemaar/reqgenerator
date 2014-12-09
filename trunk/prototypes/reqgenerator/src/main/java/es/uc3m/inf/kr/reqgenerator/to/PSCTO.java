package es.uc3m.inf.kr.reqgenerator.to;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "pscTO")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pscTO", propOrder = {
    "id",
    "prefLabel",
    "uri",
    "type",
    "subject",
    "inScheme",
    "broaders"
})
public class PSCTO {

	private String id;
	private String prefLabel;
	private String uri;
	private String type;
	private String typeLabel;
	private String subject;
	private String inScheme;	
	private List<PSCTO> broaders;
	private List<PSCTO> narrowers;
	private List<PSCTO> exactMatches;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public PSCTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PSCTO(String uri) {
		super();
		this.uri = uri;
	}
	public PSCTO(String id, String uri) {
		super();
		this.id = id;
		this.uri = uri;
	}
	public String getPrefLabel() {
		return prefLabel;
	}
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}
	
	
	public String getTypeLabel() {
		return typeLabel;
	}
	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<PSCTO> getBroaders() {
		if(this.broaders==null){
			this.broaders = new LinkedList<PSCTO>();
		}
		return broaders;
	}
	public void setBroaders(List<PSCTO> broaders) {
		this.broaders = broaders;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getInScheme() {
		return inScheme;
	}
	public void setInScheme(String inScheme) {
		this.inScheme = inScheme;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		PSCTO other = (PSCTO) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	public List<PSCTO> getNarrowers() {
		if(this.narrowers==null){
			this.narrowers = new LinkedList<PSCTO>();
		}
		return narrowers;
	}
	public void setNarrowers(List<PSCTO> narrowers) {
		this.narrowers = narrowers;
	}
	public List<PSCTO> getExactMatches() {
		if(this.exactMatches==null){
			this.exactMatches = new LinkedList<PSCTO>();
		}
		return exactMatches;
	}
	public void setExactMatches(List<PSCTO> exactMatches) {
		this.exactMatches = exactMatches;
	}
	

}

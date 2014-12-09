package es.uc3m.inf.kr.reqgenerator.to;

public class RequirementTO {

	public String id;
	public String text;
	public String type;
	public String cpvElement;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public RequirementTO(String id, String text, String type) {
		super();
		this.id = id;
		this.text = text;
		this.type = type;
	}
	public String getCpvElement() {
		return cpvElement;
	}
	public void setCpvElement(String cpvElement) {
		this.cpvElement = cpvElement;
	}
	
	
}

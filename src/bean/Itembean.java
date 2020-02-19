package bean;

public class Itembean {
	private String item;
	private float minimal;
	private String currency;
	private String business;
	public Itembean(String item, float minimal, String currency, String business) {
		super();
		this.item = item;
		this.minimal = minimal;
		this.currency = currency;
		this.business = business;
	}
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public float getMinimal() {
		return minimal;
	}
	public void setMinimal(float minimal) {
		this.minimal = minimal;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
}

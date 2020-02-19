package bean;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import deco.NoDuplicateList;


public class Dbbean {
	
	public Dbbean(String link, String fass, List<Dbrow> lst) {
		super();
		this.link = link;
		this.fass = fass;
		this.lst = lst;
	}
	public Dbbean() {
		super();
	}
	private String link;
	private String fass;
	private List<Dbrow> lst;
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getFass() {
		return fass;
	}
	public void setFass(String fass) {
		this.fass = fass;
	}
	public List<Dbrow> getLst() {
		return lst;
	}
	public void setLst(List<Dbrow> lst) {
		this.lst = lst;
	}
	public void writeToStream(DataOutputStream dos) throws IOException  {
		dos.writeUTF(link);
		dos.writeUTF(fass);
		for (Dbrow row: lst) {
			row.writeToStream(dos);
		}
	}
	public void readFromStream(DataInputStream dis) throws IOException {
		link=dis.readUTF();
		fass=dis.readUTF();
		//System.out.println(link+fass);
		lst=new NoDuplicateList<Dbrow>();
		//int i=0;
		try {
			while (true) {
				//System.out.println(i++);
				Dbrow row = new Dbrow();
				row.readFromStream(dis);
				lst.add(row);
			}
		} catch (IOException e) {
			
		}
	}

}

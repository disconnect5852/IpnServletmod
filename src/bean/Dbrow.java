package bean;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class Dbrow {
	
	public Dbrow(String emil, String ass, String ajdi, String ajdi1, boolean notifysent) {
		super();
		this.emil = emil;
		this.ass = ass;
		this.ajdi = ajdi;
		this.ajdi1 = ajdi1;
		this.notifysent=notifysent;
	}
	public Dbrow() {
		super();
	}
	private String emil;
	private String ass;
	private String ajdi;
	private String ajdi1;
	private boolean notifysent;
	public String getEmil() {
		return emil;
	}
	public void setEmil(String emil) {
		this.emil = emil;
	}
	public String getAss() {
		return ass;
	}
	public void setAss(String ass) {
		this.ass = ass;
	}
	public String getAjdi() {
		return ajdi;
	}
	public void setAjdi(String ajdi) {
		this.ajdi = ajdi;
	}
	public String getAjdi1() {
		return ajdi1;
	}
	public void setAjdi1(String ajdi1) {
		this.ajdi1 = ajdi1;
	}
	
	public boolean isNotifysent() {
		return notifysent;
	}
	public void setNotifysent(boolean notifysent) {
		this.notifysent = notifysent;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emil == null) ? 0 : emil.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Dbrow))
			return false;
		Dbrow other = (Dbrow) obj;
		if (emil == null) {
			if (other.emil != null)
				return false;
		} else if (!emil.equals(other.emil))
			return false;
		return true;
	}
	public void writeToStream(DataOutputStream dos) throws IOException {
		dos.writeUTF(emil);
		dos.writeUTF(ass);
		dos.writeUTF(ajdi);
		dos.writeUTF(ajdi1);
		dos.writeBoolean(notifysent);
	}
	public void readFromStream(DataInputStream dis) throws IOException {
		emil=dis.readUTF();
		ass=dis.readUTF();
		ajdi=dis.readUTF();
		ajdi1=dis.readUTF();
		notifysent=dis.readBoolean();
	}
	@Override
	public String toString() {
		StringBuilder strb=new StringBuilder();
		strb.append(emil);
		strb.append(" ");
		strb.append(ass);
		strb.append(" ");
		strb.append(ajdi);
		strb.append(" ");
		strb.append(ajdi1);
		strb.append(" ");
		strb.append(notifysent);
		return strb.toString();
	}
	
	
	
}

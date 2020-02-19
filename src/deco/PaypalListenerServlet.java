package deco;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

import bean.Dbbean;
import bean.Dbrow;
import bean.Itembean;

public class PaypalListenerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static List<Itembean> ib=new ArrayList<Itembean>();
	private static final org.apache.log4j.Logger logger = LogManager.getLogger(PaypalListenerServlet.class);
	//private static final Logger log=Logging.getInstance();
	private final Properties props = new Properties();
	
	private static Dbbean db= new Dbbean("changethis", "changethis", new NoDuplicateList<Dbrow>());
	private static AESEncrypter cri;
	private static boolean debugmode=false;
	
	private static final Random rnd= new Random();
	//private static DataOutputStream dos;
	private static DbxClientV2 dbxUp;
	public Properties getProps() {
		return props;
	}
	
	public static org.apache.log4j.Logger getLogger() {
		return logger;
	}
	public PaypalListenerServlet() {
		super();
		try {
			// -- terméklista betöltése
			BufferedReader br=new BufferedReader(new FileReader("itemlist.csv"));
			String line = null;
			while (( line = br.readLine()) != null){
				String[] sarr= line.split(",", 0);
				ib.add(new Itembean(sarr[0].trim(), Float.parseFloat(sarr[1]), sarr[2].trim(), sarr[3].trim()));
			}
			br.close();
			//logger.info("Itemlist loaded!");
			// -- terméklista betöltve
			// -- beállításfájl betöltése
			props.load(new FileReader("dbmaker.cfg"));
			PropertyConfigurator.configure("log4j.properties");
			debugmode=Boolean.parseBoolean(props.getProperty("debugmode"));
			
			cri= new AESEncrypter(props.getProperty("dbcryptpsw"));
			// meglévõ adatbázis betöltése
			DataInputStream dis= new DataInputStream(new InflaterInputStream(cri.decryptstream(new FileInputStream(props.getProperty("dbfile"))), new Inflater()));
			db.readFromStream(dis);
			dis.close();
			//DeflaterOutputStream dcos = new DeflaterOutputStream(cri.encryptstream(new FileOutputStream(props.getProperty("dbfile"))), new Deflater(Deflater.DEFAULT_COMPRESSION));
			//dbxUp = new DbxUploader(props.getProperty("dropbox_token"));
			dbxUp= new DbxClientV2(new DbxRequestConfig("Dbuploader v0.X"), props.getProperty("dropbox_token"));
			logger.info("Database loaded, uploader started!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//log.log(Level.SEVERE,e.getMessage());
			logger.fatal(e);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Enumeration en = request.getParameterNames();
		String str = "cmd=_notify-validate";
		while(en.hasMoreElements()){
			String paramName = (String)en.nextElement();
			String paramValue = request.getParameter(paramName);
			str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue,"UTF-8");
		}
		//System.out.println(request.getRemoteAddr());
		// post back to PayPal system to validate
		// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
		// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
		// and configured for older versions.
		//System.out.println(request.getRequestURL());
		URL u = new URL(props.getProperty("listen_url"));
		URLConnection uc = u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println(str);
		pw.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String res = in.readLine();
		in.close();

		// assign posted variables to local variables
		//String itemName = request.getParameter("item_name");
		/*String itemNumber="";
		String paymentStatus="";
		String paymentAmount ="1";
		String paymentCurrency ="";
		String receiverEmail ="";
		String txnID="";
		String charset="";*/
		String itemNumber = request.getParameter("item_number");
		String paymentStatus = request.getParameter("payment_status");
		String paymentAmount = request.getParameter("mc_gross");
		String paymentCurrency = request.getParameter("mc_currency");
		//String txnId = request.getParameter("txn_id");
		String receiverEmail = request.getParameter("business");
		String payerEmail = request.getParameter("payer_email");
		String txnID=request.getParameter("txn_id");
		String charset=request.getParameter("charset");
		String memo=request.getParameter("memo");
		String payer_business_name=request.getParameter("payer_business_name");
		StringBuilder strb=new StringBuilder();

		//strb.append("No business: ");
		strb.append(itemNumber);
		strb.append(";");
		strb.append(paymentStatus);
		strb.append(";");
		strb.append(paymentCurrency);
		strb.append(";");
		strb.append(paymentAmount);
		strb.append(";");
		strb.append(receiverEmail);
		strb.append(";");
		strb.append(txnID);
		strb.append(";");
		strb.append(payerEmail);
		strb.append(";");
		strb.append(charset);
		strb.append(";");
		strb.append(memo);
		strb.append(";");
		strb.append(payer_business_name);
		
		//log.log(Level.WARNING,);
		if ("VERIFIED".equals(res)) {
			boolean fail=true;
			for (Itembean bean: ib) {
				if (itemNumber!=null 
						&& itemNumber.equals(bean.getItem())
						&& paymentStatus.equals("Completed") 
						&& paymentCurrency.equals(bean.getCurrency()) 
						&& Float.parseFloat(paymentAmount)>=bean.getMinimal() 
						&& receiverEmail.equals(bean.getBusiness())
						&& memo==null
						&& payer_business_name==null) {
					fail=false;
					Dbrow newrow= new Dbrow(payerEmail, Long.toHexString(rnd.nextLong()), md5(payerEmail), md5(payerEmail+props.getProperty("id2_salt")),true);
					List<Dbrow> rows=db.getLst();
					if (debugmode) {
						payerEmail=props.getProperty("error_to");
					}
					if (rows.contains(newrow)) {
						//log.log(Level.WARNING,"DUPLICATE PAYMENT, item: "+strb.toString());
						logger.error("DUPLICATE PAYMENT, item: "+strb.toString());
						if (!SendMail(payerEmail,rows.get(rows.lastIndexOf(newrow)).getAss())) {
							logger.error("Could not send email to: "+payerEmail);
						}
					}
					else {
						//log.log(Level.INFO,"business, item: "+strb.toString());
						logger.info("business, item: "+strb.toString());
						if (!SendMail(payerEmail,newrow.getAss())) {
							logger.error("Could not send email to: "+payerEmail);
							newrow.setNotifysent(false);
						}
						rows.add(newrow);
						DeflaterOutputStream dcos = new DeflaterOutputStream(cri.encryptstream(new FileOutputStream(props.getProperty("dbfile"))), new Deflater(Deflater.DEFAULT_COMPRESSION));
						//GZIPOutputStream gos=new GZIPOutputStream(cri.encryptstream(new FileOutputStream(save.getSelectedFile())));
						DataOutputStream dos= new DataOutputStream(dcos);
						db.writeToStream(dos);
						//System.out.println(db.getLst().size());
						dcos.finish();
						dos.close();
						uploadFile(dbxUp,new File(props.getProperty("dbfile")), props.getProperty("upload_destination"));
					}
				}
			}
			if (fail) {
				//log.log(Level.WARNING,"FAILED, item: "+strb.toString());
				logger.error("FAILED, item: "+strb.toString());
				//System.out.println("Fail!"+strb.toString());
			}
		}
	}
	
    private static void uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(new Date(localFile.lastModified()))
                .uploadAndFinish(in);

            //System.out.println(metadata.toStringMultiline());
        } catch ( DbxException | IOException ex) {
        	logger.error(ex);
        }
    }

	
	private boolean SendMail(String to, String pass/*, File filename*/) {
		Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(props.getProperty("usr"),props.getProperty("psw"));
			}
		});

		try {

			Message message = new MimeMessage(session,new BufferedInputStream(new FileInputStream(props.getProperty("templateeml"))));
			message.setFrom(new InternetAddress(props.getProperty("usr")));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
			message.setSubject("game addon password: "+pass);
			//MimeBodyPart mbp1 = new MimeBodyPart();
			//mbp1.setText("Csipp csippp");
			//MimeBodyPart mbp2 = new MimeBodyPart();
			//FileDataSource fds = new FileDataSource(filename);
			//mbp2.setDataHandler(new DataHandler(fds));
			//mbp2.setFileName(fds.getName());
			//Multipart mp = new MimeMultipart();
			//mp.addBodyPart(mbp1);
			//mp.addBodyPart(mbp2);
			//message.setContent(mp);
			message.setSentDate(new Date());
			Transport.send(message);
			//log.log(Level.INFO,"Mail sent to:"+to);
			logger.info("Mail sent to:"+to);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			//log.log(Level.SEVERE,e.getMessage() );
			logger.error(e.getMessage());
			return false;
		}
	}
	
	private String md5(String s) {
	    try {
	        MessageDigest m = MessageDigest.getInstance("MD5");
	        m.update(s.getBytes(), 0, s.length());
	        BigInteger i = new BigInteger(1,m.digest());
	        return String.format("%1$032x", i);         
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
public static void main(String[] args) {
	if (args.length==4) {
		DbxUploader.createtoken(args);
	}
	else {
	PaypalListenerServlet PPlst= new PaypalListenerServlet();
	 Server server= new Server(Integer.parseInt(PPlst.getProps().getProperty("listen_port")));
	 ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	 context.setContextPath("/");
     //EnumSet<DispatcherType> FASZ = EnumSet.allOf(DispatcherType.class);
     //context.addFilter(new FilterHolder(EncodingFilter.class),"/",FASZ);
     server.setHandler(context);
     context.addServlet(new ServletHolder(PPlst),"/*");
     /*FilterMapping fm= new FilterMapping();
     fm.setDispatches(1);
     fm.setFilterName(EncodingFilter.class.getCanonicalName());
     fm.setPathSpec("/*");*/
	try {
		server.start();
		server.join();
	} catch (InterruptedException e) {
		logger.error(e.getMessage());
		e.printStackTrace();
	} catch (Exception e) {
		logger.fatal(e.getMessage());
		e.printStackTrace();
	}
}
}
}

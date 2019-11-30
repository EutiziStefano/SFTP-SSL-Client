package main;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.connectors.FTPProxyConnector;
import it.sauronsoftware.ftp4j.connectors.HTTPTunnelConnector;
import it.sauronsoftware.ftp4j.connectors.SOCKS4Connector;
import it.sauronsoftware.ftp4j.connectors.SOCKS5Connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class trasferisci {

	/**
	 * @param args
	 * @throws FTPListParseException 
	 */
	public static void main(String[] args) throws FTPListParseException {

		FTPClient  client=null;

		try {
			Properties properties = new Properties();
			String TIPO_TRAFERIMENTO = "";
			String IP = "";
			String PORTA="21";
			String USER = "";
			String PW = "";
			String NUMERO_FILE = "";
			String DINAMICO = "";
			String FORMATO = "";
			String REMOTE_FOLDER = "";
			String LOCAL_FOLDER = "";
			String PERMESSI = "";
			String COMANDO = "";
			String LOGLEVEL = "";
			int NUMERO_RETRY_ERROR = 100;

			String is_set_PERMESSI = "";
			String is_set_COMANDO = "";
			String PASSIVE = "";
			String TRANSFER_TYPE="AUTO";
			String DELETE_SOURCE_FILES="AUTO";
			String SSL="false";
			String SSL_TYPE="IMPLICIT"; // IMPLICIT | EXPLICIT

			String proxy="false";
			String proxy_type="";
			String proxy_ip="";
			String proxy_port="";

			String proxy_auth="";
			String proxy_user="";
			String proxy_pass="";


			try{
				properties.load(new FileInputStream("configuration.properties"));

				TIPO_TRAFERIMENTO = properties.getProperty("TIPO_TRAFERIMENTO");
				IP = properties.getProperty("IP");
				PORTA = properties.getProperty("PORTA");
				USER = properties.getProperty("USER");
				PW = properties.getProperty("PW");
				NUMERO_FILE = properties.getProperty("NUMERO_FILE");
				DINAMICO = properties.getProperty("TRASFERIMENTO_FILE");
				FORMATO = properties.getProperty("FORMATO");
				REMOTE_FOLDER = properties.getProperty("REMOTE_FOLDER");
				LOCAL_FOLDER = properties.getProperty("LOCAL_FOLDER");
				PERMESSI = properties.getProperty("PERMESSI");
				COMANDO = properties.getProperty("COMANDO");
				is_set_PERMESSI = properties.getProperty("is_set_PERMESSI");
				is_set_COMANDO = properties.getProperty("is_set_COMANDO");
				PASSIVE = properties.getProperty("PASSIVE");
				TRANSFER_TYPE=properties.getProperty("TRANSFER_TYPE");
				DELETE_SOURCE_FILES=properties.getProperty("DELETE_SOURCE_FILES");
				LOGLEVEL=properties.getProperty("LOGLEVEL");
				NUMERO_RETRY_ERROR=Integer.parseInt(properties.getProperty("NUMERO_RETRY_ERROR"));
				SSL=properties.getProperty("SSL");
				SSL_TYPE=properties.getProperty("SSL_TYPE");
				proxy=properties.getProperty("proxy");
				proxy_type=properties.getProperty("proxy_type");
				proxy_ip=properties.getProperty("proxy_ip");
				proxy_port=properties.getProperty("proxy_port");
				proxy_auth=properties.getProperty("proxy_auth");
				proxy_user=properties.getProperty("proxy_user");
				proxy_pass=properties.getProperty("proxy_pass");
			}
			catch (IOException e) {
				System.out.println("###########################");
				System.out.println("Properties file load error");
				System.out.println("###########################");
				e.printStackTrace();
				System.exit(1);
			}

			boolean debug=false;
			if(LOGLEVEL.equals("DEBUG"))
				debug=true;

			//CONNESSIONE
			client = new FTPClient();

			// SE CONNESSIONE SSL
			if(!SSL.equals("false")){
				System.out.println("Uso SSL "+ SSL_TYPE);
				TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				} };

				SSLContext sslContext = null;
				try {
					sslContext = SSLContext.getInstance(SSL);
					sslContext.init(null, trustManager, new SecureRandom());
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					System.exit(1);
				} catch (KeyManagementException e) {
					e.printStackTrace();
					System.exit(1);
				}
				SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				client.setSSLSocketFactory(sslSocketFactory);

				if(SSL_TYPE.equals("IMPLICIT"))
					client.setSecurity(FTPClient.SECURITY_FTPS); // implicito 
				else
					client.setSecurity(FTPClient.SECURITY_FTPES); // esplicito
			}

			// se proxy
			if(proxy.equals("true")){
				System.out.println("Uso il Proxy "+ proxy_ip+" Porta "+proxy_port);
				if(proxy_type.equals("HTTPTunnelConnector")){
					if(proxy_auth.equals("true"))
						client.setConnector(new HTTPTunnelConnector(proxy_ip, Integer.parseInt(proxy_port),proxy_user,proxy_pass));
					else
						client.setConnector(new HTTPTunnelConnector(proxy_ip, Integer.parseInt(proxy_port)));
				}else if(proxy_type.equals("FTPProxyConnector")){
					if(proxy_auth.equals("true"))
						client.setConnector(new FTPProxyConnector(proxy_ip, Integer.parseInt(proxy_port),proxy_user,proxy_pass));
					else
						client.setConnector(new FTPProxyConnector(proxy_ip, Integer.parseInt(proxy_port)));

				}else if(proxy_type.equals("SOCKS4Connector")){
					if(proxy_auth.equals("true"))
						client.setConnector(new SOCKS4Connector(proxy_ip, Integer.parseInt(proxy_port),proxy_user));
					else
						client.setConnector(new SOCKS4Connector(proxy_ip, Integer.parseInt(proxy_port)));
				}else if(proxy_type.equals("SOCKS5Connector")){
					if(proxy_auth.equals("true"))
						client.setConnector(new SOCKS5Connector(proxy_ip, Integer.parseInt(proxy_port),proxy_user,proxy_pass));
					else	
						client.setConnector(new SOCKS5Connector(proxy_ip, Integer.parseInt(proxy_port)));
				}
			}

			client.connect(IP, Integer.valueOf(PORTA));
			client.login(USER, PW);

			client.setPassive(new Boolean(PASSIVE).booleanValue());

			if(TRANSFER_TYPE.equals("AUTO"))
				client.setType(FTPClient.TYPE_AUTO);
			else if(TRANSFER_TYPE.equals("BINARY"))
				client.setType(FTPClient.TYPE_BINARY);
			else if(TRANSFER_TYPE.equals("TEXTUAL"))
				client.setType(FTPClient.TYPE_TEXTUAL);
//###############################################################################
			FTPFile[] list = client.list();
			
			for (FTPFile ftpFile : list) {
				
				System.out.println("FILES TO TRANSFER:" + ftpFile.getName());
			}
//###############################################################################			
			// COSTRUISCO DUE ARRAY CON I FILE SORGENTE E DESTINAZIONE
			int numero_files=Integer.parseInt(NUMERO_FILE);

			String[] sorgenti=new String[numero_files];
			String[] destinazioni=new String[numero_files];

			/***************************************************************************
			 *       					DYNAMIC FILE TRANSFER
			 ****************************************************************************/

			if(DINAMICO.equals("dinamico")){
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat(FORMATO);
				String today_formattato = formatter.format(date);

				System.out.println("Data costruita: "+today_formattato);

				for(int i=1;i<=numero_files;i++){
					String sorgente=properties.getProperty("ORIGINE_FILE_"+i);
					String destinazione=properties.getProperty("DESTINAZIONE_FILE_"+i);

					//System.out.println("Sorgente: "+sorgente+" - Destinazione: "+destinazione+" - i: "+i);
					sorgenti[i-1]=sorgente.replaceAll("%data%", today_formattato);
					destinazioni[i-1]=destinazione.replaceAll("%data%", today_formattato);
				}
			}

			/***************************************************************************
			 *       					ALL_FILES  TYPE  TRANSFER
			 ****************************************************************************/

			else if(DINAMICO.equals("all_files")){

				if(TIPO_TRAFERIMENTO.equals("download")){
					String f=LOCAL_FOLDER;
					client.changeDirectory(REMOTE_FOLDER);
					FTPFile[] ls_file=new FTPFile[1];
					try {
						ls_file = client.list();
					} catch (FTPListParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					}

					sorgenti=new String[ls_file.length-2];
					destinazioni=new String[ls_file.length-2];

					int count=0;

					for (int i=0;i<ls_file.length;i++){
						if(!ls_file[i].getName().equals(".") && !ls_file[i].getName().equals("..")){
							sorgenti[count]=ls_file[i].getName();
							destinazioni[count]=ls_file[i].getName();
							count++;
						}
					}


				}
				else if(TIPO_TRAFERIMENTO.equals("upload")) {
					// todo: upload di tutti i files in una cartella

					File[] ls_file=new File(LOCAL_FOLDER).listFiles();

					if(debug){
						stampa_array_file(ls_file);
					}

					sorgenti=new String[ls_file.length];
					destinazioni=new String[ls_file.length];

					int count=0;

					for (int i=0;i<ls_file.length;i++){
						if(!ls_file[i].getName().equals(".") && !ls_file[i].getName().equals("..")){
							sorgenti[count]=ls_file[i].getName();
							destinazioni[count]=ls_file[i].getName();
							count++;
						}
					}
				}
			}

			/***************************************************************************
			 *       					STATIC FILE TRANSFER
			 ****************************************************************************/
			else if(DINAMICO.equals("statico")){
				for(int i=1;i<=numero_files;i++){

					if(TIPO_TRAFERIMENTO.equals("download")){
						String sorgente=properties.getProperty("ORIGINE_FILE_"+i);
						String destinazione=properties.getProperty("DESTINAZIONE_FILE_"+i);

						sorgenti[i-1]=sorgente;
						destinazioni[i-1]=destinazione;
					}
					else if(TIPO_TRAFERIMENTO.equals("upload")){
						String sorgente=properties.getProperty("ORIGINE_FILE_"+i);
						String destinazione=properties.getProperty("DESTINAZIONE_FILE_"+i);

						sorgenti[i-1]=sorgente;
						destinazioni[i-1]=destinazione;
					}					
				}

			}

			//stampa_array(sorgenti);
			//stampa_array(destinazioni);

			boolean delete_source=new Boolean(DELETE_SOURCE_FILES).booleanValue();

			int tentativi=0;
			
			for(int i=0;i<sorgenti.length && tentativi<NUMERO_RETRY_ERROR;i++){

				String s=sorgenti[i];
				String d=destinazioni[i];

				client.changeDirectory(REMOTE_FOLDER);

				System.out.println(TIPO_TRAFERIMENTO+" file remoto: "+REMOTE_FOLDER+s+" locale: "+LOCAL_FOLDER+d);

				boolean inviato=false;
				while(inviato==false && tentativi<NUMERO_RETRY_ERROR){
					
					try{
						if(TIPO_TRAFERIMENTO.equals("download")){
							String f=LOCAL_FOLDER+d;
							client.download(s, new java.io.File(f));

							//Eliminazione sorgente
							if(delete_source){
								client.deleteFile(REMOTE_FOLDER+s);
								System.out.println("Eliminato file: "+REMOTE_FOLDER+s);
							}
							if(is_set_PERMESSI.equals("true")){
								Runtime.getRuntime().exec("chmod "+PERMESSI+" "+f);
							}
							if(is_set_COMANDO.equals("true")){
								String comando_dopo_replace=COMANDO.replaceAll("%file%", f);
								Runtime.getRuntime().exec(comando_dopo_replace);
							}
						}
						else if(TIPO_TRAFERIMENTO.equals("upload")) {
							client.upload(new java.io.File(LOCAL_FOLDER+s));
							if(delete_source){
								new File(LOCAL_FOLDER+s).delete();
								System.out.println("Eliminato file: "+LOCAL_FOLDER+s);
							}
						}
						
						inviato=true;
					}
					catch(Exception e){
						tentativi++;
						System.out.println("#################################");
						System.out.println("Errore durante invio file, tentativi retry: "+tentativi+" su "+ NUMERO_RETRY_ERROR);
						System.out.println("#################################");
						e.getMessage();
					}
										
				}
												
			}

		} 
		catch (SocketTimeoutException e) {
			System.out.println("#################################");
			System.out.println("FTP CONNECTION TIMEOUT");
			System.out.println("#################################");
			e.printStackTrace();
			System.exit(1);
		}
		catch (FTPException e){
			System.out.println("###########################");
			System.out.println("FTP Connection Error");
			System.out.println("###########################");
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalStateException e) {
			System.out.println("###########################");
			System.out.println("FTP Connection Error");
			System.out.println("###########################");
			e.printStackTrace();
			e.printStackTrace();
			System.exit(1);
		} catch (FTPIllegalReplyException e) {
			System.out.println("###########################");
			System.out.println("FTP Connection Error");
			System.out.println("###########################");
			e.printStackTrace();
			System.exit(1);
		} catch (FTPDataTransferException e) {
			System.out.println("###########################");
			System.out.println("FTP Data Transfer Error");
			System.out.println("###########################");
			e.printStackTrace();
			System.exit(1);
		} catch (FTPAbortedException e) {
			System.out.println("###########################");
			System.out.println("FTP Transfer Aborted");
			System.out.println("###########################");
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e) {
			System.out.println("#################################");
			System.out.println("FILE TO TRANSFER NOT FOUND");
			System.out.println("#################################");
			e.printStackTrace();
			System.exit(1);
		}


		finally
		{
			if (client != null)
			{
				try
				{
					client.disconnect(true);
				}
				catch (Exception e)
				{
					System.err.println("ERROR : Error in disconnecting the Remote Machine");
				}
			}
		}

	}






	public static void stampa_array(String[] ar){
		for(int i=0;i<ar.length;i++){
			System.out.print(ar[i]+" - ");
		}
		System.out.println();
	}
	public static void stampa_array_file(File[] ar){
		for(int i=0;i<ar.length;i++){
			System.out.print(ar[i].getName()+" - ");
		}
		System.out.println();
	}
}

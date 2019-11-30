package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

public class testFTP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FTPClient client = null;

		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("configuration.properties"));

			String TIPO_TRAFERIMENTO = properties.getProperty("TIPO_TRAFERIMENTO");
			String IP = properties.getProperty("IP");
			String USER = properties.getProperty("USER");
			String PW = properties.getProperty("PW");
			String NUMERO_FILE = properties.getProperty("NUMERO_FILE");
			String DINAMICO = properties.getProperty("DINAMICO");
			String FORMATO = properties.getProperty("FORMATO");
			String REMOTE_FOLDER = properties.getProperty("REMOTE_FOLDER");
			String LOCAL_FOLDER = properties.getProperty("LOCAL_FOLDER");
			String PERMESSI = properties.getProperty("PERMESSI");
			String COMANDO = properties.getProperty("COMANDO");

//			CONNESSIONE
			client = new FTPClient();
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
			client.configure(conf);
			client.connect(IP);
			client.login(USER, PW);

//			COSTRUISCO DUE ARRAY CON I FILE SORGENTE E DESTINAZIONE
			int numero_files = Integer.parseInt(NUMERO_FILE);
			String[] sorgenti = new String[numero_files];
			String[] destinazioni = new String[numero_files];

			if (DINAMICO.equals("true")) {
				Date date = Calendar.getInstance().getTime();
				DateFormat formatter = new SimpleDateFormat(FORMATO);
				String today_formattato = formatter.format(date);

				System.out.println("Data costruita: " + today_formattato);

				for (int i = 1; i <= numero_files; i++) {
					String sorgente = properties.getProperty("ORIGINE_FILE_" + i);
					String destinazione = properties.getProperty("DESTINAZIONE_FILE_" + i);

					sorgenti[i - 1] = sorgente.replaceAll("%data%", today_formattato);
					destinazioni[i - 1] = destinazione.replaceAll("%data%", today_formattato);
				}
			} else {
				for (int i = 1; i <= numero_files; i++) {
					String sorgente = properties.getProperty("ORIGINE_FILE_" + i);
					String destinazione = properties.getProperty("DESTINAZIONE_FILE_" + i);

					sorgenti[i - 1] = sorgente;
					destinazioni[i - 1] = destinazione;
				}
			}

			for (int i = 0; i < sorgenti.length; i++) {

				String s = sorgenti[i];
				String d = destinazioni[i];

				client.changeWorkingDirectory(REMOTE_FOLDER);
				client.setFileType(FTPClient.BINARY_FILE_TYPE);
				System.out.println(TIPO_TRAFERIMENTO + " del file remoto: " + REMOTE_FOLDER + s + " locale: " + LOCAL_FOLDER + d);

				if (TIPO_TRAFERIMENTO.equals("download")) {
					String f = LOCAL_FOLDER + d;
					FileOutputStream fos = new FileOutputStream(f);
					client.retrieveFile(s, fos);
					Runtime.getRuntime().exec("chmod " + PERMESSI + " " + f);
					String comando_dopo_replace = COMANDO.replaceAll("%file%", f);
					Runtime.getRuntime().exec(comando_dopo_replace);
				} else if (TIPO_TRAFERIMENTO.equals("upload")) {
					String lfile = LOCAL_FOLDER + s;
					File fi = new File(lfile);
					FileInputStream fin = new FileInputStream(fi);
					client.storeFile(s, fin);
				}
			}
		} catch (IOException e) {
			System.out.println("###########################");
			System.out.println("Properties File Not Found");
			System.out.println("###########################");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			System.out.println("###########################");
			System.out.println("FTP Connection Error");
			System.out.println("###########################");
			e.printStackTrace();
			e.printStackTrace();
		} finally {
			if (client != null) {
				try {
					client.disconnect();
				} catch (Exception e) {
					System.err.println("ERROR : Error in disconnecting the Remote Machine");
				}
			}
		}
	}
}

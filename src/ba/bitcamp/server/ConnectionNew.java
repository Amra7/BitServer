package ba.bitcamp.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import ba.bitcamp.logger.Logger;

public class ConnectionNew implements Runnable {

	private Socket client;

	/**
	 * @param client
	 */
	public ConnectionNew(Socket client) {
		super();
		this.client = client;
	}

	@Override
	public void run() {
		BufferedReader read = null;
		PrintStream write = null;

		try {

			read = new BufferedReader(new InputStreamReader(
					client.getInputStream()));

			write = new PrintStream(client.getOutputStream());
		} catch (IOException e1) {
			Logger.log("error", e1.getMessage());
			try {
				client.close();
			} catch (IOException e) {
				Logger.log("warning", e.getMessage());

			}
			return;
		}
		String line = "";
		String tempLine ="";
		
		try {

			
			while ((tempLine = read.readLine()) != null) {
				if (tempLine.contains("GET") || tempLine.contains("POST")) {
					line = tempLine;
					// System.out.println("We found get!");
					if(line.contains("GET"))
						break;
					
			    //  provremeno izkomentrirano
				//	break;
				}
				
				System.out.println(tempLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			if (!line.contains("GET")) {
				Logger.log("warning", "Was not GET request!");
				ResponseNew.error(write, "Invalid request"); // browseru odgovaramo
															// "Invalid request"
				closeClient();
				return;
			}
			String fileName = getFileName(line);
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				ResponseNew.error(write, "This is not page you are looking for");
				Logger.log("warning", "Client requested missing file" + e.getMessage());
				closeClient();
				return;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String fileLine = "";
			StringBuilder sb = new StringBuilder();
			try {
				while ((fileLine = br.readLine()) != null) {
					sb.append(fileLine);
				}
			} catch (IOException e) {
				//ovdje se desio 500 Internal server error
				
				Logger.log("error", e.getMessage());
				ResponseNew.serverError(write, "A wll tryied group of monkeys to fix the problem!");
				closeClient();
				return;
			}
			
			ResponseNew.ok(write, sb.toString());
			closeClient();

	}
	
	private void closeClient(){
		try {
			client.close();
		} catch (IOException e) {
			Logger.log("warning", e.getMessage());
		}
	}

	private String getFileName(String request) {
		String[] parts = request.split(" ");
		String fileName = null;
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals("GET")) {
				// predpostaljamo da je sljedeci dio niza parts ime fajla
				fileName = parts[i + 1];
				break;
			}
		}

		String basePath = "."+File.separator+"html"+File.separator;
		if (fileName == null || fileName.equals("/")) {
			// ako je posalo nesto sto nema validan argument saljemo ga u
			// index.html file
			return basePath + "index.html";
		}

		// mi predpostaljamo da ce file biti tipa .html bez obzira sta je iza tacke
		if (fileName.contains(".")) {
			fileName += ".html";
		}
		return fileName;
	}

}

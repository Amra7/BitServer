package ba.bitcamp.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import ba.bitcamp.logger.Logger;

public class Connection implements Runnable {

	private Socket client;

	/**
	 * @param client
	 */
	public Connection(Socket client) {
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

		try {

			String line = "";
			while ((line = read.readLine()) != null) {
				if (line.contains("GET") || line.isEmpty()) {
					// System.out.println("We found get!");
					break;
				}
			}
			if (!line.contains("GET")) {
				Logger.log("warning", "Was not GET request!");
				Response.error(write, "Invalid request"); // browseru odgovaramo
															// "Invalid request"
				client.close();
				return;
			}
			String fileName = line.split(" ")[1];
			if (fileName.equals("/")) {
				fileName = "index.html";

				// Ako je zaboravio ekstenziju dodati mi mu dodajemo, a prije
				// toga predpostavljamo da postoji file koji ima na serveru
			} else if (!fileName.contains("html")) {
				fileName += ".html";
			}
			
			String html ="";
			FileInputStream fis = new FileInputStream("./HTML/" + fileName);
			BufferedReader br  =new BufferedReader(new InputStreamReader(fis));
			
			String fileLine ="";
			while ( (fileLine=br.readLine()) != null){
				html += fileLine;
			}
			

			System.out.println("Dobili : " + line);
			// ovo smo koristili da nam iybaci Test na stranici
//			Response.ok(write, "<h1>Test</h1>");
			Response.ok(write, html);

			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

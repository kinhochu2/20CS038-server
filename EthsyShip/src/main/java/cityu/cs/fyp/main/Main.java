package cityu.cs.fyp.main;

import java.io.IOException;

import cityu.cs.fyp.http.HttpController;

public class Main {
	public static void main(String[] args) {
		try {
			HttpController.createHttpServer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

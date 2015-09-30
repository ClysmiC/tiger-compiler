package com.tiger.compiler;

public class test {

	public static void main(String[] args) {
		TScanner tScanner = new TScanner();
		Tuple<TokenClass, String> token;
		while ((token = tScanner.nextToken()) != null) {
			System.out.println(token);
		}
	}
}
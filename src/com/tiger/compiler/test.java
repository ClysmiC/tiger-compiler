package com.tiger.compiler;

public class test {

	public static void main(String[] args) {
		TScanner tScanner = new TScanner();
		Tuple<TokenClass, String> token;
		while (true)
		{
			token = tScanner.nextToken();
			System.out.println(token);

			if(token.x == TokenClass.EOF)
				System.exit(0);
		}
	}
}
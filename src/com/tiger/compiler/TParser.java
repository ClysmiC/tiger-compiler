package com.tiger.compiler;

import java.io.FileInputStream;

public class TParser {

	private final String DEFAULT_FILENAME = "res/example1.tiger";
	private String fileName;
	private FileInputStream fileInput;

	public TParser(String filePath) {
		this.fileName = filePath;
		try {
			this.fileInput = new FileInputStream(this.fileName);
		} catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public String parse() {
		try {
			int r;
			while ((r = fileInput.read()) != -1) {
				char c = (char) r;
				System.out.print(c);
			}
		} catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	public static void main(String[] args) {
		TParser tParser;
		if (args.length == 0) {
			System.out.println("No program specified, \"res/exmple1.tiger\"")
			tParser = new TParser(DEFAULT_FILENAME);
		} else if (args.length > 1) {
			System.out.println("main in the TParser class takes in only 1 argument");
			System.exit();
		} else {
			String fileName = (String) args[0];
			tParser = new (fileName);
		}
		tParser.parse();
	}

}
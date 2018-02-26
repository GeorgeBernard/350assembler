package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import assembulator.Assembler;
import assembulator.Assembulator;

/**
 * The main entry point for the assembler program
 * 
 * @author george
 */
public final class Main {
	public static void main(String[] args) {
		String filename;
		String extension;
		if (args.length == 0) {
			Scanner read = new Scanner(System.in);
			do {
                System.out.print("Please type the filename: ");
                filename = read.nextLine();
                extension = filename.substring(filename.lastIndexOf('.'));
            }while(!filename.isEmpty() || !extension.equals(".mips"));
			read.close();
		} else {
			filename = args[0];
		}
		
		try {
            String outName;
            String cwd = System.getProperty("user.dir");
            if(args.length == 2){
                outName = args[1].substring(0, args[1].lastIndexOf('.')) + ".mif";
            } else {
                outName = "imem.mif";
            }
            String output = String.join(File.separator, cwd, outName);
            FileOutputStream fos = new FileOutputStream(new File(output));
			Assembler a = new Assembulator(filename);
			a.writeTo(fos);
			a.writeTo(System.out);
		} catch (FileNotFoundException e) {
			System.err.println("output file not found");
		} 

	}
}

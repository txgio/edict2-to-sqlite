package io.txg;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Edict2ToSqlite {

	public static void main(String[] args) throws ParseException {
		System.out.println("Edict2ToSqlite");
		Options options = new Options();

		options.addOption(Option.builder("s").required().longOpt("source").desc("edict2 source file path").hasArg()
				.argName("file").build());
		options.addOption(Option.builder("t").required().longOpt("target").desc("sqlite target file path").hasArg()
				.argName("file").build());

		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			String sourceFilePath = cmd.getOptionValue("s");
			String targetFilePath = cmd.getOptionValue("t");

			File sourceFile = new File(sourceFilePath);
			System.out.print("Source File: " + sourceFilePath);
			if (sourceFile.exists()) {
				System.out.print(" (exists)");
				System.out.println();
			} else {
				System.out.println();
				System.err.println("The file doesn't exist");
				System.exit(1);
			}

			System.out.print("Target File: " + targetFilePath);
			File targetFile = new File(targetFilePath);
			if (targetFile.exists()) {
				if (targetFile.canWrite()) {
					if (!targetFile.delete()) {
						System.out.println();
						System.err.println("The file cannot be deleted");
						System.exit(3);
					}
				}
			}

			Edict2ToSqliteConvert converter = new Edict2ToSqliteConvert(sourceFile, targetFilePath);
			converter.convert();
		} catch (MissingOptionException moe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("edict2-to-sqlite", options);
		}
	}

}

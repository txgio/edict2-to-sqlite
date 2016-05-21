package io.txg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

import com.mariten.kanatools.KanaAppraiser;
import com.mariten.kanatools.KanaConverter;

public class Edict2ToSqliteConvert {

	private File sourceFile;
	private String targetFilePath;

	private long numberOfEntries = 0;
	private long total;
	private long current;
	private Writer writer;

	private static final String sql = "INSERT INTO dict (kanji, kana, entry, full_entry, extra) VALUES (?, ?, ?, ?, ?)";

	public Edict2ToSqliteConvert(File sourceFile, String targetFilePath) {
		this.sourceFile = sourceFile;
		this.targetFilePath = targetFilePath;
	}

	public void convert() {
		try (Stream<String> lines = Files.lines(Paths.get(sourceFile.getAbsolutePath()), StandardCharsets.UTF_8);
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + targetFilePath)) {
			total = Files.lines(Paths.get(sourceFile.getAbsolutePath()), StandardCharsets.UTF_8).count();
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE dict (kanji TEXT, kana TEXT, entry TEXT, full_entry TEXT, extra TEXT)";
			stmt.executeUpdate(sql);
			stmt.close();

			stmt = connection.createStatement();
			sql = "CREATE INDEX ix_kana ON dict (kana ASC)";
			stmt.executeUpdate(sql);
			stmt.close();

			stmt = connection.createStatement();
			sql = "CREATE INDEX ix_kanji ON dict (kanji ASC)";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		try (Stream<String> lines = Files.lines(Paths.get(sourceFile.getAbsolutePath()), StandardCharsets.UTF_8);
				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + targetFilePath);
				Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dict.txt"), "utf-8"))) {
			connection.setAutoCommit(false);
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				this.writer = writer;
				lines.forEachOrdered(line -> {
					parseLine(preparedStatement, line);
				});
				System.out.println(numberOfEntries);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void parseLine(PreparedStatement ps, String line) {
		// System.out.println("Line:" + line);
		if (!line.contains("Copyright Electronic Dictionary Research")) {
			String[] subs = line.split(" /");
			if (subs.length > 0) {
				Entry entry = new Entry();
				entry.setLine(line);
				String fullEntry = subs[0];
				entry.setFullKanjiAndKana(fullEntry);
				String extra = line.substring(line.lastIndexOf("/Ent") + 1, line.length() - 1);
				entry.setExtra(extra);
				// System.out.println("Entry:" + fullEntry);
				if (fullEntry.contains("[") && fullEntry.contains("]")) {
					String fullKanji = fullEntry.substring(0, fullEntry.indexOf('[') - 1);
					String[] kanjiSubs = fullKanji.split(";");
					if (kanjiSubs.length == 1) {
						// System.out.println(kanjiSubs[0]);
						entry.addKanjis(kanjiSubs[0]);
					} else if (kanjiSubs.length > 1) {
						// System.out.println(String.join(" ", kanjiSubs));
						entry.addKanjis(kanjiSubs);
					} else {
						// System.out.println("none");
					}
					String fullKana = fullEntry.substring(fullEntry.indexOf('[') + 1, fullEntry.indexOf(']'));
					// System.out.println(fullKana);
					String[] kanaSubs = fullKana.split(";");
					if (kanaSubs.length == 1) {
						// System.out.println(kanaSubs[0]);
						entry.addKanas(kanaSubs[0]);
					} else if (kanaSubs.length > 1) {
						// System.out.println(String.join(" ", kanaSubs));
						entry.addKanas(kanaSubs);
					} else {
						// System.out.println("none kana");
					}
				} else {
					// System.out.println("only kana:" + fullEntry);
					String[] kanaSubs = fullEntry.split(";");
					if (kanaSubs.length == 1) {
						// System.out.println(kanaSubs[0]);
						entry.addKanas(kanaSubs[0]);
					} else if (kanaSubs.length > 1) {
						// System.out.println(String.join("###", kanaSubs));
						entry.addKanas(kanaSubs);
					} else {
						// System.out.println("none kana");
					}
				}
				String meaning = line.substring(line.indexOf('/') + 1, line.lastIndexOf("/Ent"));
				// System.out.println(meaning);
				entry.setMeaning(meaning);
				// System.out.println(entry);
				insertEntry(ps, entry);
				current++;
				System.out.print(current + "/" + total + " (");
				System.out.printf("%.2f", ((float) current / total) * 100);
				System.out.println(")");
			}
		} else {
			System.out.println("first line");
		}
	}

	private void insertEntry(PreparedStatement ps, Entry entry) {
		if (entry.getKanjis().size() > 0) {
			for (String kanji : entry.getKanjis()) {
				String trimmedKanji = kanji.replaceAll("\\([a-zA-Z]*?\\)", "");
				for (String kana : entry.getKanas()) {
					String trimmedKana = kana.replaceAll("\\([a-zA-Z]*?\\)", "");
					if (trimmedKana.contains("(") && trimmedKana.contains(")")) {
						String betweenParenthesis = trimmedKana.substring(trimmedKana.indexOf('(') + 1,
								trimmedKana.indexOf(')'));
						// System.out.println(betweenParenthesis);
						if (betweenParenthesis.contains(trimmedKanji)) {
							String cleanedKana = trimmedKana.replaceAll("\\(.*?\\)", "");
							if (hasKatakana(cleanedKana)) {
								System.out.println("Case 4");
								insertRow(ps, convertKatakanaToHiragana(trimmedKanji),
										convertKatakanaToHiragana(cleanedKana),
										trimmedKanji + " [" + cleanedKana + "] /" + entry.getMeaning() + "/",
										entry.getFullKanjiAndKana(), entry.getExtra());
							} else {
								System.out.println("Case 1");
								insertRow(ps, trimmedKanji, cleanedKana, entry.getMeaning(),
										entry.getFullKanjiAndKana(), entry.getExtra());
							}
						}
					} else {
						if (hasKatakana(trimmedKana)) {
							System.out.println("Case 5");
							insertRow(ps, convertKatakanaToHiragana(trimmedKanji),
									convertKatakanaToHiragana(trimmedKana),
									trimmedKanji + " [" + trimmedKana + "] /" + entry.getMeaning() + "/",
									entry.getFullKanjiAndKana(), entry.getExtra());
						} else {
							System.out.println("Case 2");
							insertRow(ps, trimmedKanji, trimmedKana, entry.getMeaning(), entry.getFullKanjiAndKana(),
									entry.getExtra());
						}
					}
				}
			}
		} else {
			for (String kana : entry.getKanas()) {
				String trimmedKana = kana.replaceAll("\\(.*?\\)", "");
				if (hasKatakana(trimmedKana)) {
					System.out.println("Case 6");
					insertRow(ps, null, convertKatakanaToHiragana(trimmedKana),
							trimmedKana + " /" + entry.getMeaning() + "/", entry.getFullKanjiAndKana(),
							entry.getExtra());
				} else {
					System.out.println("Case 3");
					insertRow(ps, null, trimmedKana, entry.getMeaning(), entry.getFullKanjiAndKana(), entry.getExtra());
				}
			}
		}
	}

	private String convertKatakanaToHiragana(String kana) {
		return KanaConverter.convertKana(kana, KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA);
	}

	private boolean hasKatakana(String trimmedKana) {
		for (char c : trimmedKana.toCharArray()) {
			System.out.println(c);
			if (KanaAppraiser.isZenkakuKatakana(c)) {
				System.out.println("is katakana");
				return true;
			}
		}
		return false;
	}

	private void insertRow(PreparedStatement ps, String kanji, String kana, String meaning, String full, String extra) {
		System.out.println(meaning);
		// if (writer != null) {
		// try {
		// if (kanji != null) writer.write(kanji);
		// writer.write('\t');
		// if (kana != null) writer.write(kana);
		// writer.write('\t');
		// if (meaning != null)writer.write(meaning);
		// writer.write('\t');
		// if (full != null) writer.write(full);
		// writer.write('\t');
		// if (extra != null)writer.write(extra);
		// writer.write('\n');
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }
		// }
		try {
			ps.setString(1, kanji);
			ps.setString(2, kana);
			ps.setString(3, meaning);
			ps.setString(4, full);
			ps.setString(5, extra);

			ps.executeUpdate();
			ps.clearParameters();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

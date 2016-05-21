package io.txg;

public class Edict2LineParser {

	public Entry parse(String line) {
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
//						insertEntry(ps, entry);
//						current++;
//						System.out.print(current + "/" + total + " (");
//						System.out.printf("%.2f", ((float) current / total) * 100);
//						System.out.println(")");
						return entry;
					}
					return null;
				} else {
//					System.out.println("first line");
					return null;
				}
	}

}

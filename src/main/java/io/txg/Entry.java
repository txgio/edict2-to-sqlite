package io.txg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Entry {

	private String line;
	private String fullKanjiAndKana;
	private List<String> kanjis = new ArrayList<>();
	private List<String> kanas = new ArrayList<>();
	private String meaning;
	private String extra;

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getFullKanjiAndKana() {
		return fullKanjiAndKana;
	}

	public void setFullKanjiAndKana(String fullKanjiAndKana) {
		this.fullKanjiAndKana = fullKanjiAndKana;
	}

	public List<String> getKanjis() {
		return kanjis;
	}

	public void addKanjis(String... kanjis) {
		this.kanjis.addAll(Arrays.asList(kanjis));
	}

	public List<String> getKanas() {
		return kanas;
	}

	public void addKanas(String... kanas) {
		this.kanas.addAll(Arrays.asList(kanas));
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(line + "\n");
		buffer.append(fullKanjiAndKana + "\n");
		buffer.append(String.join("#;#", kanjis) + "\n");
		buffer.append(String.join("#;#", kanas) + "\n");
		buffer.append(meaning + "\n");
		buffer.append(extra + "\n");
		return buffer.toString();
	}

}

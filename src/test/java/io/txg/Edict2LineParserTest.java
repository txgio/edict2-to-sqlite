package io.txg;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Edict2LineParserTest {

	private static final String LINE_01 = "企業 [きぎょう] /(n) enterprise/undertaking/corporation/business/(P)/EntL1218190X/";

	private Edict2LineParser edict2LineParser;

	@Before
	public void setUp() {
		edict2LineParser = new Edict2LineParser();
	}

	@Test
	public void testParseLinePattern01() {
		Entry entry = edict2LineParser.parse(LINE_01);
		Assert.assertNotNull("Should not be null", entry);
		Assert.assertEquals("Should have detected one kanji", 1, entry.getKanjis().size());
		Assert.assertEquals("Should have detected the kanji", "企業", entry.getKanjis().get(0));

		Assert.assertEquals("Should have detected one kana", 1, entry.getKanas().size());
		Assert.assertEquals("Should have detected the kana", "きぎょう", entry.getKanas().get(0));

		Assert.assertEquals("Should have detected the full kanji and kana", "企業 [きぎょう]", entry.getFullKanjiAndKana());

		Assert.assertEquals("Should have detected the extra", "EntL1218190X", entry.getExtra());

		Assert.assertEquals("Should have detected the meaning", "(n) enterprise/undertaking/corporation/business/(P)",
				entry.getMeaning());

		Assert.assertEquals("Should have detected the full line", LINE_01, entry.getLine());
	}

}

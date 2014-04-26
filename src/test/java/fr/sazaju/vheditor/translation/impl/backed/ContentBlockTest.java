package fr.sazaju.vheditor.translation.impl.backed;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ContentBlockTest {

	@Test
	public void testJapContentWithConsistentNewline() {
		ContentBlock block = new ContentBlock();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			{
				String content = "ライム" + newline + "（……いくら宿のためとはいえ、" + newline
						+ "　人間に媚びるなんて……イヤ……）";
				block.setContent(content + newline);
				assertEquals(escape(newline), content, block
						.getContentWithoutNewline().getContent());
			}
			{
				String content = newline + "\\>冒険者ギルドの世界ランキングだ";
				block.setContent(content + newline);
				assertEquals(escape(newline), content, block
						.getContentWithoutNewline().getContent());
			}
		}
	}

	@Test
	public void testJapContentWithForcedNewline() {
		ContentBlock block = new ContentBlock();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			{
				String content = "ライム\n（……いくら宿のためとはいえ、\n　人間に媚びるなんて……イヤ……）";
				block.setContent(content + newline);
				assertEquals(escape(newline), content, block
						.getContentWithoutNewline().getContent());
			}
			{
				String content = newline + "\\>冒険者ギルドの世界ランキングだ";
				block.setContent(content + newline);
				assertEquals(escape(newline), content, block
						.getContentWithoutNewline().getContent());
			}
		}
	}

	@Test
	public void testEmptyOrNewlineContent() {
		ContentBlock block = new ContentBlock();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			{
				String content = "";
				block.setContent(content + newline);
				assertEquals(escape(newline), content, block
						.getContentWithoutNewline().getContent());
			}
			{
				block.setContent(newline + newline);
				assertEquals(escape(newline), newline, block
						.getContentWithoutNewline().getContent());
			}
		}
	}

	private String escape(String string) {
		string = string.replaceAll("\n", "\\\\n");
		string = string.replaceAll("\r", "\\\\r");
		return string;
	}
}

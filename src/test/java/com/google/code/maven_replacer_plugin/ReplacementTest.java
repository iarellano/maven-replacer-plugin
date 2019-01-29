/*
Original work Copyright (c) 2014 beiliubei
Modified work Copyright (c) 2019 Isaias Arellano - isaias.arellano.delgado@gmail.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.google.code.maven_replacer_plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.code.maven_replacer_plugin.file.FileUtils;


@RunWith(MockitoJUnitRunner.class)
public class ReplacementTest {
	private static final String UNESCAPED = "test\\n123\\t456";
	private static final String ESCAPED = "test\n123\t456";
	private static final String FILE = "some file";
	private static final String TOKEN = "token";
	private static final String VALUE = "value";
	private static final String XPATH = "xpath";
	private static final String ENCODING = "encoding";
	private static final String JSON_TYPE = "string";
	
	@Mock
	private FileUtils fileUtils;
	@Mock
	private DelimiterBuilder delimiter;

	@Test
	public void shouldReturnConstructorParameters() throws Exception {
		Replacement replacement = new Replacement(fileUtils, TOKEN, VALUE, false, null, ENCODING);
		
		assertThat(replacement.getToken(), equalTo(TOKEN));
		assertThat(replacement.getValue(), equalTo(VALUE));
		verifyZeroInteractions(fileUtils);
	}
	
	@Test
	public void shouldApplyToTokenDelimeterIfExists() throws Exception {
		when(delimiter.apply(TOKEN)).thenReturn("new token");
		Replacement replacement = new Replacement(fileUtils, TOKEN, VALUE, false, null, ENCODING, null, null).withDelimiter(delimiter);
		
		assertThat(replacement.getToken(), equalTo("new token"));
		assertThat(replacement.getValue(), equalTo(VALUE));
		verifyZeroInteractions(fileUtils);
	}
	
	@Test
	public void shouldUseEscapedTokensAndValues() {
		Replacement replacement = new Replacement(fileUtils, UNESCAPED, UNESCAPED, true, null, ENCODING);
		
		assertThat(replacement.getToken(), equalTo(ESCAPED));
		assertThat(replacement.getValue(), equalTo(ESCAPED));
		verifyZeroInteractions(fileUtils);
	}
	
	@Test
	public void shouldUseEscapedTokensAndValuesFromFiles() throws Exception {
		when(fileUtils.readFile(FILE, ENCODING)).thenReturn(UNESCAPED);

		Replacement replacement = new Replacement(fileUtils, null, null, true, null, ENCODING);
		replacement.setTokenFile(FILE);
		replacement.setValueFile(FILE);
		
		assertThat(replacement.getToken(), equalTo(ESCAPED));
		assertThat(replacement.getValue(), equalTo(ESCAPED));
	}

	@Test
	public void shouldUseTokenFromFileUtilsIfGiven() throws Exception {
		when(fileUtils.readFile(FILE, ENCODING)).thenReturn(TOKEN);

		Replacement replacement = new Replacement(fileUtils, null, VALUE, false, null, ENCODING);
		replacement.setTokenFile(FILE);
		assertThat(replacement.getToken(), equalTo(TOKEN));
		assertThat(replacement.getValue(), equalTo(VALUE));
	}

	@Test
	public void shouldUseValueFromFileUtilsIfGiven() throws Exception {
		when(fileUtils.readFile(FILE, ENCODING)).thenReturn(VALUE);

		Replacement replacement = new Replacement(fileUtils, TOKEN, null, false, null, ENCODING);
		replacement.setValueFile(FILE);
		assertThat(replacement.getToken(), equalTo(TOKEN));
		assertThat(replacement.getValue(), equalTo(VALUE));
	}
	
	@Test
	public void shouldSetAndGetSameValues() {
		Replacement replacement = new Replacement();
		
		replacement.setToken(TOKEN);
		replacement.setValue(VALUE);
		replacement.setXpath(XPATH);
		assertThat(replacement.getToken(), equalTo(TOKEN));
		assertThat(replacement.getValue(), equalTo(VALUE));
		assertThat(replacement.getXpath(), equalTo(XPATH));
	}
	
	@Test
	public void shouldReturnCopyOfReplacementInFrom() {
		Replacement replacement = new Replacement(fileUtils, TOKEN, VALUE, true, XPATH, ENCODING);
		Replacement copy = Replacement.from(replacement);
		
		assertThat(copy.getToken(), equalTo(TOKEN));
		assertThat(copy.getValue(), equalTo(VALUE));
		assertThat(copy.isUnescape(), equalTo(true));
		assertThat(copy.getXpath(), equalTo(XPATH));
	}
}

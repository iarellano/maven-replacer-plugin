/*
Copyright (c) 2019 Isaias Arellano - isaias.arellano.delgado@gmail.com

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

import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonPathReplacerTest {
	private static final int NO_FLAGS = -1;
	
	private Replacement replacement;
	private TokenReplacer tokenReplacer;
	private JsonPathReplacer replacer;
	
	@Before
	public void setUp() {
		replacement = mock(Replacement.class);
		tokenReplacer = mock(TokenReplacer.class);
		replacer = new JsonPathReplacer(tokenReplacer);
	}

	@Test
	public void replaceObjectProperty() {
		final String jsonPath = "$.root.name";
		when(replacement.getJsonpath()).thenReturn(jsonPath);
		when(replacement.getToken()).thenReturn("^(.*)$");
		when(replacement.getValue()).thenReturn("new value");
		when(replacement.getJsontype()).thenReturn("string");
		when(tokenReplacer.replace("old value", replacement, false, NO_FLAGS)).thenReturn("new value");

		String json = "{\"root\":{\"name\":\"old value\"}}";
		String result = replacer.replace(json, replacement, false, NO_FLAGS);
		String value = JsonPath.read(result, jsonPath);
		assertEquals(value, "new value");
	}

	@Test
	public void replaceObjectPropertyWithObject() {
		final String jsonPath = "$.root.name";
		when(replacement.getJsonpath()).thenReturn(jsonPath);
		when(replacement.getToken()).thenReturn("^(.*)$");
		when(replacement.getValue()).thenReturn("{\"child\":\"I am a grand child\"}");
		when(replacement.getJsontype()).thenReturn("object");
		when(tokenReplacer.replace("old value", replacement, false, NO_FLAGS)).thenReturn("{\"child\":\"I am a grand child\"}");

		String json = "{\"root\":{\"name\":\"old value\"}}";
		String result = replacer.replace(json, replacement, false, NO_FLAGS);
		String value = JsonPath.read(result, jsonPath + ".child");
		assertEquals(value, "I am a grand child");
	}
}

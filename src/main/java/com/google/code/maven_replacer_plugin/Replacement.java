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

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.code.maven_replacer_plugin.file.FileUtils;


public class Replacement {
	private final FileUtils fileUtils;
	
	private DelimiterBuilder delimiter;
	private boolean unescape;
	private String token;
	private String value;
	private String encoding;
	private String xpath;
	private String jsonpath;
	private String jsontype;

	public Replacement() {
		this.fileUtils = new FileUtils();
		this.unescape = false;
	}

	public Replacement(FileUtils fileUtils, String token, String value, boolean unescape,
			String xpath, String encoding) {
		this.fileUtils = fileUtils;
		setUnescape(unescape);
		setToken(token);
		setValue(value);
		setXpath(xpath);
		setEncoding(encoding);
	}

	public Replacement(FileUtils fileUtils, String token, String value, boolean unescape,
					   String xpath, String encoding, String jsonpath, String jsontype) {
		this(fileUtils, token, value, unescape, xpath, encoding);
		setJsonpath(jsonpath);
		setJsontype(jsontype);
	}

	public void setTokenFile(String tokenFile) throws IOException {
		if (tokenFile != null) {
			setToken(fileUtils.readFile(tokenFile, getEncoding()));
		}
	}

	public void setValueFile(String valueFile) throws IOException {
		if (valueFile != null) {
			setValue(fileUtils.readFile(valueFile, getEncoding()));
		}
	}

	public String getToken() {
		String newToken = unescape ? unescape(token) : token;
		if (delimiter != null) {
			return delimiter.apply(newToken);
		}
		return newToken;
	}

	public String getValue() {
		return unescape ? unescape(value) : value;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setValue(String value) {
		this.value = value;
	}

	private String unescape(String text) {
		return StringEscapeUtils.unescapeJava(text);
	}

	public void setUnescape(boolean unescape) {
		this.unescape = unescape;
	}

	public boolean isUnescape() {
		return unescape;
	}

	public static Replacement from(Replacement replacement) {
		return new Replacement(replacement.fileUtils, replacement.token, replacement.value,
				replacement.unescape, replacement.xpath, replacement.encoding, replacement.jsonpath, replacement.jsontype);
	}

	public Replacement withDelimiter(DelimiterBuilder delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getXpath() {
		return xpath;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setJsonpath(String jsonpath) {
		this.jsonpath = jsonpath;
	}

	public String getJsonpath() {
		return jsonpath;
	}

	public String getJsontype() {
		return this.jsontype;
	}

	public void setJsontype(String jsontype) {
		this.jsontype = jsontype;
	}
}

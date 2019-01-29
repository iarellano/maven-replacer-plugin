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

import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;

import java.util.*;

public class JsonPathReplacer implements Replacer {

    private final TokenReplacer tokenReplacer;
    private final Configuration jsonConfig;
    private final Gson gson;

    public JsonPathReplacer(TokenReplacer tokenReplacer) {
        try {
            if (tokenReplacer == null) {
                throw new IllegalArgumentException("Must supply a tokenReplacer to change the node's content.");
            }
            this.tokenReplacer = tokenReplacer;
            jsonConfig = Configuration.builder()
                    .options(Option.AS_PATH_LIST).build();
            gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialise JSON processing: " + e.getMessage(), e);
        }
    }

    public String replace(String content, Replacement replacement, boolean regex, int regexFlags) {
        try {
            ReadContext doc = parseJson(content);
            List<String> replacementTargets = findReplacementNodes(doc, replacement.getJsonpath());
            if (replacementTargets.size() > 0) {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(content);
                replaceContent(replacementTargets, replacement, regex, regexFlags, element);
                return gson.toJson(element);
            }
            return content;
        } catch (Exception e) {
            String cause = e.getMessage() != null ? e.getMessage() : e.getCause().getMessage();
            throw new RuntimeException("Error during JSON replacement: " + cause, e);
        }
    }

    List<Object> getPathSegemnts(String path) {
        List<Object> nodeSegments = new ArrayList<Object>();
        StringTokenizer st = new StringTokenizer(path, "]");
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            if (token.charAt(1) == '\'') {
                String name = token.substring(2, token.length() - 1);
                nodeSegments.add(name);
            } else {
                Integer index = Integer.valueOf(token.substring(1));
                nodeSegments.add(index);
            }
        }
        return nodeSegments;
    }

    private JsonElement getTargetJsonObject(JsonElement jsonElement, Iterator<Object> iterator) {
        if (iterator.hasNext()) {
            Object segment = iterator.next();
            JsonElement sibblingElement = null;
            if (segment instanceof Integer) {
                sibblingElement = jsonElement.getAsJsonArray().get((Integer) segment);
            } else {
                sibblingElement = jsonElement.getAsJsonObject().get((String) segment);
            }
            return getTargetJsonObject(sibblingElement, iterator);
        }
        return jsonElement;
    }

    private void replaceContent(List<String> replacementNodes, Replacement replacement, boolean regex, int regexFlags, JsonElement jsonElement) throws Exception {
        for (int i = 0; i < replacementNodes.size(); i++) {
            String replacementNode = replacementNodes.get(i);
            List<Object> nodeSegments = getPathSegemnts(replacementNode.substring(1));
            JsonElement target = getTargetJsonObject(jsonElement, nodeSegments.subList(0, nodeSegments.size() - 1).iterator());

            Object targetNode = nodeSegments.get(nodeSegments.size() - 1);
            if (targetNode instanceof Integer) {
                JsonElement item = target.getAsJsonArray().get((Integer) targetNode);
                String source = item.isJsonPrimitive() || item.isJsonNull()
                        ? item.getAsString()
                        : item.toString();

                String replacedValue = tokenReplacer.replace(source, replacement, regex, regexFlags);
                JsonElement value = normalizeValue(replacedValue, replacement);
                target.getAsJsonArray().set((Integer) targetNode, value);
            } else {
                JsonElement property = target.getAsJsonObject().get((String) targetNode);
                String source = property.isJsonPrimitive() || property.isJsonNull()
                        ? property.getAsString()
                        : property.toString();
                String replacedValue = tokenReplacer.replace(source, replacement, regex, regexFlags);

                JsonElement value = normalizeValue(replacedValue, replacement);
                target.getAsJsonObject().add((String) targetNode, value);

            }
        }
    }

    private JsonElement normalizeValue(String value, Replacement replacement) {
        if ("string".equals(replacement.getJsontype()) || replacement.getJsontype() == null) {
            return new JsonPrimitive(value);
        } else if ("number".equals(replacement.getJsontype())) {
            if (value.contains(".")) {
                return new JsonPrimitive(new Double(value));
            } else {
                return new JsonPrimitive(new Long(value));
            }
        } else if ("object".equals(replacement.getJsontype())) {
            return new JsonParser().parse(value);
        } else if ("true".equals(replacement.getJsontype())) {
            return new JsonPrimitive(true);
        } else if ("false".equals(replacement.getJsontype())) {
            return new JsonPrimitive(false);
        } else if ("null".equals(replacement.getJsontype())) {
            return JsonNull.INSTANCE;
        }
        throw new IllegalArgumentException("Json type '" + replacement.getJsontype() + "' not supported. Supported values are [string|number|object|array|true|false|null");
    }

    private ReadContext parseJson(String content) throws Exception {
        return JsonPath.using(jsonConfig).parse(content);
    }

    private List<String> findReplacementNodes(ReadContext doc, String jsonpathString) throws Exception {
        try {
            return doc.read(jsonpathString);
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }
}

package edu.xzit.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.xzit.core.core.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper _mapper = new ObjectMapper();

    static {
        _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule s = new SimpleModule();
        _mapper.registerModule(s);
    }

    public static String EncodePretty(Object obj) throws JsonProcessingException {
        return _mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public static byte[] Encode2Byte(Object value) throws JsonProcessingException {
        return _mapper.writeValueAsBytes(value);
    }

    public static String SafeEncodePretty(Object obj) {
        try{
            return _mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        }catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    public static String Encode(Object value) throws JsonProcessingException {
        return _mapper.writeValueAsString(value);
    }

    public static String EncodeWithGZIP(Object value) throws JsonProcessingException {
        final Base64.Encoder encoder = Base64.getEncoder();
        String orginal = Encode(value);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(orginal.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return orginal;
        }
        return encoder.encodeToString(out.toByteArray());
    }


    public static <T> T Decode(String value, Class<T> valueType) throws IOException {
        return _mapper.readValue(value, valueType);
    }

    public static <T> T Decode(byte[] bytes, Class<T> valueType) throws IOException {
        return _mapper.readValue(bytes, valueType);
    }

    public static <T> T Decode(InputStream in, Class<T> valueType) throws IOException {
        return _mapper.readValue(in, valueType);
    }

    public static <T> T Decode(JsonNode json, Class<T> valueType) throws IOException {
        return _mapper.convertValue(json, valueType);
    }

    public static <T> T Decode(String value, TypeReference<T> valueTypeRef) throws IOException {
        return _mapper.readValue(value, valueTypeRef);
    }

    public static <T> T Decode(byte[] bytes, TypeReference<T> valueTypeRef) throws IOException {
        return _mapper.readValue(bytes, valueTypeRef);
    }

    public static <T> T Decode(JsonNode json, TypeReference<T> valueTypeRef) throws IOException {
        return _mapper.convertValue(json, valueTypeRef);
    }

    public static <T> T Decode(InputStream in, TypeReference<T> valueTypeRef) throws IOException {
        return _mapper.readValue(in, valueTypeRef);
    }

    public static Map<String, Object> Decode2Map(String value) throws IOException {
        return Decode(value, new TypeReference<Map<String, Object>>() {
        });
    }

    public static Map<String, Object> Decode2MapWithGZIP(String value) throws IOException {
        final Base64.Decoder decoder = Base64.getDecoder();
        try {
            byte[] base64Decoded = decoder.decode(value);
            ByteArrayInputStream in = new ByteArrayInputStream(base64Decoded);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            String uncompressed = out.toString(StandardCharsets.UTF_8.name());
            return Decode2Map(uncompressed);
        } catch (IllegalArgumentException ex) { // if is not in valid Base64 scheme
            return Decode2Map(value);
        }
    }

    public static JsonNode Decode2Tree(String value) throws IOException {
        return _mapper.readTree(value);
    }

    public static JsonNode Decode2Tree(Map<String, Object> value) throws IOException {
        return _mapper.convertValue(value, JsonNode.class);
    }

    public static ObjectNode ObjectNode() {
        return _mapper.createObjectNode();
    }

    public static ArrayNode ArrayNode() {
        return _mapper.createArrayNode();
    }

    public static Object getObjectByPathWithDefault(Map<String, Object> root, String path, String spliter, Object _default) {
        if (root == null || path == null || path.isEmpty() || spliter == null || spliter.isEmpty()) {
            return _default;
        }
        // no spliter found, means no need recursion
        if (path.indexOf(spliter) == -1) {
            if (root.containsKey(path)) {
                return root.get(path);
            }
        } else {
            String seg1 = path.substring(0, path.indexOf(spliter));
            String segOther = path.substring(path.indexOf(spliter) + 1);
            if (root.containsKey(seg1)) {
                Object subTree = root.get(seg1);
                if (subTree instanceof Map) {
                    return getObjectByPath((Map) subTree, segOther, spliter);
                }
            }
        }

        return _default;
    }


    public static Object getObjectByPath(Map<String, Object> root, String path, String spliter) {
        return getObjectByPathWithDefault(root, path, spliter, null);
    }

    public static boolean setObjectByPathStrict(Map<String, Object> root, String path, String spliter, Object value) {
        return setObjectByPath(root, path, spliter, value, true);
    }

    public static boolean setObjectByPath(Map<String, Object> root, String path, String spliter, Object value, boolean strict) {
        if (root == null || path == null || path.isEmpty() || spliter == null || spliter.isEmpty()) {
            return false;
        }
        // no spliter found, means no need recursion
        if (path.indexOf(spliter) == -1) {
            if (root.containsKey(path)) {
                // strict means must be leaf
                if (strict) {
                    if (root.get(path) instanceof Map || root.get(path) instanceof ArrayList) {
                        return false;
                    }
                }
                root.put(path, value);
                return true;
            }
        } else {
            String seg1 = path.substring(0, path.indexOf(spliter));
            String segOther = path.substring(path.indexOf(spliter) + 1);
            if (root.containsKey(seg1)) {
                Object subTree = root.get(seg1);
                if (subTree instanceof Map) {
                    return setObjectByPath((Map) subTree, segOther, spliter, value, strict);
                }
            }
        }

        return false;
    }

    public static <T> List<T> decode2List(String text) {
        try {
            return _mapper.readValue(text, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            log.error("解析失败", e);
            throw new ServiceException("解析失败");
        }
    }
}

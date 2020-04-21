package com.fanap.midhco.appstore.applicationUtils;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by Heidari on 11/13/16.
 */
public class ZipUtil {
    public static final ZipUtil INSTANCE = new ZipUtil();

    private ZipUtil() {}

    public String compressBase64(String plain) throws Exception {
        byte[] compress = compress(plain);
        String msg = Base64.encodeBase64String(compress);
        return msg;
    }

    public byte[] compress(final byte[] input) throws IOException {
        Deflater compresser = new Deflater();
        compresser.setInput(input, 0, input.length);
        byte[] result = new byte[50000000];
        int resultLength = compresser.deflate(result, 0, input.length, Deflater.FULL_FLUSH);
        compresser.end();
        result = Arrays.copyOfRange(result, 0, resultLength);
        return result;
    }

    private byte[] compress(final String str) throws Exception {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        Deflater compresser = new Deflater();
        byte[] input = str.getBytes("UTF-8");
        compresser.setInput(input, 0, input.length);
        byte[] result = new byte[50000000];
        try {
            int resultLength = compresser.deflate(result, 0, input.length, Deflater.FULL_FLUSH);
            compresser.end();
            result = Arrays.copyOfRange(result, 0, resultLength);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    private String decompress(final byte[] compressed) throws Exception {
        String outStr = "";
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressed, 0, compressed.length);
        byte[] result = new byte[50000000];
        try {
            int resultLength = decompresser.inflate(result);
            decompresser.end();
            outStr = new String(result, 0, resultLength);
        } catch (Exception e) {
            throw e;
        }
        return outStr;
    }

    public byte[] decompressByteArray(final byte[] compressed) throws IOException, DataFormatException {
        String outStr = "";
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressed, 0, compressed.length);
        byte[] result = new byte[50000000];
        int resultLength = decompresser.inflate(result);
        decompresser.end();
        byte[] fixedResult = new byte[resultLength];
        System.arraycopy(result, 0, fixedResult, 0, resultLength);
        return fixedResult;
    }

    public boolean isBase64(String str) {
        return Base64.isBase64(str);
    }

    public String decompressBase64(String base64) throws Exception {
        byte[] decode = Base64.decodeBase64(base64);
        return decompress(decode);
    }
}
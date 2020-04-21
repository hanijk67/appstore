package com.fanap.midhco.appstore.service.security;

import com.fanap.midhco.appstore.encoders.Hex;
import com.fanap.midhco.ui.access.Access;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by admin123 on 6/19/2016.
 */
public class AccessService {
    private static final byte[] BITS = {1, 2, 4, 8, 16, 32, 64, (byte) 128};

    public static String encode(byte[] bytes) {
        return new String(Hex.encode(bytes));
    }

    public static boolean checkBit(byte[] bytes, int bitNo) {
        boolean result = false;
        int index = bitNo / 8;
        if (index < bytes.length)
            result = (bytes[index] & (byte) Math.pow(2, bitNo % 8)) != 0;
        return result;
    }

    public static void setBit(byte[] bytes, int bitNo) {
        bytes[bitNo / 8] = (byte) (bytes[bitNo / 8] | (byte) Math.pow(2, bitNo % 8));
    }

    public static int calcByteArrLen(int bitNo) {
        bitNo++; //adding the zero bitNo
        return bitNo / 8 + (bitNo % 8 > 0 ? 1 : 0);
    }

    public static String encode(SortedSet<Integer> bits) {
        if (bits.size() > 0) {
            byte[] bytes = new byte[AccessService.calcByteArrLen(bits.last())];
            for (Integer bit : bits)
                AccessService.setBit(bytes, bit);
            return AccessService.encode(bytes);
        }
        return null;
    }

    public static List<Integer> decode(byte[] bytes) {
        List<Integer> bits = new ArrayList<Integer>();
        if (bytes != null)
            for (int i = bytes.length - 1; i >= 0; i--) {
                for (int j = 7; j >= 0; j--)
                    if ((bytes[i] & BITS[j]) != 0)
                        bits.add(i * 8 + j);
            }
        return bits;
    }

    public static String addPermission(String oldPermission, Access access) {
        byte[] b = oldPermission != null ? Hex.decode(oldPermission) : null;
        return encode(addPermission(b, access));
    }

    public static byte[] addPermission(byte[] oldPermission, Access access) {
        final int bitNo = access.getBitNo();
        int len = calcByteArrLen(bitNo);
        byte[] result = oldPermission;
        if (oldPermission == null || oldPermission.length < len) {
            result = new byte[len];
            if (oldPermission != null)
                System.arraycopy(oldPermission, 0, result, 0, oldPermission.length);
        }
        setBit(result, bitNo);
        return result;
    }

    public static byte[] addPermissions(byte[] oldPermissions, String newPermissions) {
        return addPermissions(oldPermissions, getPermissionsBytes(newPermissions));
    }

    public static byte[] addPermissions(byte[] oldPermissions, byte[] newPermissions) {
        if (newPermissions == null)
            return oldPermissions;
        if (oldPermissions == null)
            return newPermissions;

        byte[] result;
        if (oldPermissions.length >= newPermissions.length)
            result = oldPermissions;
        else
            result = newPermissions;
        int len = Math.min(oldPermissions.length, newPermissions.length);
        for (int i = 0; i < len; i++)
            result[i] = (byte) (oldPermissions[i] | newPermissions[i]);
        return result;
    }

    public static boolean hasPermission(String permission, Access access) {
        return !isNullOrEmpty(permission) && hasPermission(Hex.decode(permission), access);
    }

    public static boolean hasPermission(byte[] permissions, Access access) {
        return !(permissions == null || access == null) && access.isEnabled() &&
                (checkBit(permissions, access.getBitNo()) || hasPermission(permissions, access.getParent()));
    }

    public static byte[] getPermissionsBytes(String permissions) {
        if (isNullOrEmpty(permissions))
            return null;
        return Hex.decode(permissions);
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
}

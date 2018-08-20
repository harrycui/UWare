package util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * This class is used to implement several common functions.
 */
public class BaseTool {


    public static String bytes2Hex(byte[] bts) {

        StringBuilder des = new StringBuilder();
        String tmp;

        for (byte bt : bts) {

            tmp = (Integer.toHexString(bt & 0xFF));

            if (tmp.length() == 1) {

                des.append('0');
            }
            des.append(tmp.toUpperCase());
        }
        return des.toString();
    }
    
    public static byte[] bigIntegerTo128Bits(BigInteger x) {
    	
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		buffer.clear();
		buffer.put(x.toByteArray(), 0, 16);
		return buffer.array();
    }

    //private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
    	
    		ByteBuffer buffer = ByteBuffer.allocate(8);
    		
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }
    
    
    
    public static byte[] longTo128Bits(long x) {
    	
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		buffer.clear();
		buffer.putLong(0, x);
		return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
    	
    		ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.clear();
        buffer.put(bytes, 0, 8);
        buffer.flip();//need flip
        return buffer.getLong();
    }
    

    public static long bytesToUnsignedInt(byte[] bytes) {

        ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.clear();
        buffer.put(bytes, 0, 4);
        buffer.flip();
        return buffer.getInt() & 0x0FFFFFFFFl;
    }

    /**
     * @param data the original data
     * @return long
     */
    public static long flod256Bytes(byte[] data) {

        long result = 0;

        if (data.length == 32) {

            byte[][] bb = new byte[4][8];

            for (int i = 0; i < data.length; ++i) {

                bb[i / 8][i % 8] = data[i];
            }

            result = bytesToLong(bb[0]) ^ bytesToLong(bb[1]) ^ bytesToLong(bb[2]) ^ bytesToLong(bb[3]);
        }

        return result;
    }
    
    /**
     * @param data the original data
     * @return long
     */
    public static long flod160Bits(byte[] data) {

        long result = 0;

        if (data.length == 20) {

            byte[][] bb = new byte[3][8];

            for (int i = 0; i < data.length; ++i) {

                bb[i / 8][i % 8] = data[i];
            }

            result = bytesToLong(bb[0]) ^ bytesToLong(bb[1]) ^ bytesToLong(bb[2]);
        }

        return result;
    }

    /**
     * This function is used to locate the real data of an expanded id.
     * 
     * @param id
     * @param loopSize
     * @return
     */
    public static int mapIndex(int id, int loopSize) {

        if (id % loopSize == 0) {
            return loopSize - 1;
        } else {
            return id % loopSize - 1;
        }

    }
}

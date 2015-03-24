package kr.re.dev.AndroidXMLReader;

/**
 * 주로 binary 데이터로부터 원하는 bit 만큼 데이터를 읽어서 반환하는 static 메소드를 제공한다. 
 * @author ice3x2
 */
public class Common {
	
	/**
	 * raw 에서 strt 로부터 end 의 위치까지 데이터를 읽어들인 다음 string 로 변환하여 반환한다.  
	 * @param raw
	 * @param start
	 * @param end
	 * @return
	 */
	public static String readString(byte[] raw, int start, int end) { 
		return new String(raw, start, end - start);
		
	}
	/**
	 * offset 으로부터 4바이트를 읽어서 integer 형태로 반환한다. 
	 * @param raw
	 * @param offset
	 * @return
	 */
	public static int readInt32(byte[] raw, int offset) {
		return (raw[offset + 3] << 24 & 0xff000000) |
			   (raw[offset + 2] << 16 & 0xff0000) |
			   (raw[offset + 1] << 8 & 0xff00) |
			   (raw[offset] & 0xff); 
	}
	/**
	 * offset 으로부터 2바이트를 읽어서 interger 형태로 반환한다.
	 * @param raw
	 * @param offset
	 * @return
	 */
	public static int readInt16(byte[] raw, int offset) {
		return (raw[offset + 1] << 8 & 0xff00) |
			   (raw[offset] & 0xff); 
	}
}
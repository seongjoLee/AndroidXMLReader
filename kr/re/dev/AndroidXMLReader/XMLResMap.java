package kr.re.dev.AndroidXMLReader;


public class XMLResMap extends BinaryXML {
	public static int TYPE = 0x0108;
	public static int HEADER = 0x00080180;
	public int mCount;
	public int[] mRes;
	protected static boolean verifyHeader(byte[] raw, int offset) {
		return Common.readInt32(raw, offset) == HEADER;
	}
	/**
	 * 0x00 : header(type/size)
	 * 0x04 : length
	 * 0x08 : Resource table
	 */
	protected static XMLResMap Parse(byte[] raw, int offset) {
		if(!verifyHeader(raw, offset)) return null;
		XMLResMap xmlResMap = new XMLResMap();
		int length = Common.readInt32(raw, 0x04 + offset);
		xmlResMap.mStart = offset; 
		xmlResMap.mEnd = offset + length;
		int xmlResSize = (xmlResMap.mEnd - xmlResMap.mStart) / 4;
		int res[] = new int[xmlResSize]; 
		for(int i = xmlResMap.mStart, cnt = 0; i < xmlResMap.mEnd; i += 4, ++cnt) {
			res[cnt] = Common.readInt32(raw, i);
		}
		xmlResMap.mRes = res;
		xmlResMap.mCount = xmlResSize;
		return xmlResMap;
	}
	
	public int getCount() {
		return mCount;
	}
	public int getResource(int index) {
		return mRes[index];
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("XMLResourceMap (count : ").append(getCount()).append(")");
		for(int i = 0, n = getCount(); i < n; ++i) {
			strBuilder.append('[').append(i).append("]:").append(getResource(i)).append(", ");
		}
		return strBuilder.toString();
	}
}
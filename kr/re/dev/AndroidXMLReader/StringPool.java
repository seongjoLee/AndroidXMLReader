package kr.re.dev.AndroidXMLReader;


public class StringPool extends BinaryXML {
	public static int TYPE = 0x0001;
	public static int HEADER = 0x001c0001;
	public int mCount;
	public String[] mStringPool;
	protected static boolean verifyHeader(byte[] raw, int offset) {
		return Common.readInt32(raw, offset) == HEADER;
	}
	/**
	 * 0x00 : header(type/size)
	 * 0x04 : length
	 * 0x08 : stringCount
	 * 0x0c : styleCount
	 * 0x10 : flags
	 * 0x14 : stringsStart
	 * 0x18 : stylesStart
	 * 0x1c : string index table
	 *  ~   : string table
	 */
	protected static StringPool Parse(byte[] raw, int offset) {
		if(!verifyHeader(raw, offset)) return null;
		StringPool stringPool = new StringPool();
		stringPool.mCount = Common.readInt32(raw, offset + 0x08);
		int length = Common.readInt32(raw, 0x04 + offset);
		int stringIndexTable = 0x1c + offset;
		int stringTable = stringIndexTable + (stringPool.mCount * 4);
		stringPool.mStart = offset;
		stringPool.mEnd = offset + length;
		stringPool.mStringPool = new String[stringPool.mCount];
		int indexAddr = stringIndexTable;
		int stringStart = Common.readInt32(raw, indexAddr) + stringTable;
		for(int i = 0, n = stringPool.mCount; i < n; ++i) {
			indexAddr+= 4; 
			int stringEnd = (i + 1 == stringPool.mCount)?stringPool.getEnd():
									Common.readInt32(raw, indexAddr) +  stringTable;
			// 왜인지 모르겠지만, String 값 앞에 0x0B 부터 Index 순번대로 시작하는 
			// 2바이트를 자치하는 쓸데없는(?) 수가 존재한다. 이 것을 제거하기 위하여 2바이트만큼 더한다.
			stringPool.mStringPool[i] = Common.readString(raw, stringStart + 0x02,stringEnd);

			stringStart = stringEnd;
		}
		
		return stringPool;
	}
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("String pool (count : ").append(getStringCount()).append(")");
		for(int i = 0, n = getStringCount(); i < n; ++i) {
			strBuilder.append('[').append(i).append("]:\"").append(getString(i)).append("\", ");
		}
		return strBuilder.toString();
	}
	
	public String getString(int i) {
		return (i < 0 || i >= getStringCount())?"":mStringPool[i];
	}
	
	public int getStringCount() {
		return mCount;
	}
	
}
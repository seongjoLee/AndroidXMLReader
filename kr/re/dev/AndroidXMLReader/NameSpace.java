package kr.re.dev.AndroidXMLReader;

/**
 * NameSapce. 
 * 
 * @author ice3x2
 *
 */
public class NameSpace extends BinaryXML {
	public static int TYPE_START = 0x0100;
	public static int HEADER_START = 0x00100100;
	public static int TYPE_END = 0x0101;
	public static int HEADER_END = 0x00100101;
	private int mType = TYPE_START;
	private String mComment; 
	private String mPrefix;
	private String mUri;
	private int mLineNumber;
	
	public static boolean verifyHeader(byte[] raw, int offset) {
		int header =  Common.readInt32(raw, offset);
		return header == HEADER_START || header == HEADER_END;
	}
	/**
	 * 0x00 : header(type/size)
	 * 0x04 : length
	 * 0x08 : line Number
	 * 0x0c : comment (string pool idx)
	 * 0x10 : prefix (string pool idx)
	 * 0x14 : uri (string pool idx)
	 */
	protected static NameSpace Parse(byte[] raw, int offset, StringPool stringPool) {
		if(!verifyHeader(raw, offset)) return null;
		NameSpace nameSpace = new NameSpace();
		int length = Common.readInt32(raw, 0x04 + offset);
		nameSpace.mType = Common.readInt16(raw, offset);
		nameSpace.mStart = offset; 
		nameSpace.mEnd = offset + length;
		nameSpace.mComment = stringPool.getString(Common.readInt32(raw, offset + 0x0c));
		nameSpace.mLineNumber = Common.readInt32(raw, offset + 0x08);
		nameSpace.mPrefix = stringPool.getString(Common.readInt32(raw, offset + 0x10));
		nameSpace.mUri = stringPool.getString(Common.readInt32(raw, offset + 0x14));
		return nameSpace;
	}
	protected boolean isStart() {
		return mType == TYPE_START;
	}
	public int getLineNumber() {
		return mLineNumber;
	}
	public String getPrefix() {
		return mPrefix;
	}
	public String getComment() {
		return mComment;
	}
	public String getUri() {
		return mUri;
	}
	@Override
	public String toString() {
		return super.toString() + "\n[line : " + getLineNumber() + " ,namespace : " + getPrefix() + "=\"" + getUri() + "\"]";
	}
}
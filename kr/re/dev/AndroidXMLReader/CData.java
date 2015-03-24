package kr.re.dev.AndroidXMLReader;

/**
 * CData. 
 * 
 * 15.03.25 : 개발할 필요성을 못 느껴서 파싱하는 것만 구현함. getter/setter 는 따로 구현하지 않음. 
 * @author ice3x2
 *
 */
public class CData extends BinaryXML {
	public static int TYPE = 0x0104;
	public static int HEADER = 0x00100104;
	private int mType = TYPE;
	private int mLineNumber;
	private String mComment;
	private String mNs;
	private int mData;
	private int mSize;
	private int mDataType;
	private int mDataOfType;
	
	/**
	 * 기본 데이터 사이즈 32bit
	 * 
	 * 0x00 : header(type/size)
	 * 0x04 : length
	 * 0x08 : line Number
	 * 0x0C : comment (string pool idx)
	 * 0x10 : data 
	 * 0x14 : size[16bit]
	 * 0x16 : null[8bit]
	 * 0x17 : dataType[8bit] 
	 * 0x18 : data 
	 */
	protected static boolean verifyHeader(byte[] raw, int offset) {
		return  Common.readInt32(raw, offset) == HEADER;
	}
	protected static CData Parse(byte[] raw, int offset, StringPool stringPool) {
		if(!verifyHeader(raw, offset)) return null;
		CData cdata = new CData();
		int length = Common.readInt32(raw, 0x04 + offset);
		cdata.mType = Common.readInt16(raw, offset);
		cdata.mStart = offset; 
		cdata.mEnd = offset + length;
		cdata.mLineNumber = Common.readInt32(raw, offset + 0x08);
		cdata.mComment = stringPool.getString(Common.readInt32(raw, offset + 0x0c));
		cdata.mData = Common.readInt32(raw, offset + 0x10);
		cdata.mSize = Common.readInt16(raw, offset + 0x14);
		cdata.mDataType = raw[offset + 0x17];
		cdata.mDataOfType = Common.readInt32(raw,offset + 0x18);
		return cdata;
	}
}
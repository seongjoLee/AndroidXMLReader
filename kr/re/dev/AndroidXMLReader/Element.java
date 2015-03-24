package kr.re.dev.AndroidXMLReader;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Element 트리 구조를 이루고 있다.
 * @author ice3x2
 */
public class Element extends BinaryXML {
	public static int TYPE_START = 0x0102;
	public static int HEADER_START = 0x00100102;
	public static int TYPE_END = 0x0103;
	public static int HEADER_END = 0x00100103;
	private static String[] DIMENSION = new String[] { "px", "dp", "sp", "pt", "in", "mm" };
	
	private int mType = TYPE_START;
	private int mLineNumber;
	private String mComment;
	private String mNs;
	private String mName;
	private int mAttributeCount;
	private int mIdIdx;
	private int mClassIdx;
	private int mStyleIdx;
	private Attribute[] mAttributes;
	private Element mParent;
	private LinkedList<Element> mChildren = new LinkedList<>();
	private LinkedList<CData> mCDatas = new LinkedList<>();
	
	protected static boolean verifyHeader(byte[] raw, int offset) {
		int header =  Common.readInt32(raw, offset);
		return header == HEADER_START || header == HEADER_END;
	}
	/**
	 * 기본 데이터 사이즈 32bit
	 * 
	 * 0x00 : header(type/size)
	 * 0x04 : length
	 * 0x08 : line Number
	 * 0x0C : comment (string pool idx)
	 * 
	 * 0x10 : namespace uri (string pool idx) xmlns: 로 시작. 
	 * 0x14 : name (string pool idx)
	 * 0x18 : attributeStart[16bit] 0x14 로 고정.
	 * 0x1A : attributeSize[16bit]  0x14 로 고정.
	 * 0x1C : attributeCount[16bit]
	 * 0x1E : idIndex[16bit]
	 * 0x20 : classIndex[16bit]
	 * 0x22 : styleIndex[16bit]
	 * 0x24 : Attribute[]  (array 타입.)  
	 * 			0x00 : attribute name space uri (string pool idx)
	 * 			0x04 : attribute name (string pool idx)
	 * 			0x08 : attribute rawvalue (string pool idx)
	 * 			0x0c : size[16bit] What size? ...  아마도 0x08 로 고정되어 있는듯.
	 * 			0x0e : null[8bit]
	 * 			0x0f : data type[8bit]
	 * 			0x10 : data - data type 에 따라서 달라진다. 
	 */
	protected static Element Parse(byte[] raw, int offset, StringPool stringPool, XMLResMap xmlMap) {
		if(!verifyHeader(raw, offset)) return null;
		Element element = new Element();
		int length = Common.readInt32(raw, 0x04 + offset);
		element.mType = Common.readInt16(raw, offset);
		element.mStart = offset; 
		element.mEnd = offset + length;
		element.mLineNumber = Common.readInt32(raw, offset + 0x08);
		element.mComment = stringPool.getString(Common.readInt32(raw, offset + 0x0c));
		element.mNs = stringPool.getString(Common.readInt32(raw, offset + 0x10));
		element.mName = stringPool.getString(Common.readInt32(raw, offset + 0x14));
		if(!element.isStart()) return element;
		element.mAttributeCount = Common.readInt16(raw, offset + 0x1C);
		element.mIdIdx = Common.readInt16(raw, offset + 0x1E);
		element.mClassIdx = Common.readInt16(raw, offset + 0x20);
		element.mStyleIdx = Common.readInt16(raw, offset + 0x22);
		element.mAttributes = parseAttribute(raw, offset + 0x24, element, stringPool, xmlMap);
		return element;
	}		
	
	/**
	 * Binary xml 의 Element 내의 Attribute 를 파싱한다.
	 * @param raw
	 * @param offset
	 * @param element
	 * @param stringPool
	 * @param xmlMap
	 * @return
	 */
	private static Attribute[] parseAttribute(byte[] raw, int offset, Element element, StringPool stringPool, XMLResMap xmlMap) {
		Attribute[] attributes = new Attribute[element.mAttributeCount];
		for(int i = 0; i < element.mAttributeCount; ++i) {
			attributes[i] = new Attribute(element);
			attributes[i].mNs = stringPool.getString(Common.readInt32(raw, offset));
			attributes[i].mName = stringPool.getString(Common.readInt32(raw, offset + 0x04));
			attributes[i].mRawValue = stringPool.getString(Common.readInt32(raw, offset + 0x08));
			int resType = raw[offset + 0x0f];
			// Resource Type 을 ValueType 으로 컨버팅하여 입력한다.
			attributes[i].setResourceValueType(resType);
			int data = Common.readInt32(raw, offset + 0x10);
			injectAttributeValueFromData(stringPool, attributes[i], data);
			
			
			// Attribute 의 크기는 0x14 (20) 바이트로 고정되어있다. 
			offset += 0x14;
		}
		return attributes;
	}		
	
	/**
	 * Attribute 내의 Value 값들을 binary xml 으로부터 읽어온 data 값을 이용하여 넣어준다. 
	 * @param stringPool
	 * @param attribute
	 * @param data
	 */
	private static void injectAttributeValueFromData(StringPool stringPool, Attribute attribute, int data) {
		ValueType type = attribute.mValueType;
		if(type == ValueType.COLOR) {
			attribute.mStringValue = String.format("#%08X", data);
			attribute.mNumValue = data;
		} else if(type == ValueType.BOOLEAN) {
			attribute.mNumValue = data;
			attribute.mStringValue = Boolean.toString(data != 0);
		} else if(type == ValueType.INT) {
			attribute.mNumValue = data;
			attribute.mStringValue = Integer.toString(data);
		} else if(type == ValueType.FLOAT) {
			attribute.mNumValue = data;
			attribute.mStringValue =  Float.toString(Float.intBitsToFloat(data));
		} else if(type == ValueType.DIMENSION) {
			attribute.mStringValue = Integer.toString(data >> 8) + DIMENSION[data & 0xFF];
		} else if(type == ValueType.FRACTION) {
			double fraction  = (((double) data) / ((double) 0x7FFFFFFF));
			attribute.mStringValue = new DecimalFormat("#.##%").format(fraction);;
		} else if(type == ValueType.RESOURCE) {
			attribute.mStringValue = String.format("0x%08X", data);
			attribute.mNumValue = data;
		} else if(type == ValueType.STRING) {
			attribute.mStringValue = stringPool.getString(data);
		} else  {
			attribute.mStringValue = attribute.mRawValue;
		}
	}
	
	
	protected void setParent(Element element) {
		mParent = element;
	}
	protected void addChild(Element element) {
		mChildren.add(element);
	}
	protected void addCData(CData cData) {
		mCDatas.add(cData);
	}
	
	protected boolean isStart() {
		return mType == TYPE_START;
	}
	public Element getElement() {
		return mParent;
	}
	public int getLineNumber() {
		return mLineNumber;
	}
	public String getComment() {
		return mComment;
	}
	public String getNamespace() {
		return mNs;
	}
	public String getName() {
		return mName;
	}
	public int getAttributeCount() {
		return mAttributeCount;
	}
	/**
	 * get ID index
	 * @return
	 */
	public int getIdIdx() {
		return mIdIdx;
	}
	/**
	 * get class index
	 * @return
	 */
	public int getClassIdx() {
		return mClassIdx;
	}
	/**
	 * get style index
	 * @return style index
	 */
	public int getStyleIdx() {
		return mStyleIdx;
	}
	/**
	 * index 기반으로 Attribute 객체를 반환한다. 
	 * @param index 0 >= index && index < {@link getAttributeCount}
	 * @return Attribute instance
	 */
	public Attribute getAttribute(int index) {
		return mAttributes[index];
	}
	
	/**
	 * Attribute 의 name 을 이용하여 Attribute 의 객체를 찾아서 반환한다. 
	 * 인자로 들어온 이름이 존재하는 Attribute가 없다면 null 반환.  
	 * @param name Attribute 의 이름. 
	 * @return Attribute 의 값 or null
	 */
	public Attribute getAttribute(String name) {
		for(Attribute attr : mAttributes) {
			if(attr.getName().equals(name)) return attr;
		}
		return null;
	}
	
	
	public Element[] getChildren() {
		return mChildren.toArray(new Element[0]);
	}
	
	/**
	 * 현재 경로를 포함한 모든 하위 경로에 있는 Element 를 name 기준으로 찾아서 반환한다.
	 * @param name
	 * @return 
	 */
	public Element[] findElements(String name) {
		ArrayList<Element> resulList = new ArrayList<>();
		searchChid(this, resulList, name);
		return resulList.toArray(new Element[0]);
	}
	
	private void searchChid(Element element, List<Element> elementList, String name) {
		for(Element child : element.getChildren()) {
			if(child.getName().trim().equals(name.trim())) {
				elementList.add(child);
			}
			element = child;
			searchChid(element, elementList, name);
		}
	}
	
	
	/**
	 * 현재 경로를 포함한 하위 경로에 있는 모든 Element 중에 인자값으로 들어온 이름과 동일한 이름을 갖고 있는
	 * Attribute 를 찾아서 반환한다. 
	 * @param name
	 * @return
	 */
	public Attribute[] findAttribute(String name) {
		ArrayList<Attribute> resulList = new ArrayList<>();
		searchAttribute(mAttributes,resulList, name);
		searchAttributeInChildren(this, resulList, name);
		return resulList.toArray(new Attribute[0]);
	}
	
	private void searchAttributeInChildren(Element element, List<Attribute> attributes, String name) {
		for(Element child : element.getChildren()) {
			searchAttribute(child.mAttributes, attributes, name);
			element = child;
			searchAttributeInChildren(element, attributes, name);
		}
	}
	
	private void searchAttribute(Attribute[] attributes, List<Attribute> result, String name) {
		for(Attribute attr : attributes) {
			if(attr.getName().trim().equals(name.trim())) {
				result.add(attr);
			}
		}
	}

	public static class Attribute extends BinaryXML{
		private String mNs = "";
		private String mName = "";
		private String mRawValue = "";
		private String mStringValue = "";
		private int mResourceValueType = ResourceValueType.TYPE_NULL;
		private ValueType mValueType= ValueType.NULL;
		private int mNumValue  = -1;
		private WeakReference<Element> mParent;
		
		public Attribute(Element element) {
			mParent = new WeakReference<Element>(element);
		}
		public String getNamespace() {
			return mNs;
		}
		public String getName() {
			return mName;
		}
		public Element getParent() {
			return mParent.get();
		}
		public ValueType getValueType() {
			return mValueType;
		}			
		public boolean getBooleanValue() {
			return mNumValue != 0;
		}
		public int getIntValue() {
			return mNumValue;
		}
		public float getFloatValue() {
			return Float.intBitsToFloat(mNumValue);
		}
		public String getValue() {
			return mStringValue;
		} 
		
		private void setResourceValueType(int resValueType) {
			mResourceValueType = resValueType;
			if(resValueType == ResourceValueType.TYPE_INT_BOOLEAN)
				mValueType = ValueType.BOOLEAN;
			else if(resValueType == ResourceValueType.TYPE_FLOAT) 
				mValueType = ValueType.FLOAT;
			else if(resValueType == ResourceValueType.TYPE_NULL) 
				mValueType = ValueType.NULL;
			else if(resValueType == ResourceValueType.TYPE_FRACTION) 
				mValueType = ValueType.FRACTION;
			else if(resValueType == ResourceValueType.TYPE_DIMENSION) 
				mValueType = ValueType.DIMENSION;
			else if(resValueType == ResourceValueType.TYPE_ATTRIBUTE) 
				mValueType = ValueType.RESOURCE;
			else if(resValueType >= ResourceValueType.TYPE_FIRST_COLOR_INT &&
					resValueType <= ResourceValueType.TYPE_LAST_COLOR_INT) {
				mValueType = ValueType.COLOR;
			} else if(resValueType >= ResourceValueType.TYPE_FIRST_INT &&
					resValueType <= ResourceValueType.TYPE_LAST_INT) {
				mValueType = ValueType.INT;
			}
			else if(resValueType != ResourceValueType.TYPE_STRING) {
				mValueType = ValueType.STRING;
			}
			else
				mValueType = ValueType.NULL;
		}	
		
		@Override
		public String toString() {
			//return super.toString();
			return mName + ":" + "\"" + mStringValue + "\"";
		}
	}
	

	
	/**
	 * Android 내부의 XML 파서코드에 정의된 내용. 
	 * @author ice3x2
	 */
	private static interface ResourceValueType {
		// Contains no data.
        public static int TYPE_NULL = 0x00;
        // The 'data' holds a ResTable_ref, a reference to another resource
        // table entry.
        public static int TYPE_REFERENCE = 0x01;
        // The 'data' holds an attribute resource identifier.
        public static int TYPE_ATTRIBUTE = 0x02;
        // The 'data' holds an index into the containing resource table's
        // global value string pool.
        public static int TYPE_STRING = 0x03;
        // The 'data' holds a single-precision floating point number.
        public static int TYPE_FLOAT = 0x04;
        // The 'data' holds a complex number encoding a dimension value,
        // such as "100in".
        public static int TYPE_DIMENSION = 0x05;
        // The 'data' holds a complex number encoding a fraction of a
        // container.
        public static int TYPE_FRACTION = 0x06;

        // Beginning of integer flavors...
        public static int TYPE_FIRST_INT = 0x10;

        // The 'data' is a raw integer value of the form n..n.
        public static int TYPE_INT_DEC = 0x10;
        // The 'data' is a raw integer value of the form 0xn..n.
        public static int TYPE_INT_HEX = 0x11;
        // The 'data' is either 0 or 1, for input "false" or "true" respectively.
        public static int TYPE_INT_BOOLEAN = 0x12;

        // Beginning of color integer flavors...
        public static int TYPE_FIRST_COLOR_INT = 0x1c;
        // The 'data' is a raw integer value of the form #aarrggbb.
        public static int TYPE_INT_COLOR_ARGB8 = 0x1c;
        // The 'data' is a raw integer value of the form #rrggbb.
        public static int TYPE_INT_COLOR_RGB8 = 0x1d;
        // The 'data' is a raw integer value of the form #argb.
        public static int TYPE_INT_COLOR_ARGB4 = 0x1e;
        // The 'data' is a raw integer value of the form #rgb.
        public static int TYPE_INT_COLOR_RGB4 = 0x1f;
        // ...end of integer flavors.
        public static int TYPE_LAST_COLOR_INT = 0x1f;
        // ...end of integer flavors.
        public static int TYPE_LAST_INT = 0x1f;
	}
	
}

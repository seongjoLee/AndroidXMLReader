package kr.re.dev.AndroidXMLReader;

import java.util.Stack;

/**
 * AndroidXML
 * 
 * Android 데이터 내부의 파싱 정보를 담고 있다.
 * 
 * @author ice3x2
 *
 */
public class AndroidXML extends BinaryXML {
	private StringPool mStringPool;
	private XMLResMap mXMLResMap;
	private Element mRootElement;
	private NameSpace mNameSpace;
	public static AndroidXML parse(byte[] raw) {
		AndroidXML androidXML = new AndroidXML();
		androidXML.mStringPool = StringPool.Parse(raw, 0x08);
		androidXML.mXMLResMap = XMLResMap.Parse(raw, androidXML.mStringPool.getEnd());
		androidXML.mNameSpace = NameSpace.Parse(raw, androidXML.mXMLResMap.getEnd(), androidXML.mStringPool);
		androidXML.mRootElement = Element.Parse(raw, androidXML.mNameSpace.getEnd(), 
											    androidXML.mStringPool, androidXML.mXMLResMap);
		Element element = androidXML.mRootElement;
		BinaryXML lastEle = element;
		Stack<Element> elementStack = new Stack<>();
		elementStack.push(element);
		// 엘리먼트 트리를 만든다. 
		while(!elementStack.isEmpty()) {
			if(Element.verifyHeader(raw, lastEle.getEnd())) {
				element = Element.Parse(raw, lastEle.getEnd(), 
									    androidXML.mStringPool, androidXML.mXMLResMap);
				lastEle = element;
				if(!element.isStart() && elementStack.lastElement().getName().equals(element.getName())) {
					elementStack.pop();
				} else {
					element.setParent(elementStack.lastElement());
					elementStack.lastElement().addChild(element);
					elementStack.push(element);
				}
			} else if(CData.verifyHeader(raw, lastEle.getEnd())) {
				CData cdata = CData.Parse(raw, lastEle.getEnd(), androidXML.mStringPool);
				lastEle = cdata;
				elementStack.lastElement().addCData(cdata);
			}
		}			
		return androidXML;
	}
	
	public Element getRootElement() {
		return mRootElement;
	}
	
	
}
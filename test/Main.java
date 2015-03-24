package test;

import java.io.IOException;

import kr.re.dev.AndroidXMLReader.AndroidXML;
import kr.re.dev.AndroidXMLReader.AndroidXMLReader;

public class Main {

	public static void main(String[] args) {
		try {
			AndroidXML androidXML = AndroidXMLReader.readAndroidManifest("test.apk");
			
			System.out.print(androidXML.getRootElement().findAttribute("p a c k a g e")[0].getValue());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package kr.re.dev.AndroidXMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;


/**
 * V0.9 : 대충 공부용으로 만들려다 일이 너무 커져버렸다... ;;; 
 * 		  애초에 사용하려고 만든 것이 아니기 때문에 테스트 코드는 없지만,
 * 		  이렇게 된 이상 0.9.1 버전이후로 테스트 삽입 예정. 
 * 	
 * @author ice3x2
 *
 */
public class AndroidXMLReader {
	
	
	public static AndroidXML read(String apkFilePath, String xmlFileName) throws IOException {
			if(!new File(apkFilePath).isFile()) return null;
		    JarFile jf = new JarFile(apkFilePath);
		    InputStream is = jf.getInputStream(jf.getEntry(xmlFileName));
		    byte[] xmlRaw = new byte[is.available()];
		    is.read(xmlRaw);
		    return AndroidXML.parse(xmlRaw);
	}
	public static AndroidXML readAndroidManifest(String apkFilePath) throws IOException {
		    return read(apkFilePath, "AndroidManifest.xml");
	}
}

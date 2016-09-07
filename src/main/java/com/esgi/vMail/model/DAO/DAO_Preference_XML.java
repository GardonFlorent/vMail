package com.esgi.vMail.model.DAO;

import com.esgi.vMail.model.DAL.DAL_XML;
import org.jdom2.Element;

import java.util.List;

public class DAO_Preference_XML extends DAO_XML {
	private final static String fileName = "preferences";
	static {
		switch (DAL_XML.getOrCreateFile(fileName, fileName +".xml")) {
			case DAO_XML.XMLState.CREATED:
				DAL_XML.setDocument4File(fileName, fileName);
			case DAO_XML.XMLState.EXIST:
			/*DAL_XML.setXSD4FileByXSDName(fileName, fileName);*/
				DAO_Preference_XML.rootNode = DAL_XML.getRootNode(fileName);
				break;
			default:
				break;
		}
	}

	public static boolean saveProperty(String name, Object value) {
		Element toSave = new Element(name);
		toSave.setText(String.valueOf(value));
		DAL_XML.insertChild("", rootNode, toSave);
		return true;
	}

	public static Element getProperty(String name) {
		return DAL_XML.getElementByName(name, rootNode);
	}

}

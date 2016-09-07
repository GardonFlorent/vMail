package com.esgi.vMail.model.DAO;

import org.jdom2.Element;

import java.util.List;

public abstract class DAO_XML {
	protected static Element rootNode;

	public static class XMLState {
		public final static int CREATED = 1;
		public final static int EXIST = 0;
		public final static int ERROR = -1;
	}

	public static List<Element> getAll() {
		return rootNode.getChildren();
	}
}

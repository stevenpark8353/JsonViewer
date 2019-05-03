package hk.hkk.jsonViewer;

public class TreeObject {
	// static final int TYPE_JSON_ARRAY = 1;
	// static final int TYPE_JSON_OBJECT = 2;
	// int type;

	String name;
	String key;
	Object jsonObj;

	public TreeObject(String name, String key, Object obj) {
		this.name = name;
		this.key = key;
		this.jsonObj = obj;
	}

	public String toString() {
		return name;
	}
}
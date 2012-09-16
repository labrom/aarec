package labrom.data;

import java.util.LinkedHashMap;

public class SelectionArgs extends LinkedHashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String buildSelectionString() {
		if(isEmpty())
			return null;
		StringBuilder sb = new StringBuilder();
		for(String col : keySet()) {
			if(sb.length() > 0)
				sb.append(" AND ");
			sb.append(col).append("=?");
		}
		return sb.toString();
	}
	
	String[] buildSelectionValues() {
		if(isEmpty())
			return null;
		String[] values = new String[size()];
		int i = 0;
		for(Object v : values()) {
			if(v != null)
				values[i ++] = v.toString(); 
		}
		return values;
	}


}

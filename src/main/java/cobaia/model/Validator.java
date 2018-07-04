package cobaia.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Validator {

	private AbstractModel model;
	private List<String> msgs = new ArrayList<String>();

	public Validator(AbstractModel model) {
		this.model = model;
	}
	
	public List<String> getMsgs() {
		return msgs;
	}

	public void setMsgs(List<String> msgs) {
		this.msgs = msgs;
	}

	public boolean validate() {
		return validate(this.model);
	}
	
	public static boolean validate(AbstractModel o) {
		boolean valid = false;
		
		try {
			for (Field f : o.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				if (f.isAnnotationPresent(Required.class))
					valid = testRequired(f, f.get(o));
				
				if(f.isAnnotationPresent(StrLength.class))
					valid = testStrLength(f, String.valueOf(f.get(o)));
				
				if(f.isAnnotationPresent(Email.class))
					valid = testEmail(f, String.valueOf(f.get(o)));
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return valid;
	}
	
	private static boolean testRequired(Field f, Object v) {
		if(f.getType().equals(String.class))
			return (v != null && !StringUtils.isEmpty(String.valueOf(v)));
		else
			return v!= null;
		
	}
	
	private static boolean testEmail(Field f, String v) {
		return v.matches("[\\w._]+@\\w+(\\.\\w+)+");
	}
	
	private static boolean testStrLength(Field f, String v) {
		if(v == null) return false;
		
		int min = f.getAnnotation(StrLength.class).min();
		int max = f.getAnnotation(StrLength.class).max();
		
		return min <= v.length() && v.length() <= max;
	}
	
}
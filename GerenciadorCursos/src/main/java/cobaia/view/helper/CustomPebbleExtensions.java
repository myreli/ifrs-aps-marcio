package cobaia.view.helper;

import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Test;

public class CustomPebbleExtensions extends AbstractExtension {
		
	private Map<String, Filter> filters = new HashMap<>();
	private Map<String, Test> tests 	= new HashMap<>();
	
	public CustomPebbleExtensions() {
		getTests().put("prime", new PrimeTest());
	}

	public Map<String, Filter> getFilters() {
		return filters;
	}

	public Map<String, Test> getTests() {
		return tests;
	}
	
}

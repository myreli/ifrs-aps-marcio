package cobaia.view.helper;

import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.Test;

public class PrimeTest implements Test {

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public boolean apply(Object input, Map<String, Object> args) {
		Integer n = Integer.parseInt(String.valueOf(input));

		if (n <= 2) return n == 2;

		if (n % 2 == 0) return false;

        for (int i = 3, e = (int)Math.sqrt(n); i <= e; i += 2) {
            if (n % i == 0) return false;
        }

        return true;
		
	}

}

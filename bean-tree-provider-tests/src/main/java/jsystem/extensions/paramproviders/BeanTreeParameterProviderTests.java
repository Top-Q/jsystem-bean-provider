package jsystem.extensions.paramproviders;

import org.junit.Test;

import jsystem.extensions.paramproviders.bean.Library;
import jsystem.framework.TestProperties;
import jsystem.framework.scenario.UseProvider;
import junit.framework.SystemTestCase4;

public class BeanTreeParameterProviderTests extends SystemTestCase4 {
	
	private Library library;
	
	@Test
	@TestProperties(name = "Report Library",paramsInclude = {"library"})
	public void reportLibrary(){
		
	}

	public Library getLibrary() {
		return library;
	}

	@UseProvider(provider = jsystem.extensions.paramproviders.BeanTreeParameterProvider.class)
	public void setLibrary(Library library) {
		this.library = library;
	}
	
	
}

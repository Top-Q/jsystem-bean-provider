package jsystem.extensions.paramproviders;

import java.awt.Component;

import jsystem.framework.scenario.Parameter;
import jsystem.framework.scenario.ParameterProvider;
import jsystem.framework.scenario.RunnerTest;
import jsystem.framework.scenario.Scenario;

import org.jsystemtest.BeanTreeDialog;

import com.thoughtworks.xstream.XStream;


public class BeanTreeParameterProvider implements ParameterProvider{

    static BeanTreeDialog d;

	@Override
	public void setProviderConfig(String... args) {
		//Unused
	}

	@Override
	public String getAsString(Object o) {
        if (o == null){
			return "";
		}
        XStream xStream = new XStream(/*new DomDriver()*/);
        String strRepresentation =  xStream.toXML(o);

        return strRepresentation;
	}

	@Override
	public Object getFromString(String stringRepresentation) throws Exception {
        XStream xStream = new XStream(/*new DomDriver()*/);
        Object obj = xStream.fromXML(stringRepresentation);

		return obj;
	}

	@Override
	public Object showUI(Component parent, Scenario currentScenario, RunnerTest rtest, Class<?> classType,
			Object object, Parameter parameter) throws Exception {

        if(d == null) {
		    d = new BeanTreeDialog(/*(JFrame)parent, */"Bean Tree Parameter Provider");
            d.buildDialog();
        }
		//d.buildAndShowDialog(object);

        String strObj = getAsString(object);

        d.initTreeTableModel(object);
        d.showDialog();

        if(d.isSaveClicked()) { // User clicked Save
            // NOTE: returning the original "object" doesn't work! (bug?)
            // it's a good question why is this happening...
            strObj = getAsString(d.getRootObject()); //return object; //return d.getRootObject();
        }

        return getFromString(strObj); // User clicked Cancel
	}

	@Override
	public boolean isFieldEditable() {
		return true;
	}

}

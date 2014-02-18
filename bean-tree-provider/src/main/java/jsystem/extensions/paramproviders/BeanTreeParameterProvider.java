package jsystem.extensions.paramproviders;

import java.awt.Component;

import javax.swing.JFrame;

import jsystem.framework.scenario.Parameter;
import jsystem.framework.scenario.ParameterProvider;
import jsystem.framework.scenario.RunnerTest;
import jsystem.framework.scenario.Scenario;

import org.apache.commons.lang3.StringUtils;
import org.jsystemtest.BeanTreeDialog;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class BeanTreeParameterProvider implements ParameterProvider{

	@Override
	public void setProviderConfig(String... args) {
		//Unused
	}

	@Override
	public String getAsString(Object o) {
		if (o == null){
			return "";
		}
        XStream xStream = new XStream(new DomDriver());
        return xStream.toXML(o);
	}

	@Override
	public Object getFromString(String stringRepresentation) throws Exception {
		if (StringUtils.isEmpty(stringRepresentation)){
			return new Object();
		}
		XStream xStream = new XStream(new DomDriver());
		return xStream.fromXML(stringRepresentation);
	}

	@Override
	public Object showUI(Component parent, Scenario currentScenario, RunnerTest rtest, Class<?> classType,
			Object object, Parameter parameter) throws Exception {
		BeanTreeDialog d = new BeanTreeDialog((JFrame)parent,"Bean Tree Parameter Provider");
		d.buildAndShowDialog(object);
		return object;
	}

	@Override
	public boolean isFieldEditable() {
		return true;
	}

}

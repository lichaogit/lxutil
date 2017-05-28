package org.lx.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jNodeConfiguration;
import org.lx.arch.Dom4jTreeModel;
import org.lx.util.LogicUtil.MatchCondition;

public class XmlRegexMatchCondition implements MatchCondition
{
	String m_url;

	String m_regexAttrName;

	String m_sensitiveAttrName;

	public XmlRegexMatchCondition(String url, String regexAttrName,
			String sensitiveAttrName)
	{
		m_url = url;
		m_regexAttrName = regexAttrName;
		m_sensitiveAttrName = sensitiveAttrName;
	}

	public boolean isMatch(SimpleTreeModel node)
	{
		boolean retval = false;
		String url = m_url;

		Dom4jTreeModel dt = (Dom4jTreeModel) node;
		Dom4jNodeConfiguration nodeConf = new Dom4jNodeConfiguration(dt);
		ConfigurationView confView = new ConfigurationView(nodeConf, null);
		String regex = confView.getAttribute(m_regexAttrName);
		String sensitive = confView.getAttribute(m_sensitiveAttrName);
		if (sensitive != null && !Boolean.valueOf(sensitive).booleanValue())
		{
			// to lower if do not case sensitive¡£
			regex = regex.toLowerCase();
			url = url.toLowerCase();
		}

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		retval = m.matches();

		return retval;
	}

	public boolean searchContinue()
	{
		return false;
	}

}

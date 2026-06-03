package org.xper.allen.specs;

import com.thoughtworks.xstream.XStream;
import org.xper.allen.nafc.experiment.RewardPolicy;
import org.xper.drawing.Coordinates2D;

/**
 * Fields correspond with xml entries in the "spec" column in "stimspec" database table.
 * Contains toXML and fromXML methods.
 * @author Allen Chen
 *
 */
public class PassiveStimSpecSpec {
	protected String stimType = "None";
	//@XStreamAlias("sampleObjData")
	protected long sampleObjData;
	//@XStreamAlias("choiceObjData")
	protected long matchObjData;

	protected transient static XStream s;

	static {
		s = new XStream();
		s.alias("StimSpec", PassiveStimSpecSpec.class);
		s.useAttributeFor("animation", boolean.class);
	}

	public PassiveStimSpecSpec(long sampleObjData, long matchObjData) {
		this.sampleObjData = sampleObjData;
		this.matchObjData = matchObjData;
	}

	public PassiveStimSpecSpec(String stimType, long sampleObjData, long matchObjData) {
		this.stimType = stimType;
		this.sampleObjData = sampleObjData;
		this.matchObjData = matchObjData;
	}

	public PassiveStimSpecSpec() {
	}



	public long getSampleObjData() {
		return sampleObjData;
	}

	public void setSampleObjData(long sampleObjData) {
		this.sampleObjData = sampleObjData;
	}

	public long getMatchObjData() {
		return matchObjData;
	}

	public void setMatchObjData(long matchObjData) {
		this.matchObjData = matchObjData;
	}

	public static String toXml (PassiveStimSpecSpec spec) {
		return s.toXML(spec);
	}

	public String toXml() {
		return PassiveStimSpecSpec.toXml(this);
	}

	public static PassiveStimSpecSpec fromXml (String xml) {
		PassiveStimSpecSpec ss = (PassiveStimSpecSpec)s.fromXML(xml);
		return ss;
	}


	public String getStimType() {
		return stimType;
	}

	public void setStimType(String stimType) {
		this.stimType = stimType;
	}
}
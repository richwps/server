package org.n52.wps.server;


public abstract class AbstractConditionalAnnotatedAlgorithm extends AbstractAnnotatedAlgorithm implements IHideableAlgorithm, IConditionalAlgorithm {
	
	private boolean isHidden = true;

	@Override
	public boolean isHidden() {
		return this.isHidden;
	}

}

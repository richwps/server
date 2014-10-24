package net.disy.wps.richwps.process.conditional;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.AbstractConditionalAnnotatedAlgorithm;

@Algorithm(version = "1.0.0", title="Integer greater than integer", abstrakt="Conditional process which checks if integer a is greater than integer b")
public class CIntGtInt extends AbstractConditionalAnnotatedAlgorithm {

	int intA, intB;
	boolean result;
	
	@LiteralDataInput(identifier="intA",title="intA", binding=LiteralIntBinding.class)
	public void setIntA(int a) {
		intA = a;
	}
	
	@LiteralDataInput(identifier="intB",title="intB", binding=LiteralIntBinding.class)
	public void setIntB(int b) {
		intB = b;
	}
	
	@Override
	@LiteralDataOutput(identifier="result", title="result", binding=LiteralBooleanBinding.class)
	public boolean getResult() {
		return this.result;
	}
	
	@Execute
	public void run() {
		if (this.intA > this.intB) {
			this.result = true;
		}
		else {
			this.result = false;
		}
	}

}

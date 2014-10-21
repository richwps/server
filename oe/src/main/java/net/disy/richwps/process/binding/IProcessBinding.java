package net.disy.richwps.process.binding;

import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public interface IProcessBinding {

    Map<String, IData> executeProcess(Map<String, List<IData>> inputData, List<String> outputNames);

}

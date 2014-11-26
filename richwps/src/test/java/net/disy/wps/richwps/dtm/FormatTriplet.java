package net.disy.wps.richwps.dtm;

public class FormatTriplet<S, M, E> {

	S schema;
	M mimetype;
	E encoding;
	
	public FormatTriplet(S schema, M mimetype, E encoding) {
		this.schema = schema;
		this.mimetype = mimetype;
		this.encoding = encoding;
	}
	
	S getSchema() {
		return schema;
	}
	
	M getMimeType() {
		return mimetype;
	}
	
	E getEncoding() {
		return encoding;
			
	}
}

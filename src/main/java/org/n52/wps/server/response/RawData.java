/*****************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;

/*
 * @author foerster
 *
 */
public class RawData extends ResponseData {

	static Logger LOGGER = Logger.getLogger(RawData.class);
	/**
	 * @param obj
	 * @param id
	 * @param schema
	 * @param encoding
	 * @param mimeType
	 */
	public RawData(Object obj, String id, String schema, String encoding, String mimeType) throws ExceptionReport{
		super(obj, id, schema, encoding, mimeType);
		prepareGenerator();
		
	}
	
	public void save(OutputStream stream) throws ExceptionReport {
		this.storeRaw(stream, generator);
	}

	
}

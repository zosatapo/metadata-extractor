/*
 * Copyright 2002-2019 Drew Noakes and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.drew.metadata.heif.boxes;


import com.drew.lang.ByteArrayReader;
import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.icc.IccReader;

import java.io.IOException;

/**
 * ISO/IEC 14496-12:2015 pg.159
 */
public class ColourInformationBox extends Box
{
	String colourType;
	int colourPrimaries;
	int transferCharacteristics;
	int matrixCoefficients;
	int fullRangeFlag;

	public ColourInformationBox(RandomAccessReader reader, Box box,
			Metadata metadata) throws IOException
	{
		super(box);

		colourType = reader.getString(4);
		if (colourType.equals("nclx"))
		{
			colourPrimaries = reader.getUInt16();
			transferCharacteristics = reader.getUInt16();
			matrixCoefficients = reader.getUInt16();
			// Last 7 bits are reserved
			fullRangeFlag = (reader.getUInt8() & 0x80) >> 7;
		}
		else if (colourType.equals("rICC"))
		{
			byte[] buffer = reader.getBytes((int) (size - 12));
			new IccReader().extract(new ByteArrayReader(buffer), metadata);
		}
		else if (colourType.equals("prof"))
		{
			byte[] buffer = reader.getBytes((int) (size - 12));
			new IccReader().extract(new ByteArrayReader(buffer), metadata);
		}
		
		countBytesRead = reader.getPosition() - offset;
	}

	public void addMetadata(HeifDirectory directory)
	{

	}
}

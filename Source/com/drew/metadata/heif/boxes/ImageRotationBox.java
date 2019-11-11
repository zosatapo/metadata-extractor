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


import com.drew.lang.RandomAccessReader;
import com.drew.metadata.heif.HeifDirectory;

import java.io.IOException;

/**
 * ISO/IEC 23008-12:2017 pg.15
 */
public class ImageRotationBox extends Box
{
	int angle;

	public ImageRotationBox(RandomAccessReader reader, Box box)
			throws IOException
	{
		super(box);

		// First 6 bits are reserved
		angle = reader.getUInt8() & 0x03;
		
		countBytesRead = reader.getPosition() - offset;
	}

	public void addMetadata(HeifDirectory directory)
	{
		if (!directory.containsTag(HeifDirectory.TAG_IMAGE_ROTATION))
		{
			directory.setInt(HeifDirectory.TAG_IMAGE_ROTATION, angle);
		}
	}
}

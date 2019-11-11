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
package com.drew.imaging.heif;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

import com.drew.lang.RandomAccessFileReader;
import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.boxes.Box;

public class HeifReader
{
	public static void extract(Metadata metadata, RandomAccessFile streamFile, long streamLength, HeifHandler handler)
			throws IOException, DataFormatException
	{
		RandomAccessFileReader reader = new RandomAccessFileReader(streamFile);
		reader.setMotorolaByteOrder(true);
		processBoxes(0, reader, streamLength, handler);
	}

	public static void extract(Metadata metadata, RandomAccessReader reader,HeifHandler handler)
			throws IOException, DataFormatException
	{
		reader.setMotorolaByteOrder(true);
		processBoxes(0, reader, reader.getLength(), handler);
	}
	
	public static void extract(Metadata metadata, RandomAccessReader reader, long atomEnd,HeifHandler handler)
			throws IOException, DataFormatException
	{
		reader.setMotorolaByteOrder(true);
		processBoxes(0, reader, atomEnd, handler);
	}
	
	public static void processBoxes(int depth, RandomAccessReader reader, long atomEnd, HeifHandler handler)
	{
		try
		{
			while ((atomEnd == -1) ? true : reader.getPosition() < atomEnd)
			{

				Box box = new Box(reader);
				// Determine if fourCC is container/atom and process accordingly
				// Unknown atoms will be skipped

				if (handler.shouldAcceptContainer(box))
				{
					handler.processContainer(depth, box, reader);
				}
				else if (handler.shouldAcceptBox(box))
				{
					handler.processBox(depth, box, reader);
				}
				else if (box.size > 0)
				{
					reader.skip(box.size - box.countBytesRead);
				}
				else
				{
					break;
				}
			}

			handler.processCompleted(depth, reader);

		}
		catch (IOException e)
		{
			// Currently, reader relies on IOException to end
		}
	}
}

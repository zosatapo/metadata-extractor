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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifBoxHandler;

public class HeifMetadataReader
{
	public static Metadata readMetadata(File imageFile) throws IOException
	{
		RandomAccessFile streamFile = new RandomAccessFile(imageFile, "rw");
		try
		{
			return HeifMetadataReader.readMetadata(streamFile, streamFile.length());
		}
		finally
		{
			streamFile.close();
		}
	}

	public static Metadata readMetadata(RandomAccessFile streamFile) throws IOException
	{
		return readMetadata(streamFile, streamFile.length());
	}

	public static Metadata readMetadata(RandomAccessFile streamFile, long streamLength) throws IOException
	{
		try
		{
			Metadata metadata = new Metadata();
			HeifReader.extract(metadata, streamFile, streamLength, new HeifBoxHandler(metadata));
			return metadata;
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Metadata readMetadata(RandomAccessReader reader) throws IOException
	{
		return readMetadata(reader, reader.getLength());
	}

	public static Metadata readMetadata(RandomAccessReader reader, long atomEnd) throws IOException
	{
		try
		{
			Metadata metadata = new Metadata();
			HeifReader.extract(metadata, reader, atomEnd, new HeifBoxHandler(metadata));
			return metadata;
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}

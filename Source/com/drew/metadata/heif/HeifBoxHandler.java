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
package com.drew.metadata.heif;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.drew.imaging.heif.HeifHandler;
import com.drew.imaging.heif.HeifReader;
import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.boxes.Box;
import com.drew.metadata.heif.boxes.FileTypeBox;
import com.drew.metadata.heif.boxes.FullBox;
import com.drew.metadata.heif.boxes.HandlerBox;

public class HeifBoxHandler extends HeifHandler
{
	private HeifHandlerFactory handlerFactory = new HeifHandlerFactory(this);

	public HeifBoxHandler(Metadata metadata)
	{
		super(metadata);
	}

	@Override
	public boolean shouldAcceptContainer( Box box)
	{
		List<String> boxes = Arrays.asList(HeifContainerTypes.BOX_METADATA);
		return boxes.contains(box.type);
	}


	@Override
	public void processContainer(int depth, Box box,  RandomAccessReader reader) throws IOException
	{
		if (box.type.equals(HeifContainerTypes.BOX_METADATA))
		{			
			FullBox metaBox = new FullBox(reader, box);	
//			System.out.println(HeifReader.ZERO_PADDINGS[depth] + "box[processContainer] -->" + metaBox);
			HandlerBox handlerBox = new HandlerBox(reader, new Box(reader));
//			System.out.println(HeifReader.ZERO_PADDINGS[depth] + "box[processContainer] -->" + handlerBox);
			HeifHandler handler = handlerFactory.getHandler(handlerBox, this.metadata);
			HeifReader.processBoxes(++depth,reader, metaBox.offset + metaBox.size, handler);
		}
	}

	

	@Override
	public boolean shouldAcceptBox( Box box)
	{
		List<String> boxes = Arrays.asList(HeifBoxTypes.BOX_FILE_TYPE, HeifBoxTypes.BOX_HANDLER, HeifBoxTypes.BOX_HVC1);
		return boxes.contains(box.type);
	}

	
	@Override
	public void processBox(int depth, Box box,  RandomAccessReader reader) throws IOException
	{
		if (box.type.equals(HeifBoxTypes.BOX_FILE_TYPE))
		{
			processFileType(reader, box);
		}
	}
	
	@Override
	public  void processCompleted(int depth,  RandomAccessReader reader)
			throws IOException{}
	
	private void processFileType( RandomAccessReader reader,  Box box) throws IOException
	{
		FileTypeBox fileTypeBox = new FileTypeBox(reader, box);
		fileTypeBox.addMetadata(directory);
		if (!fileTypeBox.getCompatibleBrands().contains("mif1"))
		{
			directory.addError("File Type Box does not contain required brand, mif1");
		}
	}
}

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
import com.drew.lang.ByteArrayReader;
import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.heif.boxes.AuxiliaryTypeProperty;
import com.drew.metadata.heif.boxes.Box;
import com.drew.metadata.heif.boxes.ColourInformationBox;
import com.drew.metadata.heif.boxes.ImageRotationBox;
import com.drew.metadata.heif.boxes.ImageSpatialExtentsProperty;
import com.drew.metadata.heif.boxes.ItemInfoBox;
import com.drew.metadata.heif.boxes.ItemLocationBox;
import com.drew.metadata.heif.boxes.ItemProtectionBox;
import com.drew.metadata.heif.boxes.PixelInformationBox;
import com.drew.metadata.heif.boxes.PrimaryItemBox;
import com.drew.metadata.heif.boxes.ItemInfoBox.ItemInfoEntry;
import com.drew.metadata.heif.boxes.ItemLocationBox.Extent;
import com.drew.metadata.heif.boxes.ItemLocationBox.ItemLocation;

public class HeifPictureHandler extends HeifHandler
{
	ItemProtectionBox itemProtectionBox;
	PrimaryItemBox primaryItemBox;
	ItemInfoBox itemInfoBox;
	ItemLocationBox itemLocationBox;

	public HeifPictureHandler(Metadata metadata) {
		super(metadata);

		itemProtectionBox = null;
		primaryItemBox = null;
		itemInfoBox = null;
		itemLocationBox = null;
	}

	@Override
	protected boolean shouldAcceptContainer(Box box)
	{
		return box.type.equals(HeifContainerTypes.BOX_IMAGE_PROPERTY)
				|| box.type.equals(HeifContainerTypes.BOX_ITEM_PROPERTY);
	}

	@Override
	protected void processContainer(int depth, Box box, RandomAccessReader reader) throws IOException
	{
		reader.skip(box.countBytesUnread());
	}

	@Override
	protected boolean shouldAcceptBox(Box box)
	{
		List<String> boxes = Arrays.asList(HeifBoxTypes.BOX_ITEM_PROTECTION, HeifBoxTypes.BOX_PRIMARY_ITEM,
				HeifBoxTypes.BOX_ITEM_INFO, HeifBoxTypes.BOX_ITEM_LOCATION, HeifBoxTypes.BOX_IMAGE_SPATIAL_EXTENTS,
				HeifBoxTypes.BOX_AUXILIARY_TYPE_PROPERTY, HeifBoxTypes.BOX_IMAGE_ROTATION, HeifBoxTypes.BOX_COLOUR_INFO,
				HeifBoxTypes.BOX_PIXEL_INFORMATION);

		return boxes.contains(box.type);
	}

	@Override
	protected void processBox(int depth, Box box, RandomAccessReader reader) throws IOException
	{
		if (box.type.equals(HeifBoxTypes.BOX_ITEM_PROTECTION)) {
			itemProtectionBox = new ItemProtectionBox(reader, box);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_PRIMARY_ITEM)) {
			primaryItemBox = new PrimaryItemBox(reader, box);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_ITEM_INFO)) {
			itemInfoBox = new ItemInfoBox(reader, box);
//			System.out.println(HeifReader.ZERO_PADDINGS[depth] + itemInfoBox);
			itemInfoBox.addMetadata(directory);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_ITEM_LOCATION)) {
			itemLocationBox = new ItemLocationBox(reader, box);
//			System.out.println(HeifReader.ZERO_PADDINGS[depth] + itemLocationBox);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_IMAGE_SPATIAL_EXTENTS)) {
			ImageSpatialExtentsProperty imageSpatialExtentsProperty = new ImageSpatialExtentsProperty(reader, box);
			imageSpatialExtentsProperty.addMetadata(directory);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_AUXILIARY_TYPE_PROPERTY)) {
			AuxiliaryTypeProperty auxiliaryTypeProperty = new AuxiliaryTypeProperty(reader, box);
			auxiliaryTypeProperty.addMetadata(directory);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_IMAGE_ROTATION)) {
			ImageRotationBox imageRotationBox = new ImageRotationBox(reader, box);
			imageRotationBox.addMetadata(directory);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_COLOUR_INFO)) {
			ColourInformationBox colourInformationBox = new ColourInformationBox(reader, box, metadata);
			colourInformationBox.addMetadata(directory);
		}
		else if (box.type.equals(HeifBoxTypes.BOX_PIXEL_INFORMATION)) {
			PixelInformationBox pixelInformationBox = new PixelInformationBox(reader, box);
			pixelInformationBox.addMetadata(directory);
		}
	}

	@Override
	protected void processCompleted(int depth, RandomAccessReader reader) throws IOException
	{
		readExifMetadata(depth, reader);
	}

	private void readExifMetadata(int depth, RandomAccessReader reader) throws IOException
	{
		ItemInfoEntry exifEntry = itemInfoBox.getExifItemInfoEntry();
		if (exifEntry == null) { return; }

		ItemLocation location = itemLocationBox.getLocation((int) exifEntry.itemID);
		Extent extent = location.extents.get(0);
		long position = reader.getPosition();
		reader.seek(extent.offset);

		byte[] PREAMBLE = { 0, 0, 0, 6 };
		int EXIF_SEGMENT_PREAMBLE_OFFSET = PREAMBLE.length + ExifReader.JPEG_SEGMENT_PREAMBLE.length();

		ByteArrayReader exifReader = new ByteArrayReader(reader.getBytes(extent.length));
		new com.drew.metadata.exif.ExifReader().extract(exifReader, this.metadata, EXIF_SEGMENT_PREAMBLE_OFFSET, null);

		reader.seek(position);

	}
}

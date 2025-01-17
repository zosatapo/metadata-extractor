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

import java.io.IOException;

import com.drew.lang.Charsets;
import com.drew.lang.RandomAccessReader;
import com.drew.metadata.heif.HeifDirectory;

/**
 * ISO/IEC 14496-12:2015 pg.81-83
 */
public class ItemInfoBox extends FullBox
{
	long entryCount;
	ItemInfoEntry[] entries;
	ItemInfoEntry exifEntry = null;

	public ItemInfoBox(RandomAccessReader reader, Box box) throws IOException
	{
		super(reader, box);

		if (version == 0)
		{
			entryCount = reader.getUInt16();
		}
		else
		{
			entryCount = reader.getUInt32();
		}

		entries = new ItemInfoEntry[(int) entryCount + 1];
		for (int i = 1; i <= entryCount; i++)
		{
			entries[i] = new ItemInfoEntry(reader, new Box(reader));
			if ("Exif".equals(entries[i].itemType))
			{
				exifEntry = entries[i];
			}
		}

		countBytesRead = reader.getPosition() - offset;
	}

	public ItemInfoEntry getExifItemInfoEntry()
	{
		return exifEntry;
	}
	
	public static class ItemInfoEntry extends FullBox
	{
		public long itemID;
		public long itemProtectionIndex;
		public String itemName;
		public String contentType;
		public String contentEncoding;
		public String extensionType;
		public String itemType;
		public String itemUriType;

		public ItemInfoEntry(RandomAccessReader reader, Box box) throws IOException
		{
			super(reader, box);

			int remainBytes = 0;

			if ((version == 0) || (version == 1))
			{
				itemID = reader.getUInt16();
				itemProtectionIndex = reader.getUInt16();

				remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
				if (remainBytes > 0)
				{
					itemName = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);

					remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
					if (remainBytes > 0)
					{
						contentType = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);

						remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
						if (remainBytes > 0)
						{
							extensionType = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);
						}
					}

				}

			}

			if (version == 1)
			{
				remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
				if (remainBytes >= 4)
				{
					contentEncoding = reader.getString(4);
				}
			}

			if (version >= 2)
			{
				if (version == 2)
				{
					itemID = reader.getUInt16();
				}
				else if (version == 3)
				{
					itemID = reader.getUInt32();
				}

				itemProtectionIndex = reader.getUInt16();
				itemType = reader.getString(4);

				remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
				if (remainBytes > 0)
				{
					itemName = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);
					if (itemType.equals("mime"))
					{
						remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
						if (remainBytes > 0)
						{
							contentType = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);

							remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
							if (remainBytes > 0)
							{
								contentEncoding = reader.getNullTerminatedString(remainBytes, Charsets.UTF_8);
							}
						}
					}
					else if (itemType.equals("uri"))
					{
						remainBytes = (int) (this.size - (reader.getPosition() - this.offset));
						if (remainBytes > 0)
						{
							itemUriType = reader.getString(remainBytes);
						}

					}
				}
			}

			countBytesRead = reader.getPosition() - offset;

			//System.out.println(itemID + ", " + itemProtectionIndex + ", " + itemType);
		}

	}

	public void addMetadata(HeifDirectory directory)
	{

	}
}

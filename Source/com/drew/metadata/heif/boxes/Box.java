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

import com.drew.lang.RandomAccessReader;

/**
 * ISO/IEC 14496-12:2015 pg.6
 */
public class Box
{
	private long firstsize;
	private long largesize;
	public String type;
	public String usertype;

	public long size;
	public long offset;
	public long countBytesRead;

	public Box(RandomAccessReader reader) throws IOException
	{
		this.offset = reader.getPosition();
		
		this.firstsize = reader.getUInt32();
		this.type = reader.getString(4);
		if (firstsize == 1)
		{
			largesize = reader.getInt64();
		}

		if (type.equals("uuid"))
		{
			usertype = reader.getString(16);
		}

		countBytesRead = reader.getPosition() - offset;
		size = (firstsize == 1) ? largesize : firstsize;
	}

	public Box(Box box)
	{
		this.offset = box.offset;
		this.countBytesRead = box.countBytesRead;
		this.size = box.size;

		this.firstsize = box.firstsize;
		this.largesize = box.largesize;
		this.type = box.type;
		this.usertype = box.usertype;
	}

	public boolean isLastBox()
	{
		return firstsize == 0;
	}

	public long countBytesUnread()
	{
		return this.size - this.countBytesRead;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[" + this.getClass().getName() + "]-[" + this.type + "]");
		sb.append(" offset=" + this.offset);
		sb.append(", size=" + this.size);
		sb.append(", bytesRead=" + this.countBytesRead);
		return sb.toString();
	}
}

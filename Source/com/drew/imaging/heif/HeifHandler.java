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

import com.drew.lang.RandomAccessReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.heif.boxes.Box;

public abstract class HeifHandler
{
	protected Metadata metadata;
	protected HeifDirectory directory;

	public HeifHandler(Metadata metadata)
	{
		this.metadata = metadata;
		this.directory = new HeifDirectory();
		this.metadata.addDirectory(directory);
	}

	protected abstract boolean shouldAcceptBox(Box box);

	protected abstract boolean shouldAcceptContainer(Box box);

	protected abstract void processBox(int depth, Box box, RandomAccessReader reader) throws IOException;

	/**
	 * There is potential for a box to both contain other boxes and contain
	 * information, so this method will handle those occurences.
	 */
	protected abstract void processContainer(int depth, Box box, RandomAccessReader reader) throws IOException;

	protected abstract void processCompleted(int depth, RandomAccessReader reader) throws IOException;
}

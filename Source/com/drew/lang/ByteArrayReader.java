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

package com.drew.lang;

import java.io.EOFException;
import java.io.IOException;

import com.drew.lang.annotations.NotNull;

/**
 * Provides methods to read specific values from a byte array, with a
 * consistent, checked exception structure for issues.
 * <p>
 * By default, the reader operates with Motorola byte order (big endianness).
 * This can be changed by calling <code>setMotorolaByteOrder(boolean)</code>.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class ByteArrayReader extends RandomAccessReader
{
	@NotNull
	private final byte[] _buffer;
	private final int _baseOffset;
	private long _localOffset = 0;

	@com.drew.lang.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2", justification = "Design intent")
	public ByteArrayReader(@NotNull byte[] buffer)
	{
		this(buffer, 0);
	}

	@com.drew.lang.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2", justification = "Design intent")
	public ByteArrayReader(@NotNull byte[] buffer, int baseOffset)
	{
		if (buffer == null)
			throw new NullPointerException();
		if (baseOffset < 0)
			throw new IllegalArgumentException("Must be zero or greater");

		_buffer = buffer;
		_baseOffset = baseOffset;
		_localOffset = 0;
	}

	@Override
	public long toUnshiftedOffset(long localOffset)
	{
		return localOffset + _baseOffset;
	}

	@Override
	public long getLength()
	{ return _buffer.length - _baseOffset; }

	@Override
	public byte getByte(long index) throws IOException
	{
		validateIndex(index, 1);
		_localOffset = index;
		return _buffer[(int) index + _baseOffset];
	}

	@Override
	protected void validateIndex(long index, long bytesRequested) throws IOException
	{
		if (!isValidIndex(index, bytesRequested))
			throw new BufferBoundsException(toUnshiftedOffset(index), bytesRequested, _buffer.length);
	}

	@Override
	protected boolean isValidIndex(long index, long bytesRequested) throws IOException
	{
		return bytesRequested >= 0 && index >= 0 && (long) index + (long) bytesRequested - 1L < getLength();
	}

	@Override
	@NotNull
	public byte[] getBytes(long index, long count) throws IOException
	{
		validateIndex(index, count);
		_localOffset = index + count;
		byte[] bytes = new byte[(int) count];
		System.arraycopy(_buffer, (int) index + _baseOffset, bytes, 0, (int) count);
		return bytes;
	}

	@Override
	public void seek(long index) throws IOException
	{
		validateIndex(index, 0);
		_localOffset = index;
	}

	@Override
	public long getPosition() throws IOException
	{ return _localOffset; }

	@Override
	public void skip(long n) throws IOException
	{
		if (n < 0)
			throw new IllegalArgumentException("n must be zero or greater.");

		long skippedCount = skipInternal(n);

		if (skippedCount != n)
			throw new EOFException(
					String.format("Unable to skip. Requested %d bytes but skipped %d.", n, skippedCount));
	}

	@Override
	public boolean trySkip(long n) throws IOException
	{
		if (n < 0)
			throw new IllegalArgumentException("n must be zero or greater.");

		return skipInternal(n) == n;
	}

	private long skipInternal(long n) throws IOException
	{

		validateIndex(_localOffset, n);
		long skippedTotal = n;
		_localOffset += skippedTotal;
		return skippedTotal;
	}
}

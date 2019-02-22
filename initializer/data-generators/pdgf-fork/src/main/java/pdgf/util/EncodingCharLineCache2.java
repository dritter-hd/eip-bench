/*******************************************************************************
 * Copyright (c) 2011, Chair of Distributed Information Systems, University of Passau. 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *     this list of conditions and the following disclaimer. 
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *     notice, this list of conditions and the following disclaimer in the 
 *     documentation and/or other materials provided with the distribution. 
 * 
 * 3. Neither the name of the University of Passau nor the names of its 
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 ******************************************************************************/
package pdgf.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public final class EncodingCharLineCache2 {

	private Charset charset;
	private CharsetEncoder encoder;
	private CharBuffer cb;
	private ByteBuffer bb;
	private int bufSize;
	private char[] buf;
	private byte[] byteBuf;
	private int curLen = 0;
	private int expandCounter = 0;
	private boolean directEncoding;

	public static final int WRITE_TO_CHANNEL = 1;
	public static final int WRITE_TO_WRITER = 2;
	public static final int WRITE_TO_STREAM = 3;
	public static final int DEFAULT_WRITE_DESTINATION = WRITE_TO_CHANNEL;
	private int destination = DEFAULT_WRITE_DESTINATION;

	private int minBufSize;
	private FileOutputStream out;
	private Writer osw;
	private boolean expandAllowed = false;
	private FileChannel c;
	private boolean autoFlushOn = false;

	public EncodingCharLineCache2() {
		this(Charset.defaultCharset());
	}

	public EncodingCharLineCache2(String charset) {
		this(Charset.forName(charset));
	}

	public EncodingCharLineCache2(Charset charset) {
		this(charset, 64, DEFAULT_WRITE_DESTINATION);

	}

	public EncodingCharLineCache2(Charset charset, int bufSize, int destination) {

		this.charset = charset;
		this.bufSize = bufSize;
		encoder = charset.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		cb = CharBuffer.allocate(bufSize);
		buf = cb.array();

		bb = ByteBuffer.allocate((int) (encoder.maxBytesPerChar() * bufSize));
		byteBuf = bb.array();
		// bb = ByteBuffer.allocate((int) (encoder.maxBytesPerChar()*bufSize));

		// bb.position(bb.limit());

		switch (destination) {
		case WRITE_TO_CHANNEL:
		case WRITE_TO_WRITER:
		case WRITE_TO_STREAM:
			this.destination = destination;

			break;

		default:
			throw new RuntimeException("Writing destination is unkown");

		}

	}

	public EncodingCharLineCache2(Charset charset, int minBufSize,
			FileOutputStream out, int destination) {
		// underlying buffer is 1/10 bigger than flush threshold minBufSize
		this(charset, minBufSize * 11 / 10, destination);

		this.minBufSize = minBufSize;
		try {
			setOutputStream(out);
		} catch (IOException e) {
			// cannot happen at this point
		}

	}

	public void setOutputStream(FileOutputStream newout) throws IOException {

		if (newout != null) {
			autoFlushOn = true;
			if (this.out != null) {
				flush();
			}
			this.out = newout;
			this.c = newout.getChannel();
			this.curLen = 0;
			// Writer w is a StreamEncoder;
			osw = Channels.newWriter(c, charset.newEncoder(), minBufSize);

			// osw = new OutputStreamWriter(out, charset);
		}
	}

	/**
	 * encode using <br/>
	 * for(int i = 0 ; i< charBuf.length ; i++){<br/>
	 * byteBuf[i] = (byte) charBuf[i]; <br/>
	 * }
	 * 
	 * @param encodeDirect
	 */
	public void setEncodeDirect(boolean encodeDirect) {
		this.directEncoding = encodeDirect;
	}

	/**
	 * Current buffer encoded in bytes, null if buffer length is 0;
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] getBytes() throws IOException {
		if (curLen > 0) {
			byte[] rv = new byte[bb.remaining()];
			getEncodedBuffer().get(rv);
			return rv;

		} else {
			return null;
		}
	}

	public int getLength() {
		return curLen;
	}

	/**
	 * get capacity of internal char buffer
	 * 
	 * @return
	 */
	public int getCapacity() {
		return buf.length;
	}

	/**
	 * clear internal buffer
	 */
	public void clear() {
		curLen = 0;
	}

	/**
	 * 
	 * @param dst
	 * @param destStart
	 * @param len
	 * @return true if something was copied
	 */
	public boolean copyTo(char[] dst, int destStart, int len) {
		if (curLen > 0) {

			updateCharBuffer();
			cb.get(dst, destStart, len);
			return true;
		}
		return false;
	}

	public void append(String line) {

		int newLen = curLen + line.length();
		if (newLen > buf.length) {
			expand(newLen);
		}
		line.getChars(0, line.length(), buf, curLen);
		curLen = newLen;

	}

	public void append(char[] line, int start, int len) {

		int newLen = curLen + len;
		if (newLen > buf.length) {
			expand(newLen);
		}
		System.arraycopy(line, start, buf, curLen, len);

		curLen = newLen;
	}

	public void append(char[] line) {
		append(line, 0, line.length);
	}

	public void append(char aChar) {
		int newLen = curLen + 1;
		if (curLen + 1 > buf.length) {
			expand(newLen);
		}

		buf[curLen] = aChar;
		curLen = newLen;
	}

	void expand(int minimum) {
		expandCounter++;
		// expand by 20%
		int newBufSize = (buf.length + 1) * 2;

		// we have an int overflow here
		if (newBufSize < 0) {
			newBufSize = Integer.MAX_VALUE;
		} else if (newBufSize < minimum) {
			newBufSize = minimum;
		}
		char[] tmp = new char[newBufSize];
		System.out.println("Expand from " + buf.length + " to " + newBufSize
				+ ". this happend: " + expandCounter + " times");
		System.arraycopy(buf, 0, tmp, 0, curLen);
		buf = tmp;
		cb = CharBuffer.wrap(buf);

	}

	private void updateCharBuffer() {
		cb.clear();
		cb.position(curLen);
		/*
		 * what flip does: limit = position; position = 0; mark = -1; so its not
		 * necessary to call cb.clear before
		 */
		cb.flip();
	}

	/*
	 * public void append(String line) {
	 * 
	 * cb.put(line);
	 * 
	 * }
	 * 
	 * public void append(char[] line, int start, int len) {
	 * 
	 * cb.put(line, start, len); }
	 * 
	 * public void append(char[] line) { append(line, 0, line.length); }
	 * 
	 * public void append(char aChar) { cb.put(aChar); }
	 */

	/**
	 * If an outputstream was not provided via constructor this method will
	 * throw an IO exeption
	 */
	public void flush() throws IOException {

		switch (destination) {
		case WRITE_TO_CHANNEL:

			flushToChannel(c);

			break;
		case WRITE_TO_WRITER:

			flushToWriter(osw);

			break;
		case WRITE_TO_STREAM:

			flushToStream(out);

			break;

		}

	}

	/**
	 * call this method after a line was completely written to this buffer. This
	 * method may flush the buffer if minBufSize has been exceeded.
	 * 
	 * @throws IOException
	 */
	public void flushIfFull() throws IOException {
		if (getLength() >= minBufSize) {
			// capacity reached, flush to stream
			// System.out.println("do flush...");
			flush();

		}
	}

	public void flushToStream(FileOutputStream out) throws IOException {
		if (curLen > 0) {

			if (directEncoding) {
				for (int i = 0; i < curLen; i++) {
					byteBuf[i] = (byte) buf[i];
				}
				out.write(byteBuf, 0, curLen);
				clear();
			} else {
				getEncodedBuffer();
				if (bb.hasArray()) {
					// does only work if buffer is back by an array and not
					// direct
					out.write(bb.array());

				} else {
					byte[] dst = new byte[bb.limit()];
					bb.get(dst);
					out.write(dst);

				}
				clear();
			}
		}
	}

	public void flushToWriter(Writer w) throws IOException {
		if (curLen > 0) {
			w.write(buf, 0, curLen);
			clear();
		}
	}

	public void flushToChannel(WritableByteChannel out) throws IOException {
		if (curLen > 0) {
			out.write(getEncodedBuffer());

			clear();
		}
	}

	ByteBuffer getEncodedBuffer() throws IOException {
		// only encode if min one char is cached
		if (curLen > 0) {
			if (directEncoding) {
				for (int i = 0; i < curLen; i++) {
					byteBuf[i] = (byte) buf[i];
				}
				bb.clear();
				/*
				 * what flip later does: limit = position; position = 0; mark =
				 * -1; so its not necessary to call bb.limit or bb.clear before
				 */

				bb.position(curLen);

			} else {

				updateCharBuffer();
				bb.clear();
				encoder.reset();
				encoder.encode(cb, bb, true);
				encoder.flush(bb);

			}

			bb.flip();
			return bb;
		}
		return null;
	}

	public static byte[] encode(char achar) {
		return encode(new char[] { achar }, Charset.defaultCharset());
	}

	public static byte[] encode(char achar, Charset cs) {
		return encode(new char[] { achar }, cs);
	}

	public static byte[] encode(char[] line) {
		return encode(line, Charset.defaultCharset());
	}

	public static byte[] encode(char[] line, Charset cs) {
		ByteBuffer buffer = cs.encode(CharBuffer.wrap(line, 0, line.length));
		return buffer.array();

	}
}
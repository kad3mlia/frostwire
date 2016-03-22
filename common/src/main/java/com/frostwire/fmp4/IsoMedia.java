/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.fmp4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * @author gubatron
 * @author aldenml
 */
public final class IsoMedia {

    private IsoMedia() {
    }

    static void read(InputChannel in) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1 * 1024);
        read(in, -1, null, buf);
    }

    static void read(InputChannel in, long len) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1 * 1024);
        read(in, len, null, buf);
    }

    public static void read(InputChannel ch, long len, Box parent, ByteBuffer buf) throws IOException {
        long n = ch.count();
        do {
            IO.read(ch, 8, buf);

            int size = buf.getInt();
            int type = buf.getInt();

            Long largesize = null;
            if (size == 1) {
                IO.read(ch, 8, buf);
                largesize = buf.getLong();
            }

            byte[] usertype = null;
            if (type == Box.uuid) {
                usertype = new byte[16];
                IO.read(ch, 16, buf);
                buf.get(usertype);
            }

            System.out.println(Bits.make4cc(type));
            Box b = Box.empty(type);
            b.size = size;
            b.type = type;
            b.largesize = largesize;
            b.usertype = usertype;
            long r = ch.count();
            b.read(ch, buf);
            r = ch.count() - r;

            if (type == Box.mdat) {
                System.out.println("need to handle mdat");
                //return;
            }

            if (r < b.length()) {
                read(ch, b.length() - r, b, buf);
            }
        } while (len == -1 || ch.count() - n < len);
    }
}
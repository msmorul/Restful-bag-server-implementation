/*
 * Copyright (c) 2007-2011, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
package org.chronopolis.bagserver;

import java.io.IOException;

/**
 *
 * @author toaster
 */
public interface BagCheckListener {

    public void badBagLine(BagChecker checker, String file, String line);

    public void missingBagInfo(BagChecker checker);

    public void missingBagIt(BagChecker checker);

    public void threadEnding(BagChecker checker);
    public void missingFile(BagChecker checker, String file);
    public void errorReadingFile(BagChecker checker, String file, IOException e);

    /**
     * A file doesn't have a digest in all available manifests
     * @param checker
     * @param file file with missing digest
     * @param algorithm manifest where digest is missing
     */
    public void missingDigest(BagChecker checker, String file, String algorithm);

    /**
     * A file's digest does not match the expected digest;
     *
     * @param checker
     * @param file - file w/ bad digest
     * @param algorithm - digest algorithm where mismatch occurred
     * @param expected - digest in manifest file
     * @param seen - digest calculated after reading file
     */
    public void corruptFile(BagChecker checker, String file, String algorithm,String expected, String seen);

    /**
     * A file was found on disk that does not exist in a manifest
     * @param checker
     * @param file
     */
    public void extraFile(BagChecker checker, String file);

    public class Adapter implements BagCheckListener {

        public void missingDigest(BagChecker checker, String file,String algorithm) {
        }

        public void errorReadingFile(BagChecker checker, String file, IOException e) {
        }

        public void corruptFile(BagChecker checker, String file, String algorithm, String expected, String seen) {
        }

        public void missingFile(BagChecker checker, String file) {
        }

        public void badBagLine(BagChecker checker, String file, String line) {
        }

        public void threadEnding(BagChecker checker) {
        }

        public void missingBagIt(BagChecker checker) {
        }

        public void missingBagInfo(BagChecker checker) {
        }

        public void extraFile(BagChecker checker, String file) {
        }
    }
}

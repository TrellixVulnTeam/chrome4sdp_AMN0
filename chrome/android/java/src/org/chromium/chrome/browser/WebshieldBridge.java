/* Copyright (c) 2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *      * Neither the name of The Linux Foundation nor the names of its
 *      contributors may be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT,INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chromium.chrome.browser;

import org.chromium.base.annotations.AccessedByNative;
import org.chromium.base.annotations.CalledByNative;
import org.chromium.base.annotations.CalledByNativeUnchecked;
import org.chromium.base.annotations.JNINamespace;
import org.chromium.base.annotations.NativeCall;
import org.chromium.base.annotations.NativeClassQualifiedName;

@JNINamespace("chrome::android")
public class WebshieldBridge {
    public WebshieldBridge() {
    }

    public int getXSSDefVersion() {
        return nativeGetXSSDefVersion();
    }

    public int getErrorType() {
        return nativeGetErrorType();
    }

    public boolean isMalicious(String url, int type) {
        return nativeIsMalicious(url, type);
    }

    public boolean checkURL(String url, int type) {
        return nativeCheckURL(url, type);
    }

    public String filterURL(String url) {
        return nativeFilterURL(url);
    }

    public boolean loadLibrary() {
        return nativeLoadLibrary();
    }

    public void sendUserResponse(String url, boolean result) {
        nativeSendUserResponse(url, result);
    }

    private native int nativeGetXSSDefVersion();
    private native int nativeGetErrorType();
    private native boolean nativeIsMalicious(String url, int type);
    private native boolean nativeCheckURL(String url, int type);
    private native String nativeFilterURL(String url);
    private native boolean nativeLoadLibrary();
    private native void nativeSendUserResponse(String url, boolean result);
}
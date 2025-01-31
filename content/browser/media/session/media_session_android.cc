// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "content/browser/media/session/media_session_android.h"

#include <algorithm>
#include "content/browser/media/session/media_session_impl.h"
#include "content/browser/web_contents/web_contents_android.h"
#include "content/browser/web_contents/web_contents_impl.h"
#include "content/common/android/media_metadata_android.h"
#include "content/public/browser/media_session.h"
#include "jni/MediaSessionImpl_jni.h"

namespace content {

using base::android::JavaParamRef;
using base::android::ScopedJavaLocalRef;

struct MediaSessionAndroid::JavaObjectGetter {
  static ScopedJavaLocalRef<jobject> GetJavaObject(
      MediaSessionAndroid* session_android) {
    return session_android->GetJavaObject();
  }
};

MediaSessionAndroid::MediaSessionAndroid(MediaSessionImpl* session)
    : MediaSessionObserver(session) {
  JNIEnv* env = base::android::AttachCurrentThread();
  ScopedJavaLocalRef<jobject> j_media_session =
      Java_MediaSessionImpl_create(env, reinterpret_cast<intptr_t>(this));
  j_media_session_ = JavaObjectWeakGlobalRef(env, j_media_session);

  WebContentsAndroid* contents_android = GetWebContentsAndroid();
  if (contents_android)
    contents_android->SetMediaSession(j_media_session);
}

MediaSessionAndroid::~MediaSessionAndroid() = default;

// static
bool MediaSessionAndroid::Register(JNIEnv* env) {
  return RegisterNativesImpl(env);
}

// static
ScopedJavaLocalRef<jobject> GetMediaSessionFromWebContents(
    JNIEnv* env,
    const JavaParamRef<jclass>& clazz,
    const JavaParamRef<jobject>& j_contents_android) {
  WebContents* contents = WebContents::FromJavaWebContents(j_contents_android);
  if (!contents)
    return ScopedJavaLocalRef<jobject>();

  MediaSessionImpl* session = MediaSessionImpl::Get(contents);
  DCHECK(session);
  return MediaSessionAndroid::JavaObjectGetter::GetJavaObject(
      session->session_android());
}

void MediaSessionAndroid::MediaSessionDestroyed() {
  ScopedJavaLocalRef<jobject> j_local_session = GetJavaObject();
  if (j_local_session.is_null())
    return;

  JNIEnv* env = base::android::AttachCurrentThread();
  // The Java object will tear down after this call.
  Java_MediaSessionImpl_mediaSessionDestroyed(env, j_local_session);
  j_media_session_.reset();

  WebContentsAndroid* contents_android = GetWebContentsAndroid();
  if (contents_android)
    contents_android->SetMediaSession(nullptr);
}

void MediaSessionAndroid::MediaSessionStateChanged(bool is_controllable,
                                                   bool is_suspended) {
  ScopedJavaLocalRef<jobject> j_local_session = GetJavaObject();
  if (j_local_session.is_null())
    return;

  JNIEnv* env = base::android::AttachCurrentThread();
  Java_MediaSessionImpl_mediaSessionStateChanged(env, j_local_session,
                                                 is_controllable, is_suspended);
}

void MediaSessionAndroid::MediaSessionMetadataChanged(
    const base::Optional<MediaMetadata>& metadata) {
  ScopedJavaLocalRef<jobject> j_local_session = GetJavaObject();
  if (j_local_session.is_null())
    return;

  JNIEnv* env = base::android::AttachCurrentThread();

  // Avoid translating metadata through JNI if there is no Java observer.
  if (!Java_MediaSessionImpl_hasObservers(env, j_local_session))
    return;

  ScopedJavaLocalRef<jobject> j_metadata;
  if (metadata.has_value())
    j_metadata = MediaMetadataAndroid::CreateJavaObject(env, metadata.value());
  Java_MediaSessionImpl_mediaSessionMetadataChanged(env, j_local_session,
                                                    j_metadata);
}

void MediaSessionAndroid::MediaSessionEnabledAction(
    blink::mojom::MediaSessionAction action) {
  ScopedJavaLocalRef<jobject> j_local_session = GetJavaObject();
  if (j_local_session.is_null())
    return;

  JNIEnv* env = base::android::AttachCurrentThread();
  Java_MediaSessionImpl_mediaSessionEnabledAction(env, j_local_session,
                                                  static_cast<int>(action));
}

void MediaSessionAndroid::MediaSessionDisabledAction(
    blink::mojom::MediaSessionAction action) {
  ScopedJavaLocalRef<jobject> j_local_session = GetJavaObject();
  if (j_local_session.is_null())
    return;

  JNIEnv* env = base::android::AttachCurrentThread();
  Java_MediaSessionImpl_mediaSessionDisabledAction(env, j_local_session,
                                                   static_cast<int>(action));
}

void MediaSessionAndroid::Resume(
    JNIEnv* env,
    const base::android::JavaParamRef<jobject>& j_obj) {
  DCHECK(media_session());
  media_session()->Resume(MediaSession::SuspendType::UI);
}

void MediaSessionAndroid::Suspend(
    JNIEnv* env,
    const base::android::JavaParamRef<jobject>& j_obj) {
  DCHECK(media_session());
  media_session()->Suspend(MediaSession::SuspendType::UI);
}

void MediaSessionAndroid::Stop(
    JNIEnv* env,
    const base::android::JavaParamRef<jobject>& j_obj) {
  DCHECK(media_session());
  media_session()->Stop(MediaSession::SuspendType::UI);
}

void MediaSessionAndroid::DeltaSeek(
    JNIEnv* env,
    const base::android::JavaParamRef<jobject>& j_obj,
    jint delta) {
  DCHECK(media_session());
  media_session()->DeltaSeek(delta);
}

void MediaSessionAndroid::DidReceiveAction(JNIEnv* env,
                                           const JavaParamRef<jobject>& obj,
                                           int action) {
  media_session()->DidReceiveAction(
      static_cast<blink::mojom::MediaSessionAction>(action));
}

WebContentsAndroid* MediaSessionAndroid::GetWebContentsAndroid() {
  MediaSessionImpl* session = static_cast<MediaSessionImpl*>(media_session());
  if (!session)
    return nullptr;
  WebContentsImpl* contents =
      static_cast<WebContentsImpl*>(session->web_contents());
  if (!contents)
    return nullptr;
  return contents->GetWebContentsAndroid();
}

ScopedJavaLocalRef<jobject> MediaSessionAndroid::GetJavaObject() {
  JNIEnv* env = base::android::AttachCurrentThread();
  return j_media_session_.get(env);
}

}  // namespace content

// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/android/download/download_controller_base.h"
#include "chrome/common/chrome_content_client.h"
#include "content/public/browser/resource_request_info.h"
#include "net/url_request/url_request.h"

// static
DownloadControllerBase* DownloadControllerBase::download_controller_ = nullptr;

using content::ResourceRequestInfo;

DownloadInfo::DownloadInfo(const net::URLRequest* request)
    : has_user_gesture(false) {
  request->GetResponseHeaderByName("content-disposition", &content_disposition);

  if (request->response_headers()) {
    request->response_headers()->GetMimeType(&original_mime_type);
    total_bytes = request->response_headers()->GetContentLength();
  }

  request->extra_request_headers().GetHeader(
    net::HttpRequestHeaders::kUserAgent, &user_agent);
  if (user_agent.empty())
    user_agent = GetUserAgent();
  GURL referer_url(request->referrer());
  if (referer_url.is_valid())
    referer = referer_url.spec();
  if (!request->url_chain().empty()) {
    original_url = request->url_chain().front();
    url = request->url_chain().back();
  }

  const ResourceRequestInfo* info = ResourceRequestInfo::ForRequest(request);
  if (info)
    has_user_gesture = info->HasUserGesture();
}

DownloadInfo::DownloadInfo(const DownloadInfo& other) = default;

DownloadInfo::~DownloadInfo() {}

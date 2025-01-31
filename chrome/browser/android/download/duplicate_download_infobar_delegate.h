// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef CHROME_BROWSER_ANDROID_DOWNLOAD_DUPLICATE_DOWNLOAD_INFOBAR_DELEGATE_H_
#define CHROME_BROWSER_ANDROID_DOWNLOAD_DUPLICATE_DOWNLOAD_INFOBAR_DELEGATE_H_

#include "components/infobars/core/confirm_infobar_delegate.h"

class InfoBarService;

namespace chrome {
namespace android {

// An infobar that asks if user wants to continue downloading when there is
// already a duplicate file in storage. If user chooses to proceed,
// a new file will be created.
// Note that this infobar does not expire if the user subsequently navigates,
// since such navigations won't automatically cancel the underlying download.
class DuplicateDownloadInfoBarDelegate : public ConfirmInfoBarDelegate {
 public:
  // Gets the file path to be downloaded.
  virtual std::string GetFilePath() const = 0;
  // Gets the total bytes of the file to be downloaded.
  virtual int64_t GetTotalBytes() const = 0;
  // Gets the mime type of the file to be downloaded.
  virtual std::string GetMimeType() const = 0;
  // Sets full download directory path.
  virtual bool SetDirFullPath(const std::string& dir_full_path) = 0;
  // Whether the download is for offline page.
  virtual bool IsOfflinePage() const;

  virtual std::string GetPageURL() const;

  virtual bool IsOffTheRecord() const;

  // ConfirmInfoBarDelegate implementation.
  base::string16 GetMessageText() const override;
  bool ShouldExpire(const NavigationDetails& details) const override;
};

}  // namespace android
}  // namespace chrome

#endif  // CHROME_BROWSER_ANDROID_DOWNLOAD_DUPLICATE_DOWNLOAD_INFOBAR_DELEGATE_H_

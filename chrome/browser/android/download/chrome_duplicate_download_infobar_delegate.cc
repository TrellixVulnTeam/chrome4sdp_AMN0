// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/android/download/chrome_duplicate_download_infobar_delegate.h"

#include <memory>

#include "base/android/path_utils.h"
#include "base/memory/ptr_util.h"
#include "base/strings/stringprintf.h"
#include "chrome/browser/android/download/download_controller.h"
#include "chrome/browser/download/download_path_reservation_tracker.h"
#include "chrome/browser/infobars/infobar_service.h"
#include "chrome/browser/ui/android/infobars/duplicate_download_infobar.h"
#include "components/infobars/core/infobar.h"
#include "content/public/browser/browser_context.h"
#include "content/public/browser/browser_thread.h"

namespace {

void CreateNewFileDone(
    const DownloadTargetDeterminerDelegate::FileSelectedCallback& callback,
    const base::FilePath& target_path, bool verified) {
  DCHECK_CURRENTLY_ON(content::BrowserThread::UI);
  if (verified)
    callback.Run(target_path);
  else
    callback.Run(base::FilePath());
}

}  // namespace

namespace chrome {
namespace android {

ChromeDuplicateDownloadInfoBarDelegate::
    ~ChromeDuplicateDownloadInfoBarDelegate() {
  if (download_item_)
    download_item_->RemoveObserver(this);
}

// static
void ChromeDuplicateDownloadInfoBarDelegate::Create(
    InfoBarService* infobar_service,
    content::DownloadItem* download_item,
    const base::FilePath& file_path,
    const DownloadTargetDeterminerDelegate::FileSelectedCallback& callback) {
  infobar_service->AddInfoBar(DuplicateDownloadInfoBar::CreateInfoBar(
      base::WrapUnique(new ChromeDuplicateDownloadInfoBarDelegate(
          download_item, file_path, callback))));
}

void ChromeDuplicateDownloadInfoBarDelegate::OnDownloadDestroyed(
    content::DownloadItem* download_item) {
  DCHECK_EQ(download_item, download_item_);
  download_item_ = nullptr;
}

ChromeDuplicateDownloadInfoBarDelegate::ChromeDuplicateDownloadInfoBarDelegate(
    content::DownloadItem* download_item,
    const base::FilePath& file_path,
    const DownloadTargetDeterminerDelegate::FileSelectedCallback&
        file_selected_callback)
    : download_item_(download_item),
      file_path_(file_path),
      is_off_the_record_(download_item->GetBrowserContext()->IsOffTheRecord()),
      file_selected_callback_(file_selected_callback) {
  download_item_->AddObserver(this);
}

infobars::InfoBarDelegate::InfoBarIdentifier
ChromeDuplicateDownloadInfoBarDelegate::GetIdentifier() const {
  return CHROME_DUPLICATE_DOWNLOAD_INFOBAR_DELEGATE;
}

bool ChromeDuplicateDownloadInfoBarDelegate::Accept() {
  if (!download_item_)
    return true;

  base::FilePath download_dir;
  if (!base::android::GetDownloadsDirectory(&download_dir))
    return true;

  DownloadPathReservationTracker::GetReservedPath(
      download_item_,
      file_path_,
      download_dir,
      true,
      DownloadPathReservationTracker::UNIQUIFY,
      base::Bind(&CreateNewFileDone, file_selected_callback_));
  return true;
}

bool ChromeDuplicateDownloadInfoBarDelegate::Cancel() {
  if (!download_item_)
    return true;

  file_selected_callback_.Run(base::FilePath());
  // TODO(qinmin): rename this histogram enum.
  DownloadController::RecordDownloadCancelReason(
      DownloadController::CANCEL_REASON_OVERWRITE_INFOBAR_DISMISSED);
  return true;
}

std::string ChromeDuplicateDownloadInfoBarDelegate::GetFilePath() const {
  return file_path_.value();
}

int64_t ChromeDuplicateDownloadInfoBarDelegate::GetTotalBytes() const {
  return download_item_ ? download_item_->GetTotalBytes() : 0;
}

std::string ChromeDuplicateDownloadInfoBarDelegate::GetMimeType() const {
  return download_item_ ? download_item_->GetMimeType() : std::string();
}

bool ChromeDuplicateDownloadInfoBarDelegate::SetDirFullPath(
    const std::string& dir_full_path) {
  std::vector<base::FilePath::StringType> components;
  file_path_.GetComponents(&components);
  if (components.empty())
      return false;

  base::FilePath new_download_path(dir_full_path);
  file_path_ = new_download_path.Append(components.back());
  return true;
}

void ChromeDuplicateDownloadInfoBarDelegate::InfoBarDismissed() {
  Cancel();
}

bool ChromeDuplicateDownloadInfoBarDelegate::IsOffTheRecord() const {
  return is_off_the_record_;
}

}  // namespace android
}  // namespace chrome

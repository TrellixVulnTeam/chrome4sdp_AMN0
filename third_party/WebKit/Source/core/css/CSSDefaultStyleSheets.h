/*
 * Copyright (C) 1999 Lars Knoll (knoll@kde.org)
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 Apple Inc.
 * All rights reserved.
 * Copyright (C) 2013 Google Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 *
 */

#ifndef CSSDefaultStyleSheets_h
#define CSSDefaultStyleSheets_h

#include "core/CoreExport.h"
#include "platform/heap/Handle.h"
#include "wtf/Allocator.h"

namespace blink {

class Element;
class RuleSet;
class StyleSheetContents;

class CORE_EXPORT CSSDefaultStyleSheets
    : public GarbageCollected<CSSDefaultStyleSheets> {
  WTF_MAKE_NONCOPYABLE(CSSDefaultStyleSheets);

 public:
  static CSSDefaultStyleSheets& instance();

  bool ensureDefaultStyleSheetsForElement(const Element&);
  void ensureDefaultStyleSheetForFullscreen();

  RuleSet* defaultStyle() { return m_defaultStyle.get(); }
  RuleSet* defaultExtraStyle01() { return m_defaultExtraStyle01.get(); }
  RuleSet* defaultExtraStyle02() { return m_defaultExtraStyle02.get(); }
  RuleSet* defaultQuirksStyle() { return m_defaultQuirksStyle.get(); }
  RuleSet* defaultPrintStyle() { return m_defaultPrintStyle.get(); }
  RuleSet* defaultViewSourceStyle();

  StyleSheetContents* ensureMobileViewportStyleSheet();
  StyleSheetContents* ensureTelevisionViewportStyleSheet();
  StyleSheetContents* ensureXHTMLMobileProfileStyleSheet();

  StyleSheetContents* defaultStyleSheet() { return m_defaultStyleSheet.get(); }
  StyleSheetContents* defaultExtraStyleSheet01() {
    return m_defaultExtraStyleSheet01.get();
  }
  StyleSheetContents* defaultExtraStyleSheet02() {
    return m_defaultExtraStyleSheet02.get();
  }
  StyleSheetContents* quirksStyleSheet() { return m_quirksStyleSheet.get(); }
  StyleSheetContents* svgStyleSheet() { return m_svgStyleSheet.get(); }
  StyleSheetContents* mathmlStyleSheet() { return m_mathmlStyleSheet.get(); }
  StyleSheetContents* mediaControlsStyleSheet() {
    return m_mediaControlsStyleSheet.get();
  }
  StyleSheetContents* fullscreenStyleSheet() {
    return m_fullscreenStyleSheet.get();
  }

  void addDefaultExtraStyleSheet01(const String&);
  void addDefaultExtraStyleSheet02(const String&);

  DECLARE_TRACE();

 private:
  CSSDefaultStyleSheets();

  Member<RuleSet> m_defaultStyle;
  Member<RuleSet> m_defaultExtraStyle01;
  Member<RuleSet> m_defaultExtraStyle02;
  Member<RuleSet> m_defaultQuirksStyle;
  Member<RuleSet> m_defaultPrintStyle;
  Member<RuleSet> m_defaultViewSourceStyle;

  Member<StyleSheetContents> m_defaultStyleSheet;
  Member<StyleSheetContents> m_defaultExtraStyleSheet01;
  Member<StyleSheetContents> m_defaultExtraStyleSheet02;
  Member<StyleSheetContents> m_mobileViewportStyleSheet;
  Member<StyleSheetContents> m_televisionViewportStyleSheet;
  Member<StyleSheetContents> m_xhtmlMobileProfileStyleSheet;
  Member<StyleSheetContents> m_quirksStyleSheet;
  Member<StyleSheetContents> m_svgStyleSheet;
  Member<StyleSheetContents> m_mathmlStyleSheet;
  Member<StyleSheetContents> m_mediaControlsStyleSheet;
  Member<StyleSheetContents> m_fullscreenStyleSheet;
};

}  // namespace blink

#endif  // CSSDefaultStyleSheets_h

package com.quran.labs.androidquran.ui.helpers

import com.quran.data.model.selection.SelectionIndicator
import com.quran.labs.androidquran.common.LocalTranslation
import com.quran.labs.androidquran.common.QuranAyahInfo

interface AyahTracker {
  fun getToolBarPosition(sura: Int, ayah: Int): SelectionIndicator
  fun getQuranAyahInfo(sura: Int, ayah: Int): QuranAyahInfo?
  fun getLocalTranslations(): Array<LocalTranslation>?
}

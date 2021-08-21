package com.quran.labs.androidquran.data

import android.content.Context
import android.text.TextUtils

import com.quran.data.core.QuranInfo
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.util.QuranUtils

import timber.log.Timber
import javax.inject.Inject

class QuranDisplayData @Inject constructor(private val quranInfo: QuranInfo) {

  /**
   * Get localized sura name from resources
   *
   * @param context    Application context
   * @param sura       Sura number (1~114)
   * @param wantPrefix Whether or not to show prefix "Sura"
   * @return Compiled sura name without translations
   */
  fun getSuraName(context: Context, sura: Int, wantPrefix: Boolean): String {
    return getSuraName(context, sura, wantPrefix, false)
  }

  /**
   * Get localized sura name from resources
   *
   * @param context         Application context
   * @param sura            Sura number (1~114)
   * @param wantPrefix      Whether or not to show prefix "Sura"
   * @param wantTranslation Whether or not to show sura name translations
   * @return Compiled sura name based on provided arguments
   */
  fun getSuraName(
    context: Context, sura: Int, wantPrefix: Boolean, wantTranslation: Boolean
  ): String {
    if (sura < Constants.SURA_FIRST || sura > Constants.SURA_LAST) return ""

    val builder = StringBuilder()
    val suraNames = context.resources.getStringArray(R.array.sura_names)
    if (wantPrefix) {
      builder.append(context.getString(R.string.quran_sura_title, suraNames[sura - 1]))
    } else {
      builder.append(suraNames[sura - 1])
    }
    if (wantTranslation) {
      val translation = context.resources.getStringArray(R.array.sura_names_translation)[sura - 1]
      if (!TextUtils.isEmpty(translation)) {
        // Some sura names may not have translation
        builder.append(" (")
        builder.append(translation)
        builder.append(")")
      }
    }

    return builder.toString()
  }

  fun getSuraNameFromPage(context: Context, page: Int, wantTitle: Boolean): String {
    val sura = quranInfo.getSuraNumberFromPage(page)
    return if (sura > 0) getSuraName(context, sura, wantTitle, false) else ""
  }

  fun getPageSubtitle(context: Context, page: Int): String {
    val description = context.getString(R.string.page_description)
    return String.format(description,
      QuranUtils.getLocalizedNumber(context, page),
      QuranUtils.getLocalizedNumber(context, quranInfo.getJuzForDisplayFromPage(page)))
  }

  fun getJuzDisplayStringForPage(context: Context, page: Int): String {
    val description = context.getString(R.string.juz2_description)
    return String.format(description,
      QuranUtils.getLocalizedNumber(context, quranInfo.getJuzForDisplayFromPage(page)))
  }

  fun getSuraAyahString(context: Context, sura: Int, ayah: Int): String {
    val suraName = getSuraName(context, sura, wantPrefix = false, wantTranslation = false)
    return context.getString(R.string.sura_ayah_notification_str, suraName, ayah)
  }

  fun getNotificationTitle(
    context: Context, minVerse: SuraAyah, maxVerse: SuraAyah, isGapless: Boolean
  ): String {
    val minSura = minVerse.sura
    var maxSura = maxVerse.sura

    val notificationTitle = getSuraName(context, minSura, wantPrefix = true, wantTranslation = false)
    if (isGapless) {
      // for gapless, don't show the ayah numbers since we're
      // downloading the entire sura(s).
      return if (minSura == maxSura) {
        notificationTitle
      } else {
        "$notificationTitle - " + getSuraName(
          context, maxSura, wantPrefix = true, wantTranslation = false
        )
      }
    }

    var maxAyah = maxVerse.ayah
    if (maxAyah == 0) {
      maxSura--
      maxAyah = quranInfo.getNumberOfAyahs(maxSura)
    }

    return notificationTitle.plus(
      if (minSura == maxSura) {
        if (minVerse.ayah == maxAyah) {
          " ($maxAyah)"
        } else {
          " (" + minVerse.ayah + "-" + maxAyah + ")"
        }
      } else {
        " (" + minVerse.ayah + ") - " +
            getSuraName(context, maxSura, wantPrefix = true, wantTranslation = false) +
            " (" + maxAyah + ")"
      }
    )
  }

  fun getSuraListMetaString(context: Context, sura: Int): String {
    val info = context.getString(if (quranInfo.isMakki(sura)) R.string.makki else R.string.madani) + " - "

    val ayahs = quranInfo.getNumberOfAyahs(sura)
    return info + context.resources.getQuantityString(
      R.plurals.verses, ayahs,
      QuranUtils.getLocalizedNumber(context, ayahs))
  }

  fun safelyGetSuraOnPage(page: Int): Int {
    return if (page < Constants.PAGES_FIRST || page > quranInfo.numberOfPages) {
      Timber.e(IllegalArgumentException("safelyGetSuraOnPage with page: $page"))
      quranInfo.getSuraOnPage(1)
    } else {
      quranInfo.getSuraOnPage(page)
    }
  }

  private fun getSuraNameFromPage(context: Context, page: Int): String {
    val suraNumber = quranInfo.getSuraNumberFromPage(page)
    return getSuraName(context, suraNumber, wantPrefix = false, wantTranslation = false)
  }

  fun getAyahString(sura: Int, ayah: Int, context: Context): String {
    return getSuraName(context, sura, true) + " - " + context.getString(R.string.quran_ayah, ayah)
  }

  fun getAyahMetadata(sura: Int, ayah: Int, page: Int, context: Context): String {
    val juz = quranInfo.getJuzForDisplayFromPage(page)
    return context.getString(R.string.quran_ayah_details,
      getSuraName(context, sura, true),
      QuranUtils.getLocalizedNumber(context, ayah),
      QuranUtils.getLocalizedNumber(context, quranInfo.getJuzFromSuraAyah(sura, ayah, juz)))
  }

  // do not remove the nullable return type
  fun getSuraNameString(context: Context, page: Int): String? {
    return context.getString(R.string.quran_sura_title, getSuraNameFromPage(context, page))
  }

  fun getAyahKeysOnPage(page: Int, lowerBound: SuraAyah?, upperBound: SuraAyah?): Set<String> {
    val ayahKeys: MutableSet<String> = LinkedHashSet()
    val bounds = quranInfo.getPageBounds(page)
    var start = SuraAyah(bounds[0], bounds[1])
    var end = SuraAyah(bounds[2], bounds[3])
    if (lowerBound != null) {
      start = SuraAyah.max(start, lowerBound)
    }
    if (upperBound != null) {
      end = SuraAyah.min(end, upperBound)
    }
    val iterator = SuraAyahIterator(quranInfo, start, end)
    while (iterator.next()) {
      ayahKeys.add(iterator.sura.toString() + ":" + iterator.ayah.toString())
    }
    return ayahKeys
  }
}

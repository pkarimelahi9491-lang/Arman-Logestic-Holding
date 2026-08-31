package com.example.data.local

import com.example.data.local.dao.FleetDao
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.RouteEntity
import com.example.data.local.entity.RoutePriceHistoryEntity
import com.example.util.PersianDateHelper

object CsvFleetData {

    const val DEFAULT_PRIMARY_ORIGIN = "انبار مرکزی انتخاب (مورچه خورت)"

    // 15 Distance Tiers with their specific destinations extracted from holding CSV
    data class CsvTier(
        val code: Int,
        val distanceKm: Int,
        val ratePerKmRial: Long,
        val totalPriceRial: Long,
        val destinations: List<String>
    ) {
        val totalPriceToman: Long get() = totalPriceRial / 10
    }

    val INITIAL_CSV_RAW = """
کد,مسافت,نرخ,جمع,تعداد پته,جمع کل,مقصد,مقصد2,مقصد3,مقصد4,مقصد5,مقصد6,مقصد7,مقصد8,مقصد9,مقصد10,مقصد11,مقصد12,مقصد13,مقصد14,مقصد15,مقصد16,مقصد17,مقصد18,مقصد19,مقصد20,مقصد21,مقصد22,مقصد23,مقصد24,مقصد25,مقصد26,مقصد27,مقصد28,مقصد29,مقصد30,مقصد31,مقصد32,مقصد33,مقصد34,مقصد35,مقصد36,مقصد37,مقصد38,مقصد39,مقصد40,مقصد41,مقصد42,مقصد43,مقصد44,مقصد45,مقصد46,مقصد47,مقصد48,مقصد49,مقصد50,مقصد51,مقصد52,مقصد53,مقصد54,مقصد55,مقصد56,مقصد57,مقصد58,مقصد59,مقصد60,مقصد61,مقصد62,مقصد63,مقصد64,مقصد65
1,30,"90,000","2,700,000",1,"2,700,000",انبار سادات,اقامتگاه مادر شاه,بتن تعادل,پلیس راه شاهین شهر,شاهین شهر,شهرک صنعتی بزرگ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
2,35,"90,000","3,150,000",,0,حاجی آباد شاهین شهر,گلدیس,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
3,40,"90,000","3,600,000",,0,آزادگان,خورزوق,شهرک سیمرغ,علویجه,گرگاب,گز,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
4,45,"90,000","4,050,000",4,"16,200,000",17شهریور,ابوریحان,آفرینش,آل محمد,آل یاسین,امام خمینی,امیرکبیر,پل چمران,پنج آذر,ترمینال کاوه,خانه اصفهان,خردمند,دستگرد,دولت آباد,رزمندگان,شاهپور جدید,شریف,غرضی,فدک,کاوه,گلستان,شهرک صنعتی محمود اباد,مارچین,مشیرالدوله,ملک شهر,صفائیه,نیروگاه,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
5,50,"90,000","4,500,000",,0,ابن سینا,اشراق,باهنر,برازنده,بعثت,بید ابادی,پارک لاله,پنج رمضان,جابر انصاری,چمران,حکیم شفائی اول,خرازی,دانش,دروازه تهران,رباط,رهنان,زاهد,سروش,شاهپور قدیم,شاهد,شهدا,شهرک صنعتی خمینی شهر,شهرک صنعتی دولت آباد,شهرک نگین,طالقانی,عسگریه,فردوسی,فروغی,فلاطوری,قدس,کاوه,مسجد سید,مولوی,میدان امام علی,نگارستان,هسا,وفایی,,,,,,,,,,,,,,,,,,,,,,,,,,,,
6,55,"90,000","4,950,000",7,"34,650,000",22بهمن,ابشار,آپادانا,آذر بهرام,احمد اباد,ارباب,استانداری,بزرگمهر,بوستان سعدی,بهشتی,پروین,پل تمدن,پل غدیر,پل فلزی,پل مارنون,پل میر,جهاد,چهارباغ پایین,چهارباغ عباسی,چهارراه تختی,چهارراه قصر,حبیب آباد,حکیم,حکیم شفایی دوم,خزانه,خمینی شهر,خواجه عمید,خواجو,دانشگاه صنعتی,دشتستان,دروازه دولت,رکن الدوله,زینبیه,سجاد,شریف واقفی,شمس ابادی,شهرک سلامت,شهرک ولی عصر,شیخ بهایی,شیخ صدوق,شیخ طوسی,شیخ مفید,صمدیه,علامه امینی,میدان احمد اباد,فیض,کاشانی,گلزار,لاله,لاهور,لنبان,محتشم کاشانی,مدرس,مشتاق,معراج,مهراباد,میدان امام,میدان امام حسین,میر,میرداماد,میمه,نشاط,هاتف,ولی عصر,هشت بهشت
7,60,"90,000","5,400,000",3,"16,200,000",آتشگاه,ارتش,اطشاران,امیر حمزه,انقلاب,باغ دریاچه,بلوار آیینه خانه,بلوار ملت,بیمارستان میلاد,پل وحید,تالار,ترمینال جی,ترمینال صفه,توحید,جی,جی شیر,چهارباغ بالا,حسین آباد,حکیم نظامی,حمزه,خاقانی,دانشگاه اصفهان,دانشگاه هنر,دروازه شیراز,رودکی,سروستان,سیمین,سهروردی,شریعتی,فرایبورگ,فرح اباد,قائمیه,کشاورز,کوی امیریه,لباف,مرداویج,مهاجر,ناژوان,نظر,هزار جریب,وحید,,,,,,,,,,,,,,,,,,,,,,,,
8,65,"90,000","5,850,000",,0,ارغوانیه,اریسون,خوراسگان,دکتر حسابی,شفق,دهق,کشوری,کوی امام,کهندژ,صفه,میرزا طاهر,نبوی منش,کهریزسنگ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
9,70,"90,000","6,300,000",,0,شهرک صنعتی جی,شهرک صنعتی کمشچه,فرودگاه,کمشچه,گمرک,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
10,75,"90,000","6,750,000",,0,باغ رضوان,درچه,سپاهان شهر,فلاورجان,کلیشاد,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
11,80,"90,000","7,200,000",2,"14,400,000",اشکاوند,بهاران,جوزدان,قهجاورستان,شهرک صنعتی نجف‌آباد,ویلا شهر,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
12,85,"90,000","7,650,000",,0,بهارستان,تیران,شهر ابریشم,شهرک صنعتی سروش بادران,نجف آباد,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
13,90,"90,000","8,100,000",,0,نطنز,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
14,95,"90,000","8,550,000",,0,شهرک صنعتی مبارکه,فولاد شهر,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
15,100,"90,000","9,000,000",,0,چادگان,زاینده رود,زرین شهر,سجزی,شهرضا,شهرک صنعتی سجزی,شهرک صنعتی کوهپایه,مبارکه,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    """.trimIndent()

    /**
     * Parses a CSV or Excel table text into structured CsvTier objects.
     */
    fun parseCsvTiers(rawText: String): List<CsvTier> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<CsvTier>()

        for (line in lines) {
            // Skip header lines
            if (line.startsWith("کد") || line.startsWith("Code") || line.startsWith("code")) continue

            val tokens = splitCsvLine(line)
            if (tokens.size < 7) continue

            val code = tokens.getOrNull(0)?.toIntOrNull() ?: continue
            val distanceKm = tokens.getOrNull(1)?.toIntOrNull() ?: (code * 5 + 25)
            val rateRaw = cleanNumber(tokens.getOrNull(2) ?: "90000")
            val rate = rateRaw.toLongOrNull() ?: 90000L
            val totalRaw = cleanNumber(tokens.getOrNull(3) ?: "")
            val totalRial = totalRaw.toLongOrNull() ?: (distanceKm * rate)

            // Destinations start from index 6
            val destinations = mutableListOf<String>()
            for (i in 6 until tokens.size) {
                val dest = tokens[i].trim().trim('"', '\'', '،')
                if (dest.isNotBlank() && dest != "0" && dest != "-" && dest != "null") {
                    destinations.add(dest)
                }
            }

            if (destinations.isNotEmpty()) {
                result.add(
                    CsvTier(
                        code = code,
                        distanceKm = distanceKm,
                        ratePerKmRial = rate,
                        totalPriceRial = totalRial,
                        destinations = destinations.distinct()
                    )
                )
            }
        }
        return result
    }

    private fun cleanNumber(str: String): String {
        return str.replace(",", "").replace("،", "").replace("\"", "").replace("'", "").trim()
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                (ch == ',' || ch == '\t' || ch == ';') && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    /**
     * Seeds or synchronizes all locations and routes from CSV into Room database.
     */
    suspend fun syncWithDatabase(
        dao: FleetDao,
        csvText: String = INITIAL_CSV_RAW,
        primaryOriginName: String = DEFAULT_PRIMARY_ORIGIN,
        operatorName: String = "مدیریت سیستم / اکسل تعرفه"
    ): Int {
        val tiers = parseCsvTiers(csvText)
        if (tiers.isEmpty()) return 0

        val todayStr = PersianDateHelper.getTodayJalali().formatStandard()

        // 1. Ensure Hub / Central Origins
        val originLocations = listOf(
            LocationEntity(id = 1, name = primaryOriginName, city = "مورچه خورت / اصفهان", isActive = true),
            LocationEntity(id = 2, name = "کارخانه لوازم خانگی انتخاب (اسنوا)", city = "مورچه خورت", isActive = true),
            LocationEntity(id = 3, name = "دفتر مرکزی اصفهان", city = "اصفهان", isActive = true),
            LocationEntity(id = 4, name = "انبار مرکزی تهران (ملاصدرا)", city = "تهران", isActive = true)
        )
        dao.insertLocations(originLocations)

        // 2. Gather all unique destinations and insert into Locations
        val allDestNames = tiers.flatMap { it.destinations }.distinct()
        val locationEntities = mutableListOf<LocationEntity>()
        var locIdCounter = 100L

        for (dest in allDestNames) {
            val city = guessCityForLocation(dest)
            locationEntities.add(
                LocationEntity(
                    id = locIdCounter++,
                    name = dest,
                    city = city,
                    isActive = true
                )
            )
        }
        dao.insertLocations(locationEntities)

        // 3. Generate Route Entities from primary origin to each destination
        val routeEntities = mutableListOf<RouteEntity>()
        val priceHistories = mutableListOf<RoutePriceHistoryEntity>()

        var totalRouteCount = 0

        for (tier in tiers) {
            val priceToman = tier.totalPriceToman

            tier.destinations.forEachIndexed { subIndex, destName ->
                val destLocation = locationEntities.find { it.name == destName }
                val destId = destLocation?.id ?: (2000L + totalRouteCount)

                // Route Code Format: AR-TierCode-SubIndex (e.g., AR-01-01, AR-04-12, AR-15-05)
                val routeCode = String.format("AR-%02d-%02d", tier.code, subIndex + 1)
                val routeId = "rt-tier${tier.code}-${subIndex + 1}"

                val description = "مسافت مصوب: ${tier.distanceKm} کیلومتر | نرخ هر کیلومتر: ${tier.ratePerKmRial / 10} تومان"

                val route = RouteEntity(
                    id = routeId,
                    routeCode = routeCode,
                    originId = 1L,
                    originName = primaryOriginName,
                    destinationId = destId,
                    destinationName = destName,
                    currentPrice = priceToman,
                    currency = "TOMAN",
                    isActive = true,
                    description = description,
                    updatedAtJalali = todayStr
                )
                routeEntities.add(route)

                priceHistories.add(
                    RoutePriceHistoryEntity(
                        routeId = routeId,
                        routeCode = routeCode,
                        oldPrice = 0,
                        newPrice = priceToman,
                        changedBy = operatorName,
                        effectiveDateJalali = todayStr
                    )
                )

                totalRouteCount++
            }
        }

        // Insert all routes & histories
        dao.insertRoutes(routeEntities)
        priceHistories.forEach { dao.insertPriceHistory(it) }

        return totalRouteCount
    }

    fun guessCityForLocation(name: String): String {
        return when {
            name.contains("شاهین شهر") || name.contains("گلدیس") || name.contains("مادر شاه") -> "شاهین شهر"
            name.contains("خورزوق") -> "خورزوق"
            name.contains("گرگاب") -> "گرگاب"
            name.contains("گز") -> "گز برخوار"
            name.contains("علویجه") -> "علویجه"
            name.contains("خمینی شهر") -> "خمینی شهر"
            name.contains("نجف آباد") || name.contains("ویلا شهر") -> "نجف‌آباد"
            name.contains("فلاورجان") -> "فلاورجان"
            name.contains("کلیشاد") -> "کلیشاد"
            name.contains("درچه") -> "درچه"
            name.contains("بهارستان") -> "بهارستان"
            name.contains("مبارکه") -> "مبارکه"
            name.contains("فولاد شهر") -> "فولادشهر"
            name.contains("زرین شهر") -> "زرین‌شهر"
            name.contains("نطنز") -> "نطنز"
            name.contains("میمه") -> "میمه"
            name.contains("چادگان") -> "چادگان"
            name.contains("شهرضا") -> "شهرضا"
            name.contains("سجزی") -> "سجزی"
            name.contains("کوهپایه") -> "کوهپایه"
            name.contains("تیران") -> "تیران و کرون"
            name.contains("کمشچه") -> "کمشچه"
            name.contains("قهجاورستان") -> "قهجاورستان"
            else -> "اصفهان"
        }
    }
}

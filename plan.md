# خطة تطبيق "موادي" - My Subjects

> تطبيق To-Do خفيف مخصص لتنظيم المواد الدراسية - Kotlin Native + Jetpack Compose بتصميم iOS

---

## 1. نظرة عامة

**الهدف:** تطبيق أندرويد خفيف يسمح للطالب بتنظيم مواده الدراسية هرميا: `مادة -> وحدات (قوائم منسدلة) -> دروس` مع إمكانية إضافة دروس مباشرة داخل المادة بدون وحدة. كل درس له 3 حالات ويحسب التقدم تلقائيا.

**المنصة:** Android Native - Kotlin
**التصميم:** iOS UI بالكامل (Cupertino) - ألوان هادية جدا - خط IBM Plex Sans Arabic
**اللغة:** عربي فقط
**التخزين:** Offline فقط (Room) + نسخ احتياطي JSON

---

## 2. القرارات المعتمدة من المستخدم

| # | الموضوع | القرار النهائي |
|---|---------|---------------|
| 1 | شكل الرئيسية | **List** (قائمة عمودية) وليس Grid |
| 2 | محتوى كارت المادة | **اسم المادة + عدد الدروس المتبقية** |
| 3 | زر الإضافة | **FAB عائم** (Floating Action Button) |
| 4 | تثبيت المادة Pin | **نعم** - المادة المثبتة تثبت فوق |
| 5 | مصير المادة المكتملة | **تفضل في القائمة** (لا أرشيف) |
| 6-10 | صفحة المادة | صفحة عادية - الوحدات `Accordion` (قائمة منسدلة) - ترتيب حسب الإضافة - زر توسيع/طي الكل |
| 11-13 | حقول الإضافة | **اسم فقط** للمادة/الوحدة/الدرس (خفيف) |
| 14 | إضافة سريعة Bulk | **نعم** - صياغة: `*` بداية وحدة، `-` نهاية وحدة في سطر لوحده |
| 15 | تعديل/حذف | **ضغط طويل** يظهر Action Sheet iOS |
| 16 | حالات الدرس | **3 حالات:** لم يبدأ (☐ رمادي) / تم (☑ أخضر) / محتاج إعادة (🔄 برتقالي) |
| 17 | Animation | **لا** |
| 18-20 | حقول إضافية (أولوية/نوت/بومودورو) | **لا** - يبقى خفيف |
| 21 | بحث عام | **لا** |
| 22 | فلتر داخل المادة | **نعم** [الكل \| المتبقي \| المكتمل \| محتاج إعادة] |
| 23 | ترتيب | **حسب الإضافة فقط** |
| 24 | شريط التقدم | **دائرة + رقم %** |
| 25 | إحصائيات | **نعم** صفحة منفصلة |
| 26 | تقويم | **لا** |
| 27 | ألوان | **هادية جدا** - خلفية iOS #F2F2F7 - كروت بيضاء |
| 28 | Dark Mode | **لا حاليا** |
| 29 | اللغة | عربي فقط |
| 30 | الخط | **IBM Plex Sans Arabic** |
| 31 | Backup | **نعم** - زر أيقونة فقط (مربع بسهم iOS) |
| 32 | صياغة Bulk | `*` للبداية و `-` للنهاية |
| 34 | تغيير حالة الدرس | ضغطة واحدة تلف بين 3 حالات (قرار ذوقي) |
| 36 | ترتيب المثبت | المثبت فوق مرتب حسب تاريخ التثبيت (قرار ذوقي) |

---

## 3. الهيكل الهرمي وتدفق التنقل

```
[ الرئيسية: قائمة المواد (List) ]
    │
    ├─ FAB (+) -> Dialog إضافة مادة (اسم فقط)
    ├─ أيقونة Backup (أعلى) -> تصدير/استيراد JSON
    ├─ ضغط على كارت مادة -> [ صفحة المادة ]
    └─ ضغط طويل على مادة -> Action Sheet (تعديل/حذف/تثبيت 📌)

[ صفحة المادة: {اسم المادة} ]
    │
    ├─ TopBar iOS + زر رجوع + زر (+) إضافة + زر Bulk + فلتر
    ├─ ليستة مختلطة بترتيب الإضافة:
    │    ├─ درس مباشر (بدون وحدة) -> ☐/☑/🔄
    │    ├─ ▼ وحدة 1 [دائرة % + 2/5] (مفتوحة - Accordion)
    │    │     ├─ ☐ درس 1
    │    │     └─ 🔄 درس 2
    │    ├─ ▶ وحدة 2 [دائرة %] (مغلقة)
    │    └─ درس مباشر آخر
    ├─ ضغط على درس -> يلف الحالة (☐ -> ☑ -> 🔄 -> ☐)
    ├─ ضغط طويل على وحدة/درس -> Action Sheet (تعديل/حذف)
    └─ Empty State: "لا يوجد دروس، أضف درس أو وحدة"

[ صفحة الإحصائيات ]
    │
    ├─ كارت إجمالي: إجمالي الدروس / مكتمل / متبقي / نسبة عامة (دائرة كبيرة)
    └─ ليستة المواد: كل مادة + دائرتها + عدد متبقي
```

---

## 4. التقنيات (Tech Stack)

| المكون | التقنية | السبب |
|--------|---------|-------|
| اللغة | **Kotlin 1.9+** | Native |
| UI | **Jetpack Compose + Material3** مخصص ليحاكي **Cupertino iOS** | أحدث وأسرع من XML |
| Navigation | **Navigation Compose 2.7+** | تنقل بين الشاشات |
| Database | **Room 2.6+** | تخزين محلي Offline |
| State | **ViewModel + StateFlow + Compose State** | MVVM |
| DI | **Hilt (اختياري للمرحلة 2)** | تنظيم |
| Font | **IBM Plex Sans Arabic** (Google Fonts) | قرار المستخدم |
| Backup | **Gson + Storage Access Framework (SAF)** | تصدير JSON ومشاركته |
| Min SDK | 24 (Android 7) | تغطية واسعة |
| Target SDK | 34 | |

**تصميم iOS على Compose:**
- خلفية: `#F2F2F7` (iOS grouped background)
- كروت: `#FFFFFF` مع `RoundedCornerShape(12.dp)` + ظل خفيف
- List: `Inset Grouped List` (مثل إعدادات iOS)
- TopBar: Large Title (عنوان كبير يصغر عند السكرول)
- أزرار: `CupertinoButton` مخصص (أزرق iOS #007AFF)
- Action Sheet: `ModalBottomSheet` يحاكي iOS

---

## 5. نموذج البيانات (Data Model)

### 5.1 الجداول (Room Entities)

```kotlin
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // اسم فقط
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null, // لتـرتيب المثبت
    val order: Int, // ترتيب الإضافة
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "units",
    foreignKeys = [ForeignKey(entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = CASCADE)],
    indices = [Index("subjectId")]
)
data class Unit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val order: Int,
    val isExpanded: Boolean = true, // حالة الفتح/الإغلاق Accordion
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = CASCADE),
        ForeignKey(entity = Unit::class, parentColumns = ["id"], childColumns = ["unitId"], onDelete = CASCADE)
    ],
    indices = [Index("subjectId"), Index("unitId")]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val unitId: Long? = null, // null = درس مباشر في المادة
    val title: String,
    val status: LessonStatus = LessonStatus.TODO,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LessonStatus { TODO, DONE, NEEDS_REVIEW }
```

**ملاحظة هامة:** `unitId = null` يحقق متطلب "درس بدون تفرع".

### 5.2 حساب التقدم

```kotlin
// لكل وحدة:
unitProgress = lessonsInUnit.count { it.status == DONE } / lessonsInUnit.size

// لكل مادة:
allLessonsInSubject = directLessons + lessonsInAllUnits
subjectProgress = allLessonsInSubject.count { it.status == DONE } / allLessonsInSubject.size
remainingCount = allLessonsInSubject.count { it.status != DONE }
```

### 5.3 مثال بيانات

```
Subject(id=1, name="اللغة العربية", isPinned=true, order=0)
  Lesson(id=1, subjectId=1, unitId=null, title="درس الفاعل", status=TODO, order=0)
  Lesson(id=2, subjectId=1, unitId=null, title="درس المفعول", status=DONE, order=1)
  Unit(id=1, subjectId=1, name="الاسم", order=2, isExpanded=true)
    Lesson(id=3, subjectId=1, unitId=1, title="مبتدا", status=TODO, order=0)
    Lesson(id=4, subjectId=1, unitId=1, title="خبر", status=NEEDS_REVIEW, order=1)
  Lesson(id=5, subjectId=1, unitId=null, title="ظن واخواتها", status=TODO, order=3)
```

---

## 6. تفصيل الشاشات والمكونات

### 6.1 الشاشة الرئيسية - SubjectsScreen

**الـ Layout:**
- `Scaffold` بخلفية `#F2F2F7`
- `TopAppBar` iOS: عنوان "موادي" Large Title + زر أيقونة Backup (square.and.arrow.up) يمين + زر إحصائيات يسار
- `LazyColumn` (List):
  - كل عنصر: `Card` أبيض `RoundedCornerShape(12.dp)` padding 16.dp
  - داخل الكارت: `Row` -> `Column` (اسم المادة + نص ثانوي "متبقي 5 دروس") + `Spacer` + `CircularProgressIndicator` صغير (دائرة) + `Text` نسبة % + أيقونة Pin لو مثبت
  - ضغط عادي: `navController.navigate("subject/{id}")`
  - ضغط طويل: `ModalBottomSheet` iOS Action Sheet (تعديل الاسم / حذف مع تأكيد / تثبيت-إلغاء تثبيت)
- `FloatingActionButton` (أزرق iOS #007AFF، أيقونة + بيضاء) أسفل يمين -> Dialog إضافة مادة (TextField واحد + زر حفظ/إلغاء)
- Empty State: أيقونة كتاب + نص "ابدأ بإضافة مادتك الأولى" + زر

**المنطق:**
- ترتيب: `subjects.sortedWith(compareByDescending<Subject> { it.isPinned }.thenBy { it.pinnedAt }.thenBy { it.order })`
- حذف مادة يحذف وحداتها ودروسها (CASCADE)

### 6.2 صفحة المادة - SubjectDetailScreen

**الـ Layout:**
- `TopAppBar` iOS: عنوان اسم المادة + زر رجوع + زر (+) + زر Bulk (أيقونة قائمة) + `FilterChip` group
- `LazyColumn` مختلطة:
  - نوعين ViewType: `ITEM_LESSON_DIRECT` و `ITEM_UNIT`
  - `UnitItem`: كارت وحدة -> `Row` (سهم ▼/▶ + اسم الوحدة + دائرة صغيرة + "2/5" + زر +) + `AnimatedVisibility` للدروس الداخلية (Expandable)
  - `LessonItem` (مباشر أو داخل وحدة): `Row` -> أيقونة حالة (دائرة فارغة/صح خضراء/برتقالي) + عنوان الدرس + `Spacer` (ضغط يغير الحالة)
- `FilterBar`: `SegmentedControl` iOS style [الكل | المتبقي | المكتمل | إعادة] -> يفلتر الـ LazyColumn
- Dialogs: إضافة وحدة (اسم فقط)، إضافة درس (عنوان فقط)، تعديل (نفس الحقول)، تأكيد حذف
- Bulk Dialog: `TextField` كبير متعدد الأسطر + زر "إضافة" + معاينة

**المنطق:**
- جلب `units` و `lessons` للـ `subjectId` مرتبة حسب `order`
- تجميع للعرض: `displayList = buildList { add directLessons; add unitsWithLessons }` مرتبة حسب `order` المختلط (نحتاج حقل `order` موحد للمادتين أو نحسبه)
  - **حل مقترح:** جدول `SubjectContentOrder` أو ببساطة نعتمد أن ترتيب العرض هو: الدروس المباشرة والوحدات مختلطة حسب `createdAt`/`order` العام. أبسط: نضيف `displayOrder` في كل من Unit و Lesson ونرتبهم معا عند العرض (نحتاج دمج القائمتين وترتيبها).
  - **تبسيط MVP:** نعرض أولا كل الدروس المباشرة ثم الوحدات (أسهل). أو نستخدم `order` واحد يزيد لكل إضافة سواء وحدة أو درس مباشر (نخزن `nextOrder` في Subject).
- تغيير حالة الدرس: `onLessonClick { status = when(status){TODO->DONE, DONE->NEEDS_REVIEW, NEEDS_REVIEW->TODO} }`
- طي/فتح وحدة: `unit.copy(isExpanded = !isExpanded)` + update DB

### 6.3 شاشة الإحصائيات - StatsScreen

- كارت إجمالي كبير: دائرة كبيرة في المنتصف + نص "12/30 درس مكتمل - 40%"
- `LazyColumn` للمواد: كل صف -> اسم المادة + دائرة صغيرة + نسبة + متبقي

### 6.4 مكونات مشتركة (iOS Style)

- `IosCard`, `IosListItem`, `IosCircularProgress`, `IosActionSheet`, `IosSegmentedControl`, `IosFAB`

---

## 7. ميزة الإضافة السريعة (Bulk Add) - مواصفة نهائية

**الصياغة المعتمدة:** `*` بداية وحدة، `-` نهاية وحدة في سطر لوحده.

**القواعد:**
1.  كل سطر يعتبر عنصر واحد (نقطة `.` في مثال المستخدم كانت تمثل سطر جديد)
2.  السطر الذي يبدأ بـ `*` (بعد trim) -> اسم وحدة جديدة (يزيل `*` ويأخذ الباقي)
3.  السطر الذي يساوي `-` تماما (بعد trim) -> يغلق الوحدة الحالية
4.  أي سطر آخر:
    - لو هناك وحدة مفتوحة حاليا -> يضاف كدرس داخلها
    - لو لا يوجد وحدة مفتوحة -> يضاف كدرس مباشر في المادة
5.  سطور فارغة تهمل
6.  لو انتهى النص ووحدة لا تزال مفتوحة -> تغلق تلقائيا
7.  أسماء فارغة تهمل

**Parser (Kotlin Pseudo):**

```kotlin
fun parseBulk(input: String, subjectId: Long, startOrder: Int): ParsedResult {
    var currentUnit: Unit? = null
    var order = startOrder
    val unitsToInsert = mutableListOf<Unit>()
    val lessonsToInsert = mutableListOf<Lesson>()
    val lines = input.split("\n", ".") // ندعم \n و . كفاصل للمرونة
        .map { it.trim() }.filter { it.isNotEmpty() }
    for (line in lines) {
        when {
            line.startsWith("*") -> {
                val name = line.removePrefix("*").trim()
                if (name.isNotEmpty()) {
                    currentUnit = Unit(subjectId=subjectId, name=name, order=order++)
                    unitsToInsert.add(currentUnit)
                }
            }
            line == "-" -> currentUnit = null
            else -> {
                lessonsToInsert.add(Lesson(subjectId=subjectId, unitId=currentUnit?.id, title=line, order=...))
            }
        }
    }
    return ParsedResult(unitsToInsert, lessonsToInsert)
}
```

**ملاحظة تنفيذية:** لأن `Unit.id` يتولد بعد الإدخال (autoGenerate)، نحتاج إدخال الوحدات أولا ثم ربط الدروس بها عبر خريطة مؤقتة (tempId).

**مثال المستخدم محلل:**

```
Input:
درس الفاعل
درس المفعول
*الاسم
مبتدا
خبر
-
ظن واخواتها

Result:
- Lesson "درس الفاعل" (direct)
- Lesson "درس المفعول" (direct)
- Unit "الاسم"
  - Lesson "مبتدا"
  - Lesson "خبر"
- Lesson "ظن واخواتها" (direct)
```

**UI للـ Bulk:**
- Dialog كبير `TextField` متعدد الأسطر hint يوضح الصياغة + مثال مصغر
- زر "معاينة" يظهر عدد الوحدات/الدروس التي سيتم إنشاؤها قبل التأكيد

---

## 8. النسخ الاحتياطي (Backup)

**التصميم:** زر أيقونة فقط في TopBar الرئيسية (أيقونة iOS `square.and.arrow.up`)

**عند الضغط:**
- `Action Sheet`: [تصدير نسخة | استيراد نسخة]

**تصدير:**
- تجميع كل البيانات: `BackupData(subjects, units, lessons, exportedAt)`
- تحويل لـ JSON عبر `Gson` (pretty)
- فتح `ActivityResultContracts.CreateDocument("application/json")` -> يختار مكان الحفظ
- أو فتح `Share Intent` لمشاركة الملف على واتساب/درايف

**استيراد:**
- فتح `ActivityResultContracts.OpenDocument()`
- قراءة JSON -> `Gson.fromJson`
- **سياسة الدمج:** حذف كل البيانات الحالية واستبدالها (مع تأكيد Dialog "سيتم استبدال كل بياناتك الحالية") - أبسط وآمن لـ MVP
- إعادة إدخال مع الحفاظ على IDs أو توليد جديد

**ملف JSON مثال:**

```json
{
  "exportedAt": 1717000000000,
  "subjects": [{"id":1,"name":"اللغة العربية","isPinned":true,"order":0}],
  "units": [{"id":1,"subjectId":1,"name":"الاسم","order":2}],
  "lessons": [{"id":1,"subjectId":1,"unitId":null,"title":"درس الفاعل","status":"TODO","order":0}]
}
```

---

## 9. هيكلة المشروع (Project Structure)

```
app/
 ├─ src/main/
 │   ├─ java/com/mymaterials/app/
 │   │   ├─ MainActivity.kt
 │   │   ├─ MyMaterialsApp.kt (Application + Hilt)
 │   │   ├─ data/
 │   │   │   ├─ db/AppDatabase.kt
 │   │   │   ├─ dao/SubjectDao.kt, UnitDao.kt, LessonDao.kt
 │   │   │   ├─ entity/Subject.kt, Unit.kt, Lesson.kt
 │   │   │   └─ repository/MaterialsRepository.kt
 │   │   ├─ ui/
 │   │   │   ├─ theme/Theme.kt, Color.kt, Type.kt (IBM Plex + iOS colors)
 │   │   │   ├─ components/IosCard.kt, IosProgress.kt, IosActionSheet.kt
 │   │   │   ├─ screens/subjects/SubjectsScreen.kt, SubjectsViewModel.kt
 │   │   │   ├─ screens/subjectdetail/SubjectDetailScreen.kt, DetailViewModel.kt
 │   │   │   ├─ screens/stats/StatsScreen.kt
 │   │   │   └─ navigation/NavGraph.kt
 │   │   ├─ util/BulkParser.kt, BackupManager.kt
 │   │   └─ res/font/ibm_plex_sans_arabic_*.ttf
 │   └─ AndroidManifest.xml
 └─ build.gradle.kts
```

---

## 10. خطة التنفيذ - 5 مراحل

### المرحلة 1: التأسيس (Setup) - يوم 1
- [ ] إنشاء مشروع Android Studio (Empty Activity + Compose)
- [ ] إعداد Gradle: Room, Navigation Compose, Hilt, Gson
- [ ] إضافة خط IBM Plex Sans Arabic وإعداد Typography
- [ ] إعداد Theme (ألوان iOS #F2F2F7, #FFFFFF, #007AFF)
- [ ] إنشاء Entities + DAOs + AppDatabase + Repository
- [ ] إعداد NavGraph (3 routes: subjects, subject/{id}, stats)

### المرحلة 2: الشاشة الرئيسية (Subjects) - يوم 1-2
- [ ] SubjectsViewModel (CRUD + Pin logic)
- [ ] SubjectsScreen UI (iOS List + Card + دائرة %)
- [ ] FAB + Dialog إضافة/تعديل مادة
- [ ] Action Sheet (ضغط طويل) + حذف بتأكيد
- [ ] Empty State + ترتيب Pin

### المرحلة 3: صفحة المادة (Core) - يوم 2-3
- [ ] DetailViewModel (جلب وحدات+دروس + فلتر + تقدم)
- [ ] Unit Accordion Component (AnimatedVisibility + سهم)
- [ ] Lesson Row (3 حالات + ضغطة تلف)
- [ ] إضافة وحدة/درس منفرد + تعديل/حذف
- [ ] Filter Chips + حساب التقدم (دائرة + رقم)
- [ ] خلط دروس مباشرة + وحدات حسب order

### المرحلة 4: الإضافة السريعة (Bulk) - يوم 3
- [ ] BulkParser.kt + اختبارات وحدة
- [ ] Bulk Dialog UI (TextField + معاينة)
- [ ] ربط الإدخال بالـ DB (معالجة IDs المؤقتة)
- [ ] اختبار مثال المستخدم كاملا

### المرحلة 5: الإحصائيات والنسخ الاحتياطي واللمسات النهائية - يوم 4
- [ ] StatsScreen + حساب إجمالي
- [ ] BackupManager (تصدير/استيراد JSON + SAF)
- [ ] زر أيقونة Backup في TopBar
- [ ] مراجعة iOS UI (ظلال، حواف، مسافات)
- [ ] اختبار شامل + إصلاح Bugs + أيقونة التطبيق

---

## 11. خارج النطاق (للإصدارات القادمة)

- بحث عام - Dark Mode - تقويم - Pomodoro - أولوية/تاريخ استحقاق - نوت/روابط - سحب لإعادة الترتيب - مزامنة سحابية

---

## 12. معايير القبول (Definition of Done)

- [ ] يمكن إضافة مادة باسم فقط وتظهر في List مع عدد متبقي 0
- [ ] يمكن Pin مادة فتثبت فوق
- [ ] داخل المادة يمكن إضافة وحدة (Accordion) ودرس مباشر
- [ ] Bulk بنفس مثال المستخدم ينتج نفس الهيكل المتوقع
- [ ] ضغط درس يلف 3 حالات ويحدث التقدم (دائرة + رقم) فورا
- [ ] فلتر يعمل داخل المادة
- [ ] إحصائيات تعرض نسب صحيحة
- [ ] تصدير JSON ثم حذفه ثم استيراده يرجع كل البيانات
- [ ] التصميم iOS هادي + خط IBM Plex ظاهر في كل النصوص
- [ ] ضغط طويل على أي عنصر يظهر تعديل/حذف

---

*الخطة معتمدة من المستخدم بتاريخ 30/08/2026 - جاهزة للتنفيذ*

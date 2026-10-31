package ua.sprotyv.screens

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ua.sprotyv.content.Content
import ua.sprotyv.content.Kit
import ua.sprotyv.content.MySupplies
import ua.sprotyv.content.Supplies
import ua.sprotyv.home.Pins
import ua.sprotyv.home.Preset
import ua.sprotyv.personal.FamilyPlan
import ua.sprotyv.personal.KitCheck
import ua.sprotyv.personal.MedicalCard
import ua.sprotyv.personal.Note
import ua.sprotyv.personal.Person
import ua.sprotyv.personal.PlanRow
import ua.sprotyv.personal.Setting
import ua.sprotyv.personal.backup.BackupCrypto
import ua.sprotyv.personal.backup.BackupData
import ua.sprotyv.ui.Bookmark
import ua.sprotyv.ui.Bookmarks
import ua.sprotyv.ui.KEY_KIT_REVIEWED
import ua.sprotyv.ui.KEY_PATH
import ua.sprotyv.ui.KEY_SAFE_MESSAGE
import ua.sprotyv.ui.KEY_SUPPLIES
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A made-up person for trying the app with a year of use behind it: Лідія Шевченко, a mother of
 * three in a community near the front. Everything here is invented (names, numbers, places); the
 * phone numbers are not real ones. The result is an ordinary encrypted backup, restored in the
 * app with the password below; it goes to build/screens with the screen pictures.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DemoDataTest {
    private val zone = ZoneId.of("Europe/Kyiv")
    private fun at(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0) = LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant().toEpochMilli()

    @Test fun lidia() {
        val app: Application = ApplicationProvider.getApplicationContext()
        val content = Content.get(app)

        val medical = MedicalCard(
            name = "Лідія Шевченко",
            birthDate = "14.03.1989",
            bloodGroup = "A(II) Rh+",
            allergies = "Пеніцилін: висип і набряк обличчя",
            medicines = "Постійно не приймаю. Після пологів — залізо курсами.",
            conditions = "Гіпотонія: тиск буває 90/60, при слабкості — лягти й підняти ноги.",
            notes = "Троє дітей зі мною: Софія 14 р., Марко 9 р. (астма, інгалятор сальбутамол у рюкзаку), Даринка 4 р. Якщо я непритомна — подзвоніть мамі або сестрі (довірені люди).",
            updatedAt = at(2026, 9, 2),
        )

        val people = listOf(
            Person(name = "Андрій (чоловік)", phone = "+380000000101", relation = "Чоловік, на службі", safeList = true, ice = true),
            Person(name = "Мама Ніна", phone = "+380000000102", relation = "Мама, живе в сусідньому селі", safeList = true, ice = true),
            Person(name = "Оксана (сестра)", phone = "+380000000103", relation = "Сестра, в тилу; приймає дітей", safeList = true, ice = true),
            Person(name = "Софія (донька)", phone = "+380000000104", relation = "Донька, 14 р.", safeList = true),
            Person(name = "Галина Петрівна", phone = "+380000000105", relation = "Сусідка з 3-го поверху, має ключ", safeList = false),
            Person(name = "Староста Іван Миколайович", phone = "+380000000106", relation = "Староста громади", safeList = false),
            Person(name = "Класна керівниця Марка", phone = "+380000000107", relation = "Школа, онлайн-навчання", safeList = false),
            Person(name = "Педіатр Олена Василівна", phone = "+380000000108", relation = "Сімейна лікарка дітей", safeList = false),
        )

        val plan = FamilyPlan(
            point1 = "Укриття в підвалі школи № 2 (вхід з подвір'я)",
            point2 = "Пункт незламності в будинку культури",
            point3 = "У сестри Оксани (в тилу), якщо громаду евакуюють",
            goBetweenName = "Оксана (сестра)",
            goBetweenPhone = "+380000000103",
            notes = "Документи й ліки Марка — в синій папці в рюкзаку. Даринку несу я, Софія веде Марка. Кота — в переноску, вона під ліжком.",
            updatedAt = at(2026, 6, 20),
        )
        val rows = listOf(
            PlanRow(kind = PlanRow.KIND_ROLE, first = "Лідія", second = "Документи, гроші, ліки, Даринка", sort = 0),
            PlanRow(kind = PlanRow.KIND_ROLE, first = "Софія (14)", second = "Веде Марка, павербанк і телефон", sort = 1),
            PlanRow(kind = PlanRow.KIND_ROLE, first = "Марко (9)", second = "Свій рюкзак, вода, ліхтарик, інгалятор", sort = 2),
            PlanRow(kind = PlanRow.KIND_ROLE, first = "Даринка (4)", second = "Тримається за маму, улюблена іграшка", sort = 3),
            PlanRow(kind = PlanRow.KIND_IF_THEN, first = "Немає зв'язку", second = "О 18:00 зустрічаємося в укритті школи № 2", sort = 4),
            PlanRow(kind = PlanRow.KIND_IF_THEN, first = "Діти в школі під час тривоги", second = "Лишаються з учителями в укритті школи, я забираю після відбою", sort = 5),
            PlanRow(kind = PlanRow.KIND_IF_THEN, first = "Оголосили евакуацію", second = "Автобус від ЦНАПу; пишу Оксані «Я в безпеці» і куди їдемо", sort = 6),
            PlanRow(kind = PlanRow.KIND_IF_THEN, first = "Марку погано з диханням", second = "Інгалятор, сісти, відкрити вікно; не минає за 10 хв — 103", sort = 7),
        )

        // The go-bag: most of it ticked over the year, a few things still missing
        val kitItems = Kit.groups(content.guide).flatMap { it.items }.distinctBy { it.key }
        val kit = kitItems.mapIndexed { i, item -> KitCheck(item.key, checked = i % 5 != 3, at = at(2026, 1 + i % 10, 1 + i % 27)) }

        // Supplies for four people for two weeks: most food in place, medicines run out soon
        val food = Supplies.food(content.guide, 4, 14).map { Kit.key(it.what) }
        val supplies = MySupplies(
            people = 4, days = 14, water = 150, waterUntil = "2027-02",
            have = food.filterIndexed { i, _ -> i % 4 != 2 }.toSet(),
            until = mapOf(MySupplies.MEDS to "2026-11", MySupplies.BATTERIES to "2027-05") +
                food.take(3).mapIndexed { i, k -> k to listOf("2027-03", "2026-12", "2027-08")[i] },
        )

        val path = listOf("bleed", "kit", "move", "plan", "people", "shelter", "facts", "neighbours", "help")
        val bookmarks = listOf(
            Bookmark("https://www.dsns.gov.ua", "ДСНС: новини й поради", at(2025, 12, 5)),
            Bookmark("https://diia.gov.ua", "Дія", at(2025, 11, 20)),
            Bookmark("https://mine.dsns.gov.ua", "Мапа мінної небезпеки", at(2026, 4, 12)),
            Bookmark("https://savelife.in.ua", "Повернись живим", at(2026, 2, 24)),
            Bookmark("https://voices.org.ua", "Голоси дітей: психолог для Марка", at(2026, 5, 3)),
        )
        val settings = listOf(
            Setting(KEY_SAFE_MESSAGE, "Я в безпеці, з дітьми все добре. Ми в укритті школи № 2. Напишу, коли буде зв'язок."),
            Setting(KEY_KIT_REVIEWED, at(2026, 9, 28).toString()),
            Setting(KEY_SUPPLIES, supplies.toJson()),
            Setting(KEY_PATH, org.json.JSONArray(path.sorted()).toString()),
        ) + Bookmarks.toPages(bookmarks)!!.mapIndexed { i, page -> Setting(Bookmarks.KEYS[i], page) }

        val notes = listOf(
            Note(text = "Пункт незламності — будинок культури\nПрацює 8:00–20:00. Є генератор, Starlink, чай, можна зарядити телефони. Беру з собою павербанк і подовжувач.", pinned = true, updatedAt = at(2025, 11, 18)),
            Note(text = "Евакуація: що сказав староста\nАвтобус від ЦНАПу о 7:00 і 15:00, якщо оголосять. Брати лише валізу на кожного, тварин можна в переносках. Список — у старости.", pinned = true, updatedAt = at(2026, 7, 9)),
            Note(text = "Марко: астма\nСальбутамол 2 вдихи при нападі. Запасний інгалятор у Софії. Термін придатності — до 03.2027. Алергія на пил і котячу шерсть (кіт живе з нами, лікарка дозволила).", pinned = true, updatedAt = at(2026, 3, 14)),
            Note(text = "Для бригади, 14:20\nСусід Петро, ~60 р., поранення стегна осколком. Турнікет 14:05 (накладала я). Свідомий, блідий. 103 викликано о 14:07.", updatedAt = at(2026, 8, 17, 14, 40)),
            Note(text = "Школа онлайн\nСофія: 8:30–14:00, Марко: 9:00–12:30. Якщо світла немає — уроки в записі ввечері. Пароль від класу в Софії.", updatedAt = at(2026, 9, 1)),
            Note(text = "Гуманітарна допомога\nВПО-довідки не маємо (ми місцеві). Набори видає Червоний Хрест у четвер у бібліотеці, потрібен паспорт і свідоцтва дітей.", updatedAt = at(2026, 1, 22)),
            Note(text = "Генератор\nСусід Петро ділиться: я плачу за 5 л бензину на тиждень. Холодильник вмикаємо на 2 год зранку і ввечері.", updatedAt = at(2026, 1, 9)),
            Note(text = "Що купити в місті\n— батарейки АА (8 шт.)\n— сальбутамол\n— вода 6 л ×4\n— пластир, бинт еластичний\n— консерви для кота", updatedAt = at(2026, 10, 20)),
            Note(text = "Даринка: садочок\nЗакритий через обстріли до кінця року. Група розвитку онлайн у вівторок і четвер о 10:00.", updatedAt = at(2026, 9, 15)),
            Note(text = "Курс першої допомоги\nЧервоний Хрест, у суботу 10:00, бібліотека. Записалася з Софією. Взяти воду й блокнот.", updatedAt = at(2026, 2, 6)),
            Note(text = "Укриття школи № 2\nКлюч у сторожа. Там є лавки, вода, аптечка. Брати каремати й теплі речі для дітей — бетон холодний.", updatedAt = at(2025, 12, 2)),
            Note(text = "Подяка\nВолонтери з Пункту незламності допомогли закрити вікна плівкою після вибуху 12 квітня. Віднести їм пиріг.", updatedAt = at(2026, 4, 13)),
        )

        val home = mapOf(
            "mode" to "FULL", "pins" to Pins.write(Preset.GENERAL.pins), "lock" to "false", "theme" to "SYSTEM",
            "dynamic" to "false", "call_bar" to "true", "readiness_hidden" to "false", "medic" to "false", "service" to "false",
            "neutral" to "false", "lock_mine" to "false",
            "notify_tourniquet" to "true", "notify_wait" to "true", "notify_counts" to "true",
            "notify_supplies" to "true", "notify_notes" to "true", "notify_kit" to "true", "notify_trip_all" to "false",
        )

        // A copy keeps each record's own number, as the app's database gives them
        val snap = BackupData.Snapshot(
            created = at(2026, 10, 31, 9), medical = medical,
            people = people.mapIndexed { i, p -> p.copy(id = i + 1L) }, plan = plan,
            rows = rows.mapIndexed { i, r -> r.copy(id = i + 1L) },
            kit = kit, settings = settings, home = home,
            notes = notes.mapIndexed { i, n -> n.copy(id = i + 1L) },
        )
        val plain = BackupData.encode(snap)
        val file = BackupCrypto.encrypt(plain, PASSWORD.toCharArray(), BackupCrypto.Purpose.BACKUP)
        // The file opens again with the same password, and reads back the same
        val back = BackupData.decode(BackupCrypto.decrypt(file, PASSWORD.toCharArray(), BackupCrypto.Purpose.BACKUP))
        assertEquals(snap.records, back.records)
        val dir = File("build/screens").apply { mkdirs() }
        File(dir, "demo-lidia-shevchenko.sprotyv").writeBytes(file)

        // And it restores into a real database as the app does it, all of it
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(ctx, ua.sprotyv.personal.PersonalDb::class.java).allowMainThreadQueries().build()
        try {
            kotlinx.coroutines.runBlocking {
                // Words typed with spaces and capitals open it too, as on the restore screen
                val typed = BackupCrypto.asWords("Lidia Demo Test Rik".toCharArray())!!
                val restored = BackupData.decode(BackupCrypto.decrypt(file, typed, BackupCrypto.Purpose.BACKUP))
                BackupData.replaceAll(db, restored, ua.sprotyv.ui.BACKED_UP_SETTINGS)
                assertEquals(people.size, db.people().all().size)
                assertEquals(notes.size, db.notes().all().size)
            }
        } finally {
            db.close()
        }
    }

    companion object {
        /** Test data only: the password is public on purpose. */
        const val PASSWORD = "lidia-demo-test-rik"
    }
}

package io.legado.app.help.book

import io.legado.app.constant.BookType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookAutoTagHelperTest {

    @Test
    fun novelKindMapsToCanonicalTags() {
        assertEquals(
            listOf("玄幻"),
            BookAutoTagHelper.detectTags(BookType.text, "玄幻", null, null)
        )
        assertEquals(
            listOf("玄幻"),
            BookAutoTagHelper.detectTags(BookType.text, "东方玄幻", null, null)
        )
        assertEquals(
            listOf("仙侠", "都市", "重生"),
            BookAutoTagHelper.detectTags(BookType.text, "重生之都市修仙", null, null)
        )
    }

    @Test
    fun novelKindCanProduceMultipleTags() {
        assertEquals(
            listOf("都市", "言情", "穿越"),
            BookAutoTagHelper.detectTags(BookType.text, "都市言情,穿越", null, null)
        )
        assertEquals(
            listOf("悬疑", "灵异"),
            BookAutoTagHelper.detectTags(BookType.text, "悬疑灵异", null, null)
        )
    }

    @Test
    fun kindNameAndOriginNameAreUsedTogether() {
        assertEquals(
            listOf("玄幻", "都市"),
            BookAutoTagHelper.detectTags(BookType.text, "玄幻", "都市传说", null)
        )
    }

    @Test
    fun realDeviceNovelsGetMissingTags() {
        assertEquals(
            listOf("玄幻"),
            BookAutoTagHelper.detectTags(
                BookType.text,
                null,
                "斗破苍穹",
                "🍅番茄小说源",
                "这里是属于斗气的世界，没有花俏艳丽的魔法，有的，仅仅是繁衍到巅峰的斗气！"
            )
        )

        assertEquals(
            listOf("灵异"),
            BookAutoTagHelper.detectTags(
                BookType.text,
                "鬼话,2024-06-16",
                "人在聊斋：开局炖了讨封黄皮子",
                "💠 手机看书"
            )
        )
        assertEquals(
            listOf("仙侠", "灵异"),
            BookAutoTagHelper.detectTags(
                BookType.text,
                "鬼话,2024-08-21",
                "长生：从瓶山开始修仙法",
                "💠 手机看书"
            )
        )
        assertEquals(
            listOf("玄幻", "灵异"),
            BookAutoTagHelper.detectTags(
                BookType.text,
                "鬼话,2024-09-07",
                "诸天第一禁忌",
                "💠 手机看书"
            )
        )
    }

    @Test
    fun comicKindMapsToPlatformCategories() {
        assertEquals(
            listOf("热血", "冒险", "少年"),
            BookAutoTagHelper.detectTags(BookType.image, "热血,少年,冒险", null, null)
        )
        assertEquals(
            listOf("武侠仙侠"),
            BookAutoTagHelper.detectTags(BookType.image, "武侠仙侠", null, null)
        )
        assertEquals(
            listOf("悬疑灵异"),
            BookAutoTagHelper.detectTags(BookType.image, "悬疑灵异", null, null)
        )
    }

    @Test
    fun realDeviceComicsGetMissingTags() {
        assertEquals(
            listOf("热血", "冒险"),
            BookAutoTagHelper.detectTags(
                BookType.image,
                "海盗王, OP, 航海王, 2026-07-11, 下篇, 尾田荣一郎",
                "海贼王剧场版 红发歌姬",
                "漫画搬运（优+）"
            )
        )
        assertEquals(
            listOf("奇幻", "架空"),
            BookAutoTagHelper.detectTags(
                BookType.image,
                "幼女な魔女と森の熊さん～异世界でポーション创ってのんびりスローライフがしたいのじゃが？～, 2026-08-08, 第01话, 雪 砂くじら",
                "TS年幼魔女与森林里的熊先生～本想靠制作药水在异世界悠闲度日？～",
                "漫画搬运（优+）"
            )
        )
        assertEquals(
            listOf("热血", "恋爱", "奇幻"),
            BookAutoTagHelper.detectTags(
                BookType.image,
                "恋爱,动作",
                "让出天赋后，我成了魔法界团宠",
                "爱优漫吧（优+）"
            )
        )
    }

    @Test
    fun audioFallsBackToNameAndOriginName() {
        assertEquals(
            listOf("相声"),
            BookAutoTagHelper.detectTags(BookType.audio, null, "郭德纲相声全集", null)
        )
        assertEquals(
            listOf("评书", "历史"),
            BookAutoTagHelper.detectTags(BookType.audio, "评书,历史", null, null)
        )
        assertEquals(
            listOf("儿童", "故事"),
            BookAutoTagHelper.detectTags(BookType.audio, null, "儿童睡前故事", null)
        )
    }

    @Test
    fun realDeviceAudioGetsBroadcastDramaTag() {
        assertEquals(
            listOf("广播剧"),
            BookAutoTagHelper.detectTags(
                BookType.audio,
                null,
                "我反对这门亲事「真香 双男“神” 欢喜冤家 强强」",
                "喜马拉雅（优+）"
            )
        )
    }

    @Test
    fun mergeTagsKeepsExistingAndDeduplicates() {
        assertEquals(
            "玄幻,收藏,都市",
            BookAutoTagHelper.mergeTags("玄幻 收藏", listOf("都市"))
        )
        assertEquals(
            "玄幻",
            BookAutoTagHelper.mergeTags("玄幻", listOf("玄幻"))
        )
        assertNull(BookAutoTagHelper.mergeTags(null, emptyList()))
    }
}

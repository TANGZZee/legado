package io.legado.app.help.book

import io.legado.app.constant.BookType
import io.legado.app.data.appDb

/**
 * 新书入库时按类型自动打标签。
 *
 * 标签体系参考主流平台分类：
 * - 小说：起点中文网/番茄小说（玄幻、奇幻、武侠、仙侠、都市、现实、军事、历史、
 *   游戏、体育、科幻、悬疑、灵异、言情、青春、轻小说及常见题材标签）
 * - 漫画：哔哩哔哩漫画（热血、冒险、搞笑、恋爱、校园、奇幻、科幻、悬疑、推理、
 *   运动、恐怖、都市、历史、古风、玄幻、武侠仙侠、游戏竞技、悬疑灵异、架空、
 *   青春、西幻、现代、少年、少女、青年）
 * - 音频：喜马拉雅（有声小说、儿童、相声、评书、广播剧、播客、电台、音乐、戏曲、
 *   小品、新闻、故事、情感、历史、人文、财经、科技、娱乐、生活、健康、体育、
 *   外语、汽车、游戏、旅游、时尚、知识、脱口秀）
 */
object BookAutoTagHelper {

    private data class TagRule(val tag: String, val keywords: List<String>)

    private val novelRules = listOf(
        TagRule(
            "玄幻",
            listOf(
                "玄幻", "东方玄幻", "异界大陆", "异世大陆", "高武世界", "洪荒", "远古神话", "诸天", "神魔",
                "斗气", "斗气大陆", "斗破苍穹", "斗罗大陆", "武动乾坤", "完美世界", "遮天", "圣墟", "大主宰", "元尊"
            )
        ),
        TagRule(
            "奇幻",
            listOf(
                "奇幻", "西幻", "西方奇幻", "魔幻", "魔法", "魔法界", "魔女", "异世界", "药水", "剑与魔法",
                "史诗奇幻", "黑暗奇幻", "诡秘之主", "哈利波特", "魔戒", "冰与火之歌"
            )
        ),
        TagRule(
            "武侠",
            listOf(
                "武侠", "传统武侠", "新武侠", "古典武侠", "江湖", "国术", "武侠同人",
                "射雕英雄传", "神雕侠侣", "倚天屠龙记", "天龙八部", "笑傲江湖", "鹿鼎记", "雪中悍刀行", "小李飞刀"
            )
        ),
        TagRule(
            "仙侠",
            listOf(
                "仙侠", "修仙", "修真", "古典仙侠", "现代修真", "幻想修仙", "神话修真", "仙侠奇缘", "凡人流", "飞升",
                "凡人修仙传", "仙逆", "一念永恒", "我欲封天", "求魔", "剑来", "择天记"
            )
        ),
        TagRule(
            "都市",
            listOf(
                "都市", "都市生活", "都市异能", "都市青春", "都市职场", "都市言情", "都市种田", "商战", "职场",
                "官场", "娱乐明星", "异术超能", "豪门", "总裁", "校花的贴身高手", "最强弃少"
            )
        ),
        TagRule("现实", listOf("现实", "现实百态", "人间百态", "家庭伦理", "乡村生活", "生活向", "当代生活", "纪实")),
        TagRule("军事", listOf("军事", "军旅", "军旅生涯", "战争", "抗战", "抗日", "谍战", "特工", "特种兵", "军工")),
        TagRule(
            "历史",
            listOf(
                "历史", "架空历史", "历史架空", "架空", "历史传记", "穿越历史", "秦汉", "三国", "两晋", "隋唐",
                "大唐", "盛唐", "五代", "大明", "明朝", "清朝", "清史", "民国", "先秦", "古代",
                "庆余年", "琅琊榜", "明朝那些事儿", "三国演义"
            )
        ),
        TagRule(
            "游戏",
            listOf("游戏", "网游", "虚拟网游", "游戏异界", "电子竞技", "电竞", "游戏系统", "全息网游", "游戏竞技", "全职高手")
        ),
        TagRule("体育", listOf("体育", "体育竞技", "竞技", "篮球", "足球", "网球", "乒乓球", "格斗", "搏击", "运动")),
        TagRule(
            "科幻",
            listOf(
                "科幻", "科幻末世", "星际", "星际文明", "太空", "机甲", "古武机甲", "末世", "末日", "进化",
                "变异", "时空穿梭", "超级科技", "未来世界", "赛博朋克", "星战", "外星", "数字生命", "人工智能",
                "元宇宙", "三体", "球状闪电", "流浪地球"
            )
        ),
        TagRule(
            "悬疑",
            listOf(
                "悬疑", "悬疑推理", "推理", "侦探", "刑侦", "罪案", "破案", "盗墓", "探险", "奇闻", "烧脑",
                "盗墓笔记", "鬼吹灯", "我有一座冒险屋", "十宗罪"
            )
        ),
        TagRule(
            "灵异",
            listOf(
                "灵异", "灵异鬼怪", "鬼怪", "鬼话", "聊斋", "志怪", "恐怖", "恐怖灵异", "怪谈", "都市怪谈",
                "僵尸", "驱鬼", "惊悚", "盗墓笔记", "鬼吹灯"
            )
        ),
        TagRule(
            "言情",
            listOf(
                "言情", "古代言情", "现代言情", "都市言情", "言情穿越", "古言", "现言", "纯爱", "浪漫", "甜宠",
                "虐恋", "宫斗", "宅斗", "女频", "何以笙箫默", "微微一笑很倾城", "步步惊心"
            )
        ),
        TagRule("青春", listOf("青春", "青春校园", "青春文学", "校园", "校园爱情", "少年", "少女", "青春疼痛")),
        TagRule("轻小说", listOf("轻小说", "二次元", "动漫", "同人", "衍生", "日常", "中二")),
        TagRule("穿越", listOf("穿越", "穿越时空", "架空穿越", "古代穿越", "现代穿越", "快穿", "重生穿越", "庆余年", "步步惊心", "寻秦记")),
        TagRule("重生", listOf("重生", "重回", "重来")),
        TagRule("系统", listOf("系统", "系统流", "签到", "任务流", "万界")),
        TagRule("无限流", listOf("无限流", "主神", "轮回", "副本", "诸天万界")),
        TagRule("种田", listOf("种田", "种田流", "田园")),
        TagRule("宫斗", listOf("宫斗", "宫廷", "宫闱")),
        TagRule("宅斗", listOf("宅斗", "宅门", "世家"))
    )

    private val comicRules = listOf(
        TagRule(
            "热血",
            listOf(
                "热血", "燃", "战斗", "动作", "争霸", "格斗", "海贼王", "海盗王", "航海王", "op", "少年热血", "热血少年",
                "火影忍者", "龙珠", "七龙珠", "鬼灭之刃", "咒术回战", "全职猎人", "我的英雄学院", "电锯人"
            )
        ),
        TagRule(
            "冒险",
            listOf(
                "冒险", "探险", "寻宝", "冒险家", "旅行", "海贼王", "海盗王", "航海王", "op",
                "火影忍者", "龙珠", "鬼灭之刃", "全职猎人", "我的英雄学院"
            )
        ),
        TagRule("搞笑", listOf("搞笑", "幽默", "轻松", "沙雕", "欢乐", "吐槽", "一拳超人", "银魂", "辉夜大小姐", "月刊少女野崎君")),
        TagRule("恋爱", listOf("恋爱", "恋爱日常", "爱情", "纯爱", "浪漫", "甜宠", "虐恋", "言情", "辉夜大小姐", "好想告诉你", "元气少女缘结神", "月刊少女野崎君")),
        TagRule("校园", listOf("校园", "学院", "学园", "学校", "青春校园", "灌篮高手", "网球王子", "排球少年", "辉夜大小姐", "古见同学")),
        TagRule(
            "奇幻",
            listOf(
                "奇幻", "东方奇幻", "史诗奇幻", "黑暗奇幻", "魔法", "魔法界", "魔女", "异世界", "药水",
                "咒术回战", "鬼灭之刃", "进击的巨人", "葬送的芙莉莲", "间谍过家家"
            )
        ),
        TagRule(
            "科幻",
            listOf(
                "科幻", "星际", "太空", "机甲", "赛博朋克", "未来", "末世", "末日", "外星", "超级科技",
                "一拳超人", "攻壳机动队", "新世纪福音战士", "命运石之门", "星际牛仔", "亚人"
            )
        ),
        TagRule("悬疑", listOf("悬疑", "侦探", "刑侦", "罪案", "烧脑", "惊悚", "名侦探柯南", "柯南", "金田一", "死亡笔记", "进击的巨人", "电锯人")),
        TagRule("推理", listOf("推理", "悬疑推理", "本格推理", "解密", "解谜", "名侦探柯南", "柯南", "金田一", "死亡笔记")),
        TagRule("运动", listOf("运动", "体育", "体育竞技", "篮球", "足球", "网球", "排球", "棒球", "格斗", "竞技", "灌篮高手", "网球王子", "排球少年", "足球小将", "黑子的篮球")),
        TagRule("恐怖", listOf("恐怖", "恐怖灵异", "鬼怪", "怪谈", "都市怪谈", "僵尸", "电锯人", "伊藤润二", "地狱少女", "进击的巨人")),
        TagRule("都市", listOf("都市", "都市生活", "都市异能", "都市言情", "职场", "商战")),
        TagRule("历史", listOf("历史", "架空历史", "历史架空", "古代", "三国", "大唐", "明朝", "清朝", "民国", "历史传记", "王者天下", "亚尔斯兰战记", "火凤燎原")),
        TagRule("古风", listOf("古风", "古装", "古典", "宫廷", "古代言情", "古言")),
        TagRule("玄幻", listOf("玄幻", "东方玄幻", "洪荒", "高武", "诸天", "斗破苍穹", "斗罗大陆", "完美世界", "遮天")),
        TagRule("武侠仙侠", listOf("武侠仙侠", "武侠", "仙侠", "修仙", "修真", "江湖", "国术", "传统武侠", "古典仙侠", "秦时明月", "不良人")),
        TagRule("游戏竞技", listOf("游戏竞技", "游戏", "网游", "电竞", "电子竞技", "虚拟网游", "游戏异界")),
        TagRule("悬疑灵异", listOf("悬疑灵异", "灵异", "鬼怪", "怪谈", "都市怪谈")),
        TagRule("架空", listOf("架空", "架空历史", "历史架空", "异世界", "异世大陆", "穿越")),
        TagRule("青春", listOf("青春", "青春文学", "青春疼痛")),
        TagRule("西幻", listOf("西幻", "西方奇幻", "剑与魔法", "魔幻")),
        TagRule("现代", listOf("现代", "现代都市", "当代", "现实")),
        TagRule("少年", listOf("少年", "少年漫画", "热血少年", "少年向", "少年jump", "少年JUMP")),
        TagRule("少女", listOf("少女", "少女漫画", "少女心")),
        TagRule("青年", listOf("青年", "青年漫画", "青年向"))
    )

    private val audioRules = listOf(
        TagRule(
            "有声小说",
            listOf(
                "有声小说", "有声书", "听书", "小说", "悬疑小说", "玄幻小说", "言情小说", "都市小说", "武侠小说",
                "科幻小说", "历史小说", "恐怖小说", "凡人修仙传", "斗破苍穹", "斗罗大陆", "遮天", "完美世界",
                "雪中悍刀行", "庆余年", "赘婿", "鬼吹灯", "盗墓笔记", "三体", "诡秘之主", "琅琊榜", "明朝那些事儿",
                "大奉打更人"
            )
        ),
        TagRule("儿童", listOf("儿童", "儿歌", "睡前故事", "少儿", "童话", "早教", "亲子", "宝宝")),
        TagRule("相声", listOf("相声", "相声小品", "郭德纲", "德云社")),
        TagRule("评书", listOf("评书", "说书", "单田芳", "袁阔成", "刘兰芳", "田连元", "岳飞传", "隋唐演义", "三国演义", "白眉大侠", "童林传")),
        TagRule(
            "广播剧",
            listOf(
                "广播剧", "有声剧", "配音剧", "猫耳", "双男", "双男主",
                "魔道祖师", "天官赐福", "撒野", "杀破狼", "默读", "破云", "吞海", "将进酒", "六爻", "碎玉投珠",
                "小蘑菇", "残次品", "犯罪心理", "判官", "全球高考", "营业悖论"
            )
        ),
        TagRule("播客", listOf("播客", "podcast", "Podcast", "聊天")),
        TagRule("电台", listOf("电台", "FM", "广播电台", "网络电台", "直播")),
        TagRule("音乐", listOf("音乐", "歌曲", "歌单", "钢琴", "古筝", "纯音乐", "轻音乐", "流行", "摇滚", "古典音乐")),
        TagRule("戏曲", listOf("戏曲", "京剧", "越剧", "豫剧", "黄梅戏", "昆曲", "评剧", "粤剧")),
        TagRule("小品", listOf("小品", "相声小品", "喜剧小品")),
        TagRule("新闻资讯", listOf("新闻", "资讯", "时事", "热点", "快讯")),
        TagRule("故事", listOf("故事", "睡前故事", "寓言", "童话故事", "民间故事", "情感故事")),
        TagRule("情感", listOf("情感", "感情", "两性", "婚姻", "心理", "治愈", "心灵", "夜听")),
        TagRule("历史", listOf("历史", "三国", "大唐", "明朝", "清朝", "民国", "史记", "历史人文", "考古")),
        TagRule("人文", listOf("人文", "文化", "文学", "哲学", "艺术", "国学", "诗词", "名著", "经典")),
        TagRule("财经", listOf("财经", "经济", "金融", "投资", "理财", "商业", "股市", "基金", "保险")),
        TagRule("科技", listOf("科技", "数码", "互联网", "人工智能", "编程", "电脑", "手机", "科学")),
        TagRule("娱乐", listOf("娱乐", "明星", "八卦", "影视", "综艺", "搞笑", "段子")),
        TagRule("生活", listOf("生活", "美食", "家居", "日常", "生活方式")),
        TagRule("健康", listOf("健康", "养生", "医疗", "健身", "减肥", "营养")),
        TagRule("体育", listOf("体育", "篮球", "足球", "健身", "运动", "奥运")),
        TagRule("外语", listOf("外语", "英语", "日语", "韩语", "法语", "德语", "口语", "雅思", "托福")),
        TagRule("汽车", listOf("汽车", "买车", "驾驶", "二手车", "汽车知识")),
        TagRule("游戏", listOf("游戏", "电竞", "电子竞技", "手游", "网游", "主机游戏")),
        TagRule("旅游", listOf("旅游", "旅行", "出行", "景点", "游记", "户外")),
        TagRule("时尚", listOf("时尚", "穿搭", "美妆", "潮流", "设计", "奢侈品")),
        TagRule("知识", listOf("知识", "科普", "课堂", "教育", "学习", "课程", "讲座", "百科", "通识")),
        TagRule("脱口秀", listOf("脱口秀", "单口喜剧", "stand-up", "喜剧"))
    )

    fun detectTags(
        type: Int,
        kind: String?,
        name: String?,
        originName: String?,
        intro: String? = null
    ): List<String> {
        val rules = buildList {
            if (type and BookType.text > 0) addAll(novelRules)
            if (type and BookType.image > 0) addAll(comicRules)
            if (type and BookType.audio > 0) addAll(audioRules)
        }
        if (rules.isEmpty()) return emptyList()

        // kind 往往带有来源平台附加信息，书名/文件名也可能携带更明确的题材词；书名侧没命中时才看简介
        val primaryText = listOfNotNull(kind, name, originName)
            .joinToString(",")
            .trim()
        val primaryTags = detectFromText(primaryText, rules)
        if (primaryTags.isNotEmpty()) return primaryTags

        return detectFromText(intro.orEmpty().trim(), rules)
    }

    private fun detectFromText(text: String, rules: List<TagRule>): List<String> {
        if (text.isBlank()) return emptyList()

        val tokens = BookTagHelper.parse(text)
        val exactMatchedTokens = tokens.filterTo(mutableSetOf()) { token ->
            rules.any { rule -> rule.keywords.any { it.equals(token, ignoreCase = true) } }
        }
        val unmatchedTokens = tokens.filterNot { it in exactMatchedTokens }

        return rules.mapNotNull { rule ->
            val exact = rule.keywords.any { keyword ->
                tokens.any { it.equals(keyword, ignoreCase = true) }
            }
            val fuzzy = !exact && unmatchedTokens.any { token ->
                rule.keywords.any { keyword ->
                    token.contains(keyword, ignoreCase = true)
                }
            }
            if (exact || fuzzy) rule.tag else null
        }.distinct()
    }

    fun mergeTags(existing: String?, detected: List<String>): String? {
        if (detected.isEmpty()) return existing
        return BookTagHelper.join(BookTagHelper.parse(existing) + detected)
    }

    /**
     * 为自动标签上线前已入库的书籍补一次标签，只合并识别结果，不覆盖用户已有标签。
     */
    fun backfillExistingBooks() {
        appDb.bookDao.all
            .filter { (it.type and BookType.notShelf) == 0 }
            .filter { it.type and (BookType.text or BookType.image or BookType.audio) > 0 }
            .forEach { book ->
                val tags = detectTags(book.type, book.kind, book.name, book.originName, book.intro)
                if (tags.isNotEmpty()) {
                    appDb.bookDao.updateCustomTag(book.bookUrl, mergeTags(book.customTag, tags))
                }
            }
    }
}

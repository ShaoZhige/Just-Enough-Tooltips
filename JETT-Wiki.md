# JETT (Just Enough Tooltips) 使用指南

> 版本：v1.5.0 | Minecraft 1.16.1-1.16.5 | Forge 32+

---

## 这个模组是做什么的？

JETT 让你**完全掌控物品提示框（tooltip）的显示内容**——隐藏不想要的属性行、添加全局/按物品的自定义文字、合并多修饰符、删除物品名称、用正则过滤任意行。纯客户端，只改你看到的，不影响游戏数据。

---

## 快速开始

1. 把 `JETT-1.16.x-1.5.0.jar` 丢进 `mods/` 文件夹
2. 启动游戏一次，会自动生成配置文件 `config/jett-common.toml`
3. 关闭游戏，打开配置，按需开启功能
4. 重新进游戏，效果生效

> **提示：** 部分配置支持热加载（改完文件切回游戏即可生效），但建议每次改完配置后重启游戏。

---

## 配置文件位置

```
.minecraft/
  └── config/
      └── jett-common.toml    ← 这个文件
```

---

## 功能详解

### 1. 隐藏属性修饰符

**有什么用？** 武器上的 "攻击伤害" / "攻击速度" 看腻了？关掉它们。

**配置位置：** `[general]` → `hiddenAttributes`

**填写规则：** 每一项是属性的注册名。下表是原版全部 9 种属性：

| 配置名 | 对应 tooltip 显示（中文/英文） |
|---|---|
| `generic.attack_damage` | 攻击伤害 / Attack Damage |
| `generic.attack_speed` | 攻击速度 / Attack Speed |
| `generic.armor` | 护甲值 / Armor |
| `generic.armor_toughness` | 盔甲韧性 / Armor Toughness |
| `generic.knockback_resistance` | 击退抗性 / Knockback Resistance |
| `generic.max_health` | 最大生命值 / Max Health |
| `generic.movement_speed` | 移动速度 / Movement Speed |
| `generic.attack_knockback` | 攻击击退 / Attack Knockback |
| `generic.luck` | 幸运 / Luck |

**示例：**

```toml
# 只隐藏攻击伤害和攻击速度（最常用的设置）
hiddenAttributes = ["generic.attack_damage", "generic.attack_speed"]

# 连护甲值一起隐藏
hiddenAttributes = ["generic.attack_damage", "generic.attack_speed", "generic.armor"]

# 隐藏全部原版属性
hiddenAttributes = [
    "generic.attack_damage",
    "generic.attack_speed",
    "generic.armor",
    "generic.armor_toughness",
    "generic.knockback_resistance",
    "generic.max_health",
    "generic.movement_speed",
    "generic.attack_knockback",
    "generic.luck"
]

# 不隐藏任何属性（默认状态）
hiddenAttributes = []
```

**注意：** 其他模组的属性也可以隐藏，填对应的注册名即可。如果你安装了 CraftTweaker，可以用 `/ct dump attribute` 指令导出所有可用属性的完整列表。

---

### 2. 合并多修饰符

**有什么用？** 当同种属性有多个修饰符时（如武器基础值 ++CRT 额外加成 + 装备词条叠加），合并成一行显示总值。

**配置位置：** `[general]` → `mergeModifiers`

**灵感来源：** NeoForge 内置的 `enableMergedAttributeTooltips` 功能（`AttributeUtil.applyTextFor`）。

| 值 | 效果 |
|---|---|
| `false`（默认） | 多修饰符分行显示 |
| `true` | 合并显示总值 |

**合并规则：**

| 属性 | 显示格式 | 颜色 |
|---|---|---|
| 攻击伤害 | ` 8 Attack Damage`（总值 = API 和 + 1） | 绿色 |
| 攻击速度 | ` 1.6 Attack Speed`（总值 = API 和 + 4） | 绿色 |
| 其他所有属性 | ` +6 Armor` 或 ` -2 Armor`（总值 = API 和） | 蓝色 / 红色 |

> 攻击伤害和攻击速度是原版默认修饰符，合并后继承无符号显示风格；其他属性保持 `+/-` 符号风格。

**运算支持：**
- ✅ 加法（ADDITION）— 正常累加
- ✅ 减法（负值的 ADDITION）— 自动纳入求和
- ❌ 乘法（MULTIPLY_BASE / MULTIPLY_TOTAL）— 检测到则跳过该属性，保留原样

**效果对比：**

```
钻石剑 + CRT 加 1 伤害：
mergeModifiers = false:          mergeModifiers = true:
   2 攻击伤害                      8 攻击伤害        （绿色）
   7 攻击伤害
   1.6 攻击速度                   1.6 攻击速度      （绿色）

钻石胸甲 + CRT 加 3 护甲：
mergeModifiers = false:          mergeModifiers = true:
   8 护甲值                       +11 护甲值        （蓝色）
  +3 护甲值
```

**兼容性：**
- 兼容 **CraftTweaker** 通过 `addGlobalAttributeModifier` 添加的修饰符
- 兼容 其他模组自己添加的属性
- tooltip 文本匹配使用当前语言

**注意事项：**
- 只有一个修饰符时不变，两个及以上才合并
- 攻击伤害永远在攻击速度上面，合并后自动检查并修正
- 合并和 `hiddenAttributes` 互不冲突——合并后的行一样可以被隐藏

---

### 3. 隐藏槽位标题行 + 特殊标题

**有什么用？** 装备栏的 "在主手时："、"在副手时：" 等标题行，以及药水箭的 "当生效后：" 都能独立开关。

> ⚠️ **1.20.x 版本额外支持：** 隐藏烟花的 "飞行时间：N" （`hideFireworkFlight`）。

**配置位置：** `[slotHeaders]`

**可用开关：**

| 开关 | 对应标题 |
|---|---|
| `hideMainhand` | 在主手时 / When in main hand |
| `hideOffhand` | 在副手时 / When in off hand |
| `hideHead` | 在头盔时 / When on head |
| `hideChest` | 在盔甲时 / When on body |
| `hideLegs` | 在护腿时 / When on legs |
| `hideFeet` | 在靴子时 / When on feet |
| `hidePotionEffectHeader` | 当生效后： / When Applied: |

**示例：**

```toml
[slotHeaders]
    hideMainhand           = true   # 隐藏主手标题
    hideOffhand            = false  # 保留副手标题
    hidePotionEffectHeader = true   # 隐藏药水箭"当生效后："
```

---

### 4. 物品白名单

**有什么用？** 加了白名单的物品，JETT **不会碰它的 tooltip**——属性保留、标题保留、一切照原样显示。

**配置位置：**

| 白名单 | 作用范围 |
|---|---|
| `[general] itemWhitelist` | 全局：跳过所有 JETT 处理 |
| `[slotHeaders] slotHeaderWhitelist` | 仅：槽位标题行不被隐藏 |

**格式：** `modid:item_name`，每行一个。

**示例：**

```toml
[general]
    # 这些物品完全不受 JETT 影响
    itemWhitelist = ["minecraft:diamond_sword", "minecraft:netherite_sword"]

[slotHeaders]
    # 只有槽位标题不受影响，属性照常隐藏
    slotHeaderWhitelist = ["minecraft:iron_chestplate"]
```

---

### 5. 全局提示文字

**有什么用？** 给**所有物品**统一加上一行文字——比如服务器名称、整合包包水印、使用提示等。不需要每个物品单独配置。

**配置位置：** `[globalTooltip]`

**两个配置项：**

| 配置项 | 类型 | 说明 |
|---|---|---|
| `globalTooltip` | 字符串 | 要显示的文字（空 = 禁用） |
| `globalTooltipBlacklist` | 列表 | 不显示全局提示的物品（格式：`modid:item_name`） |

**格式规则：**

```
"text"    → 追加到 tooltip 末尾
"N:text"  → 插入到第 N 行（0 = 第一行）
""        → 禁用
```

**示例：**

```toml
[globalTooltip]
    # 在所有物品末尾附加灰色提示
    globalTooltip = "§7§o右键使用"

    # 在所有物品第一行加上金色标题
    globalTooltip = "0:§e§l★ 服务器物品 ★"

    # 排除空气和屏障方块
    globalTooltipBlacklist = ["minecraft:air", "minecraft:barrier"]
```

**支持的格式代码：** 与自定义 tooltip 相同（见下方第 6 节的颜色代码表）。

---

### 6. 自定义 tooltip 文字

**有什么用？** 给物品添加你想显示的任何文字。

**配置位置：** `[customTooltips]` → `customTooltips`

**基本格式：**

```
"key=[N:]要显示的文字"
```

**key 支持两种写法：**

| 写法 | 示例 | 说明 |
|---|---|---|
| 单个物品 | `minecraft:diamond_sword` | 只对钻石剑生效 |
| 多物品 | `minecraft:diamond,minecraft:diamond_block,minecraft:emerald` | 逗号分隔，一条配置覆盖多个物品 |

**指定位置：** 在文字前加 `行号:`，可以控制插在第几行：

```
"物品注册名=2:要显示的文字"   → 插在第三行（0 是第一行）
"物品注册名=要显示的文字"     → 不加行号 = 追加到末尾
```

**支持原版颜色 & 格式代码：**

| 代码 | 效果 | 代码 | 效果 |
|---|---|---|---|
| `§0` | 黑色 | `§a` | 绿色 |
| `§1` | 深蓝 | `§b` | 亮蓝 |
| `§2` | 深绿 | `§c` | 红色 |
| `§3` | 青色 | `§d` | 粉色 |
| `§4` | 深红 | `§e` | 黄色 |
| `§5` | 紫色 | `§f` | 白色 |
| `§6` | 金色 | `§7` | 灰色 |
| `§8` | 深灰 | `§9` | 蓝色 |
| `§k` | 乱码 | `§l` | **粗体** |
| `§m` | ~~删除线~~ | `§n` | <u>下划线</u> |
| `§o` | *斜体* | `§r` | 重置格式 |

> **重要：颜色代码必须写在格式代码前面！** 例如 `§b§o文字`（亮蓝+斜体）生效，`§o§b文字` 则斜体失效只有颜色。

**多条目、同位置、保序：** 多条同位置的条目按配置顺序排列，不会互相覆盖。

**示例大全：**

```toml
[customTooltips]
    customTooltips = [
        # 基础用法：末尾追加
        "minecraft:iron_sword=自制的铁剑",

        # 带颜色
        "minecraft:diamond_sword=§b§o钻石制品",

        # 指定位置：插在第 1 行（物品名后面）
        "minecraft:diamond_sword=1:§b[稀有]",

        # 指定位置：插在第 0 行（物品名前面）
        "minecraft:golden_apple=0:§e§l★ 神器 ★",

        # 多条目不同位置
        "minecraft:netherite_sword=1:§4§l传说级",
        "minecraft:netherite_sword=§8耐久: 无限",

        # 多物品共用（逗号分隔）
        "minecraft:diamond,minecraft:diamond_block,minecraft:emerald=0:§b★ 闪闪发光 ★"
    ]
```

**效果预览：** 以上配置让下界合金剑显示为：

```
§e§l★ 神器 ★
下界合金剑                ← 原始名称
§4§l传说级                 ← 第 1 行插入
 8 攻击伤害                ← 原始属性（没被隐藏的话）
§8耐久: 无限               ← 末尾追加
```

---

### 7. 隐藏物品名称

**有什么用？** 去掉物品名，配合自定义 tooltip 实现完全重命名。

**配置位置：** `[general]`

**两种模式：**

| 模式 | 配置项 | 用法 |
|---|---|---|
| 指定物品 | `hideItemNames` | 列出要隐藏名称的物品 |
| 全局隐藏 | `hideAllItemNames` | 设为 `true`，所有物品名消失 |

**白名单：** `itemNameWhitelist` 在全局隐藏时保留指定物品的名称。

**示例：**

```toml
[general]
    # 模式一：只隐藏铁剑和钻石剑的名称
    hideItemNames = ["minecraft:iron_sword", "minecraft:diamond_sword"]

    # 模式二：隐藏所有物品名称，但保留下界合金剑的名字
    hideAllItemNames = true
    itemNameWhitelist = ["minecraft:netherite_sword"]
```

---

### 8. 正则表达式删除

**有什么用？** 用正则匹配任意 tooltip 行，匹配到的直接删除。最灵活的功能。

**配置位置：** `[regexRemove]`

**三种模式：**

| 模式 | 配置项 | 说明 |
|---|---|---|
| 全局正则 | `regexRemove` | 匹配所有物品 |
| 按物品正则 | `perItemRegexRemove` | 仅对指定物品生效 |
| 白名单 | `regexRemoveWhitelist` | 上面的物品跳过全局正则 |

**注意：** `Matcher.find()` 匹配行中**任意位置**，不是只匹配开头。写 `A` 会匹配到所有含 "A" 的行（不区分大小写需要加 `(?i)`）。

**常用正则示例：**

| 要删除的内容 | 正则 |
|---|---|
| 包含 "Attack Damage" 的行 | `Attack Damage` |
| 包含 "攻击伤害" 的行 | `攻击伤害` |
| 所有槽位标题行 | `在.*：` 或 `When in` |
| 所有带数字的行 | `\d` |
| 排除正数（绿/蓝色） | `\+\d+` |
| 排除负数（红色） | `-\d+` |
| 不区分大小写匹配 armor | `(?i)armor` |
| 完全等于某一整行 | `^行文本内容$` |

**示例：**

```toml
[regexRemove]
    # 全局：删除所有含 "Attack Damage" 的行
    regexRemove = ["Attack Damage", "攻击伤害"]

    # 白名单：钻石剑不受全局正则影响
    regexRemoveWhitelist = ["minecraft:diamond_sword"]

    # 按物品：只对铁剑删除含 "Speed" 的行
    perItemRegexRemove = ["minecraft:iron_sword=Speed"]
```

**执行顺序：** 正则删除是最后一步，会覆盖前面所有功能的结果，包括自定义 tooltip。

---

## 功能执行顺序

了解顺序有助于排查"为什么不该出现的还在/该出现的不在"的问题。所有功能按以下顺序执行，**后面的可以覆盖前面的结果**：

```
1. 隐藏物品名称    — 删除第一行。hideItemNames 指定物品 / hideAllItemNames 全局
2. 全局提示文字    — globalTooltip 对所有物品（黑名单除外）添加统一文字
3. 白名单检查    — itemWhitelist 命中则跳过 4-9，只保留 2+7
4. 隐藏槽位标题行    — slotHeaders 逐槽位开关，slotHeaderWhitelist 例外
5. 合并多修饰符    — mergeModifiers 将同属性多行汇总为一行（CRT/KubeJS 兼容）
6. 隐藏属性修饰符    — hiddenAttributes 按注册名删除匹配行（含合并后的行）
7. 添加自定义文字    — customTooltips 在指定位置插入，支持多物品共用和颜色代码
8. 正则删除    — regexRemove 全局匹配 + perItemRegexRemove 按物品匹配
    regexRemoveWhitelist 白名单排除。最后执行，可删除前面任何结果
```

> 💡 关键点：`globalTooltip` 在白名单检查之前执行 → 白名单物品也能收到全局提示。`hiddenAttributes` 在合并之后执行 → 合并后的行一样可以被隐藏。`regexRemove` 在最后 → 就算自定义文字加上了，也能用正则删掉。

---

## 常见问题

**Q: 我改了配置但游戏里没变化？**
A: 部分配置支持热加载（改完文件切回游戏即可），建议重启游戏以确保所有配置生效。

**Q: 我在服务器玩，服务端需要装吗？**
A: 不需要。JETT 是纯客户端模组，只有你自己能看到效果。

**Q: 怎么知道物品的注册名？**
A: 按 F3+H 开启高级提示框，在背包里查看具体物品 ID。

**Q: 能对附魔书、药水等特殊物品生效吗？**
A: 可以，只要知道注册名（如 `minecraft:enchanted_book`），所有物品都支持。

**Q: 配置里的 "generic.xxx" 属性怎么来的？**
A: 这是 Minecraft 内部的属性标识符。配置文件注释里已经全部列出了原版 9 种属性，直接复制粘贴即可。如果安装了 CraftTweaker 的话用 `/ct dump attribute` 可以导出所有可用属性。

**Q: 合并多修饰符时，如果 CRT 加了乘法运算怎么办？**
A: 检测到乘法（MULTIPLY_BASE / MULTIPLY_TOTAL）会自动跳过该属性的合并，保留原样显示。乘法运算涉及 `(1+amount)` 连乘，简单求和结果不准确。

**Q: 合并后攻击伤害和攻击速度顺序反了？**
A: 不应该出现。合并最后一步会强制检查并交换——攻击伤害永远在攻击速度上面。

**Q: 怎么开启调试日志排查问题？**
A: 在 `[general]` 中设置 `debug = true`，游戏控制台会输出属性检测和合并的详细信息。平时保持关闭（`false`），避免日志刷屏。

**Q: 自定义文字可以用中文吗？**
A: 可以，UTF-8 编码支持所有语言。

**Q: 全局提示和按物品提示有什么区别？**
A: `globalTooltip` 是对所有物品统一添加，`customTooltips` 是指定物品单独添加。两者可以同时使用，全局提示先执行。

**Q: 怎么让一条配置覆盖多个物品？**
A: `customTooltips` 的 key 用逗号分隔即可：`"minecraft:diamond,diamond_block,emerald=0:§b★ Gem ★"`。`globalTooltip` 本身就是对所有物品生效，不需要指定物品。

**Q: 全局提示会不会覆盖自定义提示？**
A: 不会。两者独立执行，互不冲突。全局提示先插入，自定义提示后插入，可以在不同位置叠加。

**Q: 我只想给大部分物品加全局提示，但少数几个不要，怎么办？**
A: 用 `globalTooltipBlacklist` 把这些物品列入黑名单即可。

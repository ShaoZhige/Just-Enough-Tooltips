package com.shao.jett;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

/**
 * JETT (Just Enough Tooltips) configuration.
 * <p>
 * Config file: {@code config/jett-common.toml}
 * <p>
 * Features:
 * <ul>
 *   <li>Hide attribute modifiers from tooltips</li>
 *   <li>Merge multi-modifier attributes into a single line</li>
 *   <li>Hide equipment slot headers ("When in main hand:", etc.)</li>
 *   <li>Global tooltip text displayed on all items (with blacklist)</li>
 *   <li>Per-item custom tooltip text (with position control)</li>
 *   <li>Hide item display names</li>
 *   <li>Regex-based tooltip line removal</li>
 * </ul>
 *
 * @author Shao_Zhige
 */
public class Config {

    // ======================== [general] ========================

    /** Items completely excluded from JETT processing. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> itemWhitelist;

    /** Attribute registry paths to hide from tooltips (e.g. "generic.attack_damage") */
    public final ConfigValue<List<? extends String>> hiddenAttributes;

    /** Merge multiple modifiers of the same attribute into a single line showing the total */
    public final ConfigValue<Boolean> mergeModifiers;

    /** Items whose display names should be hidden. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> hideItemNames;

    /** Hide ALL item display names (use itemNameWhitelist to exclude specific items) */
    public final ConfigValue<Boolean> hideAllItemNames;

    /** Items to keep their names when hideAllItemNames is enabled */
    public final ConfigValue<List<? extends String>> itemNameWhitelist;

    /** Enable verbose debug logging in the game log */
    public final ConfigValue<Boolean> debug;

    // ======================== [globalTooltip] ========================

    /** Text displayed on every item's tooltip. Supports "N:text" position prefix */
    public final ConfigValue<String> globalTooltip;

    /** Items excluded from the global tooltip. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> globalTooltipBlacklist;

    // ======================== [slotHeaders] ========================

    public final ConfigValue<Boolean> hideSlotHeaderMainhand;
    public final ConfigValue<Boolean> hideSlotHeaderOffhand;
    public final ConfigValue<Boolean> hideSlotHeaderHead;
    public final ConfigValue<Boolean> hideSlotHeaderChest;
    public final ConfigValue<Boolean> hideSlotHeaderLegs;
    public final ConfigValue<Boolean> hideSlotHeaderFeet;

    /** Items that keep their slot header lines. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> slotHeaderWhitelist;

    // ======================== [customTooltips] ========================

    /** Per-item custom tooltip lines. Format: "key=[N:]text" (key supports comma-separated multi-item) */
    public final ConfigValue<List<? extends String>> customTooltips;

    // ======================== [regexRemove] ========================

    /** Regex patterns to remove matching tooltip lines (applied to all items) */
    public final ConfigValue<List<? extends String>> regexRemove;

    /** Items excluded from global regex removal */
    public final ConfigValue<List<? extends String>> regexRemoveWhitelist;

    /** Per-item regex removal. Format: "modid:item_name=regex pattern" */
    public final ConfigValue<List<? extends String>> perItemRegexRemove;

    // ================================================================

    Config(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "",
                "JETT (Just Enough Tooltips) Configuration",
                "",
                "A client-side mod that gives you full control over item tooltips.",
                "All features are optional — only enable what you need.",
                "",
                "Note: some settings support hot-reloading (switch to game after editing),",
                "but restarting the game is always recommended.",
                "",
                "纯客户端模组，所有功能均可独立开关。",
                "部分设置支持热重载，但建议每次改完配置后重启游戏。")
               .push("general");

        itemWhitelist = builder
                .comment(
                        "Items that JETT will NOT modify at all (keep everything as-is).",
                        "Format: \"modid:item_name\", one per line.",
                        "完全不受影响的物品白名单。格式：modid:item_name")
                .defineList("itemWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        hiddenAttributes = builder
                .comment(
                        "Attribute types to hide from tooltips.",
                        "Each entry is the registry path of an attribute.",
                        "Vanilla attributes:",
                        "  generic.attack_damage         - Attack Damage (攻击伤害)",
                        "  generic.attack_speed          - Attack Speed (攻击速度)",
                        "  generic.armor                 - Armor (护甲值)",
                        "  generic.armor_toughness       - Armor Toughness (盔甲韧性)",
                        "  generic.knockback_resistance  - Knockback Resistance (击退抗性)",
                        "  generic.max_health            - Max Health (最大生命值)",
                        "  generic.movement_speed        - Movement Speed (移动速度)",
                        "  generic.attack_knockback      - Attack Knockback (攻击击退)",
                        "  generic.luck                  - Luck (幸运)",
                        "",
                        "Mod-added attributes use the same format (e.g. \"epicfight.stun\" etc.)",
                        "其他模组的属性同样适用，填注册名即可。",
                        "",
                        "Example: [\"generic.attack_damage\", \"generic.armor\"]",
                        "Empty list = show everything. 空列表 = 全部显示。")
                .defineList("hiddenAttributes", Lists::newArrayList, obj -> obj instanceof String);

        mergeModifiers = builder
                .comment(
                        "Merge multiple modifiers of the same attribute into one line.",
                        "Inspired by NeoForge's enableMergedAttributeTooltips.",
                        "将同属性的多个修饰符合并为一行显示总值。",
                        "灵感来自 NeoForge。",
                        "  false = show each modifier separately (default)",
                        "  true  = merge into a single line")
                .define("mergeModifiers", false);

        hideItemNames = builder
                .comment(
                        "Hide the display name of specific items.",
                        "Format: \"modid:item_name\", one per line.",
                        "Use hideAllItemNames=true to hide ALL item names instead.",
                        "隐藏指定物品的名称。格式：modid:item_name")
                .defineList("hideItemNames", Lists::newArrayList, obj -> obj instanceof String);

        hideAllItemNames = builder
                .comment(
                        "Hide ALL item display names.",
                        "Use itemNameWhitelist to keep names on specific items.",
                        "隐藏所有物品名称，配合 itemNameWhitelist 排除特定物品。")
                .define("hideAllItemNames", false);

        itemNameWhitelist = builder
                .comment(
                        "Items that keep their names when hideAllItemNames is enabled.",
                        "Format: \"modid:item_name\", one per line.",
                        "在全局隐藏名称时保留指定物品的名称。")
                .defineList("itemNameWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        debug = builder
                .comment(
                        "Enable verbose debug logging.",
                        "When enabled, the complete tooltip processing pipeline is logged.",
                        "Zero performance cost when disabled. 零性能开销。")
                .define("debug", false);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Global tooltip text displayed on every item.",
                "",
                "Color codes (copy the § symbol directly):",
                "  §0=black  §1=dark_blue  §2=dark_green  §3=dark_aqua",
                "  §4=dark_red  §5=dark_purple  §6=gold  §7=gray",
                "  §8=dark_gray  §9=blue  §a=green  §b=aqua",
                "  §c=red  §d=light_purple  §e=yellow  §f=white",
                "  Style: §l=bold  §o=italic  §n=underline  §m=strikethrough  §k=obfuscated  §r=reset",
                "",
                "Format:",
                "  \"\"        = disabled",
                "  \"text\"    = append to end of every tooltip",
                "  \"N:text\"  = insert at line N (0 = first line)",
                "",
                "Examples:",
                "  §7§oRight-click to use         → appended",
                "  0:§e§l★ Server Item ★        → inserted at line 0",
                "",
                "全局提示：对所有物品显示的自定义文字（黑名单除外）。",
                "格式: \"text\"（追加）或 \"N:text\"（插入第N行）。空=禁用。")
               .push("globalTooltip");

        globalTooltip = builder
                .comment(
                        "Text displayed on every item's tooltip.",
                        "Empty string = disabled.",
                        "全局提示文本。空字符串 = 禁用。")
                .define("globalTooltip", "");

        globalTooltipBlacklist = builder
                .comment(
                        "Items excluded from the global tooltip.",
                        "Format: \"modid:item_name\", one per line.",
                        "Example: [\"minecraft:air\", \"minecraft:barrier\"]",
                        "全局提示黑名单。格式：modid:item_name")
                .defineList("globalTooltipBlacklist", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "Hide equipment slot header lines.",
                "All disabled by default — enable only what you need.",
                "",
                "Examples of slot headers:",
                "  \"When in main hand:\"  (item.modifiers.mainhand)",
                "  \"When in off hand:\"   (item.modifiers.offhand)",
                "  \"When on head:\"       (item.modifiers.head)",
                "  \"When on body:\"       (item.modifiers.chest)",
                "  \"When on legs:\"       (item.modifiers.legs)",
                "  \"When on feet:\"       (item.modifiers.feet)",
                "",
                "隐藏装备槽位标题行（如\"在主手时：\"等）。全部默认关闭。")
               .push("slotHeaders");

        hideSlotHeaderMainhand = builder
                .comment("Hide the main hand slot header.")
                .define("hideMainhand", false);
        hideSlotHeaderOffhand = builder
                .comment("Hide the off hand slot header.")
                .define("hideOffhand", false);
        hideSlotHeaderHead = builder
                .comment("Hide the head slot header.")
                .define("hideHead", false);
        hideSlotHeaderChest = builder
                .comment("Hide the chest slot header.")
                .define("hideChest", false);
        hideSlotHeaderLegs = builder
                .comment("Hide the legs slot header.")
                .define("hideLegs", false);
        hideSlotHeaderFeet = builder
                .comment("Hide the feet slot header.")
                .define("hideFeet", false);

        slotHeaderWhitelist = builder
                .comment(
                        "Items that keep their slot headers when hideSlot* is enabled.",
                        "Format: \"modid:item_name\", one per line.",
                        "槽位标题白名单。格式：modid:item_name")
                .defineList("slotHeaderWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Per-item custom tooltip text.",
                "",
                "Color codes (copy the § symbol directly):",
                "  §0=black  §1=dark_blue  §2=dark_green  §3=dark_aqua",
                "  §4=dark_red  §5=dark_purple  §6=gold  §7=gray",
                "  §8=dark_gray  §9=blue  §a=green  §b=aqua",
                "  §c=red  §d=light_purple  §e=yellow  §f=white",
                "  Style: §l=bold  §o=italic  §n=underline  §m=strikethrough  §k=obfuscated  §r=reset",
                "  Note: put color codes BEFORE style codes (e.g. §b§o works, §o§b may not)",
                "",
                "Format: \"key=[N:]text\"",
                "  key   = single item: \"modid:item_name\"",
                "         OR comma-separated: \"modid:a,modid:b,modid:c\"",
                "  N     = optional 0-based line number (omit to append at end)",
                "  text  = display text (supports § color/format codes)",
                "",
                "Examples:",
                "  \"minecraft:diamond_sword=1:§bRare\"",
                "  \"minecraft:diamond_sword=§b§o钻石制品\"",
                "  \"minecraft:diamond,emerald=0:§a★ Gem ★\"",
                "  \"minecraft:diamond_sword,netherite_sword=§cLegendary\"",
                "",
                "按物品的自定义提示文字。",
                "格式: \"key=[N:]text\"，key支持逗号分隔多物品。")
               .push("customTooltips");

        customTooltips = builder
                .comment(
                        "Add custom text lines to item tooltips.",
                        "Each entry matches one or more items and inserts/appends text.",
                        "添加自定义 tooltip 文字行。")
                .defineList("customTooltips", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Regex-based tooltip line removal.",
                "Uses Java regex patterns. Matcher.find() matches anywhere in the line.",
                "This runs LAST — it can delete lines added by customTooltips too.",
                "",
                "基于正则表达式的 tooltip 行删除。最后执行，可覆盖前面所有结果。")
               .push("regexRemove");

        regexRemove = builder
                .comment(
                        "Regex patterns applied to ALL tooltips.",
                        "Any tooltip line containing a match is removed.",
                        "Uses Matcher.find() — matches anywhere in the line.",
                        "Example: \"Attack Damage\" removes lines with that text.",
                        "全局正则，匹配任意物品。")
                .defineList("regexRemove", Lists::newArrayList, obj -> obj instanceof String);

        regexRemoveWhitelist = builder
                .comment(
                        "Items excluded from global regex removal.",
                        "Per-item regex (perItemRegexRemove) still applies.",
                        "Format: \"modid:item_name\", one per line.",
                        "全局正则白名单。格式：modid:item_name")
                .defineList("regexRemoveWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        perItemRegexRemove = builder
                .comment(
                        "Per-item regex removal.",
                        "Format: \"modid:item_name=regex pattern\"",
                        "Example: [\"minecraft:diamond_sword=Attack\"]",
                        "仍会执行按物品正则。",
                        "按物品的正则删除。格式: \"modid:item_name=regex\"")
                .defineList("perItemRegexRemove", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();
    }
}

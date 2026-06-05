package com.shao.jett;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TooltipHandler {

    private static void debugLog(String format, Object... args) {
        if (JETT.CONFIG.debug.get()) {
            JETT.LOGGER.info("[JETT-Debug] " + format, args);
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemName = rl != null ? rl.toString() : "<unknown>";
        debugLog("Processing tooltip for: {}", itemName);

        // 物品名称隐藏
        removeItemName(event, stack);

        // 全局 tooltip（所有物品，黑名单排除）
        addGlobalTooltip(event, stack);

        if (isWhitelisted(stack)) {
            debugLog("  Item is whitelisted, skipping most processing");
            addCustomTooltips(event, stack);
            return;
        }

        // 槽位标题行隐藏（先执行，独立于属性列表）
        removeSlotHeaders(event, stack);

        // 合并多修饰符（先执行，保证合并后的行不被误删）
        mergeAttributeModifiers(event, stack);

        // 根据 hiddenAttributes 配置收集要匹配的属性翻译名
        Set<String> hiddenNames = collectHiddenAttributeNames(stack);
        if (!hiddenNames.isEmpty()) {
            debugLog("  [hiddenAttributes] Hiding attribute names: {}", hiddenNames);
            // 文本匹配移除（属性名始终在行尾，用 endsWith 避免 "Armor" 误匹配 "Armor Toughness"）
            event.getToolTip().removeIf(line -> {
                String text = line.getString();
                for (String name : hiddenNames) {
                    if (text.endsWith(name)) return true;
                }
                return false;
            });
        }

        // 添加自定义 tooltip
        addCustomTooltips(event, stack);

        // 正则表达式删除（仿 CRT，最后执行以覆盖前面所有结果）
        removeByRegex(event, stack);
    }

    /** 移除 tooltip 第一行（物品名称） */
    private void removeItemName(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null || event.getToolTip().isEmpty()) return;

        String itemName = rl.toString();
        Config config = JETT.CONFIG;

        // 全局隐藏模式
        if (config.hideAllItemNames.get()) {
            if (!config.itemNameWhitelist.get().contains(itemName)) {
                String removed = event.getToolTip().get(0).getString();
                event.getToolTip().remove(0);
                debugLog("  [removeItemName] Global hide: removed \"{}\" from {}", removed, itemName);
            } else {
                debugLog("  [removeItemName] Whitelisted (global hide): {}", itemName);
            }
            return;
        }

        // 单物品隐藏模式
        if (config.hideItemNames.get().contains(itemName)) {
            String removed = event.getToolTip().get(0).getString();
            event.getToolTip().remove(0);
            debugLog("  [removeItemName] Specific hide: removed \"{}\" from {}", removed, itemName);
        }
    }

    /**
     * 全局 tooltip：对所有物品（黑名单除外）添加统一文字。
     *
     * 格式："text"（追加）或 "N:text"（插入第 N 行）。
     * 空字符串表示禁用。
     */
    private void addGlobalTooltip(ItemTooltipEvent event, ItemStack stack) {
        String raw = JETT.CONFIG.globalTooltip.get();
        if (raw.isEmpty()) return;

        ResourceLocation itemRl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRl == null) return;

        String itemName = itemRl.toString();
        List<? extends String> blacklist = JETT.CONFIG.globalTooltipBlacklist.get();
        if (!blacklist.isEmpty() && blacklist.contains(itemName)) {
            debugLog("  [globalTooltip] Skipped (blacklisted): {}", itemName);
            return;
        }

        int pos = -1;
        String text = raw;
        int colon = raw.indexOf(':');
        if (colon > 0) {
            try {
                pos = Integer.parseInt(raw.substring(0, colon));
                text = raw.substring(colon + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        if (text.isEmpty()) return;

        if (pos >= 0) {
            int target = Math.min(pos, event.getToolTip().size());
            event.getToolTip().add(target, new StringTextComponent(text));
        } else {
            event.getToolTip().add(new StringTextComponent(text));
        }
        debugLog("  [globalTooltip] Added '{}' at position {}", text, pos >= 0 ? pos : "end");
    }

    private void removeSlotHeaders(ItemTooltipEvent event, ItemStack stack) {
        // 槽位标题白名单检查
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl != null && JETT.CONFIG.slotHeaderWhitelist.get().contains(rl.toString())) {
            debugLog("  [removeSlotHeaders] Whitelisted: {}", rl);
            return;
        }

        Config config = JETT.CONFIG;
        List<String> removed = new ArrayList<>();
        event.getToolTip().removeIf(line -> {
            if (!(line instanceof TranslationTextComponent)) return false;
            String key = ((TranslationTextComponent) line).getKey();
            if (key.equals("item.modifiers.mainhand") && config.hideSlotHeaderMainhand.get()) { removed.add(key); return true; }
            if (key.equals("item.modifiers.offhand")  && config.hideSlotHeaderOffhand.get())  { removed.add(key); return true; }
            if (key.equals("item.modifiers.head")     && config.hideSlotHeaderHead.get())     { removed.add(key); return true; }
            if (key.equals("item.modifiers.chest")    && config.hideSlotHeaderChest.get())    { removed.add(key); return true; }
            if (key.equals("item.modifiers.legs")     && config.hideSlotHeaderLegs.get())     { removed.add(key); return true; }
            if (key.equals("item.modifiers.feet")     && config.hideSlotHeaderFeet.get())     { removed.add(key); return true; }
            if (key.equals("potion.whenDrank")                       && config.hidePotionEffectHeader.get()) { removed.add(key); return true; }
            return false;
        });
        if (!removed.isEmpty()) {
            debugLog("  [removeSlotHeaders] Removed: {}", removed);
        }
    }

    private boolean isWhitelisted(ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return rl != null && JETT.CONFIG.itemWhitelist.get().contains(rl.toString());
    }

    /**
     * 合并多修饰符为一行（灵感来自 NeoForge）。
     *
     * 通用：遍历所有属性 modifier，多修饰符则按路径合并。
     * 特殊：攻击伤害（+1 空手基础）、攻击速度（+4 空手基础）绿色无符号；
     *       其他属性蓝色/红色显示 +/- 符号。
     *       乘法运算检测到则跳过该属性。
     */
    private void mergeAttributeModifiers(ItemTooltipEvent event, ItemStack stack) {
        if (!JETT.CONFIG.mergeModifiers.get()) return;

        // 收集合并计划：path → (count, total, hasMultiply, translatedName)
        Map<String, Double> pathTotal = new LinkedHashMap<>();
        Map<String, Integer> pathCount = new LinkedHashMap<>();
        Map<String, Boolean> pathMultiply = new LinkedHashMap<>();
        Map<String, String> pathName = new LinkedHashMap<>();

        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            for (Map.Entry<Attribute, AttributeModifier> e : stack.getAttributeModifiers(slot).entries()) {
                ResourceLocation rl = e.getKey().getRegistryName();
                if (rl == null) continue;
                String path = rl.getPath();
                AttributeModifier.Operation op = e.getValue().getOperation();
                if (op == AttributeModifier.Operation.MULTIPLY_BASE
                        || op == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    debugLog("  [mergeModifiers] Skipping {} (multiply op, slot: {})", path, slot);
                    pathMultiply.put(path, true);
                    continue;
                }
                pathTotal.merge(path, e.getValue().getAmount(), Double::sum);
                pathCount.merge(path, 1, Integer::sum);
                pathName.computeIfAbsent(path, k ->
                        new TranslationTextComponent("attribute.name." + path).getString());
            }
        }

        // 构建合并计划，按 firstPos 降序（处理后面的不影响前面的）
        class Plan { String path; String name; double total; int pos; boolean isDefault; }
        List<Plan> plans = new ArrayList<>();
        for (String path : pathCount.keySet()) {
            if (pathCount.get(path) <= 1 || Boolean.TRUE.equals(pathMultiply.get(path))) continue;
            Plan p = new Plan();
            p.path = path;
            p.name = pathName.get(path);
            p.total = pathTotal.get(path);
            p.isDefault = path.equals("generic.attack_damage") || path.equals("generic.attack_speed");
            // 默认属性显示 = API 总和 + 基础值
            if (path.equals("generic.attack_damage")) p.total += 1;
            else if (path.equals("generic.attack_speed")) p.total += 4;
            // 找 tooltip 第一行位置（endsWith 避免 "Armor" 误匹配 "Armor Toughness"）
            p.pos = event.getToolTip().size();
            for (int i = 0; i < event.getToolTip().size(); i++) {
                String text = event.getToolTip().get(i).getString().trim();
                if (text.endsWith(p.name)) { p.pos = i; break; }
            }
            plans.add(p);
        }
        if (plans.isEmpty()) return;
        plans.sort((a, b) -> Integer.compare(b.pos, a.pos));

        debugLog("  [mergeModifiers] Merging {} attribute(s)...", plans.size());
        // 执行合并
        for (Plan p : plans) {
            debugLog("    {}: {} modifiers → total={}, isDefault={}", p.path, pathCount.get(p.path), p.total, p.isDefault);
            // 删旧行
            for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
                if (event.getToolTip().get(i).getString().trim().endsWith(p.name)) {
                    event.getToolTip().remove(i);
                }
            }
            // 构建合并行
            String numStr = formatMergedValue(p.total, p.isDefault);
            TextFormatting color = p.isDefault ? TextFormatting.DARK_GREEN
                    : (p.total >= 0 ? TextFormatting.BLUE : TextFormatting.RED);
            event.getToolTip().add(Math.min(p.pos, event.getToolTip().size()),
                    new StringTextComponent(color + numStr + " " + p.name));
        }

        // 攻击伤害必须在速度上面
        String dmgName = pathName.get("generic.attack_damage");
        String spdName = pathName.get("generic.attack_speed");
        if (dmgName != null && spdName != null) {
            int dmgIdx = -1, spdIdx = -1;
            for (int i = 0; i < event.getToolTip().size(); i++) {
                String text = event.getToolTip().get(i).getString().trim();
                if (dmgIdx < 0 && text.endsWith(dmgName)) dmgIdx = i;
                if (spdIdx < 0 && text.endsWith(spdName)) spdIdx = i;
            }
            if (dmgIdx >= 0 && spdIdx >= 0 && dmgIdx > spdIdx) {
                ITextComponent spd = event.getToolTip().remove(spdIdx);
                ITextComponent dmg = event.getToolTip().remove(dmgIdx > spdIdx ? dmgIdx - 1 : dmgIdx);
                event.getToolTip().add(spdIdx, dmg);
                event.getToolTip().add(dmgIdx, spd);
            }
        }
    }

    /**
     * 格式化合并后的数值。
     * 默认属性（绿色）带缩进；其他属性（蓝/红色）顶格显示 +/- 符号。
     */
    private static String formatMergedValue(double value, boolean isDefault) {
        if (isDefault) {
            String indent = " "; // 一个空格，匹配原版默认属性缩进
            if (value == (long) value) return indent + (long) value;
            return indent + String.format("%.1f", value);
        }
        // 非默认属性：顶格显示，正数加 +
        if (value == (long) value) {
            return (value >= 0 ? "+" : "") + (long) value;
        }
        return (value >= 0 ? "+" : "") + String.format("%.1f", value);
    }

    /**
     * 遍历物品所有槽位的属性修饰符，找出 registry path 在 hiddenAttributes
     * 配置列表中的属性，返回其翻译后名称（如 "攻击伤害"）。
     */
    private Set<String> collectHiddenAttributeNames(ItemStack stack) {
        List<? extends String> hidden = JETT.CONFIG.hiddenAttributes.get();
        if (hidden.isEmpty()) return new HashSet<>();

        Set<String> names = new HashSet<>();
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                ResourceLocation rl = entry.getKey().getRegistryName();
                if (rl != null && hidden.contains(rl.getPath())) {
                    String key = "attribute.name." + rl.getPath();
                    names.add(new TranslationTextComponent(key).getString());
                }
            }
        }
        return names;
    }

    /**
     * 解析配置中的自定义 tooltip，匹配物品注册名后插入到指定位置。
     *
     * 格式：key=[N:]文本
     *   key: 单个物品 (modid:item_name) 或逗号分隔多物品 (modid:a,modid:b)
     *   N: 可选行号（0-based），多条同位置按配置序排列，超出范围截断到末尾
     *   无 N: 追加到 tooltip 末尾（兼容旧格式）
     */
    private void addCustomTooltips(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation itemRl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRl == null) return;

        String itemName = itemRl.toString();
        List<? extends String> entries = JETT.CONFIG.customTooltips.get();

        // 分组收集：位置 → 文本列表（LinkedHashMap 保序）
        Map<Integer, List<String>> groups = new LinkedHashMap<>();
        List<String> appended = new ArrayList<>();

        for (String entry : entries) {
            int eq = entry.indexOf('=');
            if (eq <= 0 || eq >= entry.length() - 1) continue;

            String key = entry.substring(0, eq);
            if (!matchesItemKey(key, itemName)) continue;

            String raw = entry.substring(eq + 1);
            int colon = raw.indexOf(':');
            int pos = -1;
            String text = raw;

            // 解析可选位置前缀 "N:"
            if (colon > 0) {
                try {
                    pos = Integer.parseInt(raw.substring(0, colon));
                    if (pos < 0) pos = -1;
                    text = raw.substring(colon + 1);
                } catch (NumberFormatException ignored) {
                    // 不是数字 → 整段当文本
                }
            }
            if (text.isEmpty()) continue;

            if (pos >= 0) {
                groups.computeIfAbsent(pos, k -> new ArrayList<>()).add(text);
            } else {
                appended.add(text);
            }
        }

        // 按位置升序插入，累积偏移量处理同位置多条
        int offset = 0;
        for (int pos : groups.keySet().stream().sorted().collect(Collectors.toList())) {
            List<String> texts = groups.get(pos);
            int target = Math.min(pos + offset, event.getToolTip().size());
            List<ITextComponent> comps = texts.stream()
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList());
            event.getToolTip().addAll(target, comps);
            offset += texts.size();
        }

        // 末尾追加无位置前缀的条目
        for (String text : appended) {
            event.getToolTip().add(new StringTextComponent(text));
        }

        int total = groups.values().stream().mapToInt(List::size).sum() + appended.size();
        if (total > 0) {
            debugLog("  [customTooltips] Added {} line(s): groupPositions={}, appended={}",
                    total, groups.keySet().stream().sorted().collect(Collectors.toList()), appended.size());
        }
    }

    /**
     * 正则表达式删除 tooltip 行。
     *
     * 仿 CraftTweaker 的 ActionRemoveRegexTooltip 实现。
     *
     * CRT 中的原始调用链：
     *   1. ZenScript: item.removeTooltip("regex")
     *   2. → IIngredient.removeTooltip()                [api/item/IIngredient.java]
     *   3. → ActionRemoveRegexTooltip 构造，注册 ITooltipFunction
     *      [impl/actions/items/tooltips/ActionRemoveRegexTooltip.java]
     *   4. TooltipFunction 存入全局 Map
     *      → ActionTooltipBase.getTooltip()
     *      [impl/actions/items/tooltips/ActionTooltipBase.java]
     *   5. ItemTooltipEvent 触发时回调
     *      → CTClientEventHandler.handleTooltips()
     *      [impl/events/CTClientEventHandler.java]
     *
     * CRT 核心过滤逻辑（ActionRemoveRegexTooltip 第 20-29 行）：
     *   遍历 tooltip → MCTextComponent.getFormattedText() = ITextComponent.getString()
     *   → regex.matcher(text).find() → 匹配的行丢弃，不匹配的保留
     *   → tooltip.clear() + tooltip.addAll(filtered)
     *
     * JETT 复刻版：支持三种模式 — 全局正则、按物品正则、白名单排除。
     * 执行顺序：先检查白名单 → 有白名单则跳过全局正则 → 仍执行按物品正则。
     */
    private void removeByRegex(ItemTooltipEvent event, ItemStack stack) {
        Config config = JETT.CONFIG;
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemName = rl != null ? rl.toString() : "";

        // 收集所有要应用的正则
        List<Pattern> compiled = new ArrayList<>();

        // 按物品正则：仅当物品名匹配时才添加对应正则
        for (String entry : config.perItemRegexRemove.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0 || eq >= entry.length() - 1) continue;
            if (entry.substring(0, eq).equals(itemName)) {
                try {
                    compiled.add(Pattern.compile(entry.substring(eq + 1)));
                } catch (PatternSyntaxException e) {
                    JETT.LOGGER.error("[JETT] Invalid regex in perItemRegexRemove for item {}: \"{}\" — {}",
                            itemName, entry.substring(eq + 1), e.getMessage());
                }
            }
        }

        // 全局正则：仅当物品不在白名单中时才添加
        if (!config.regexRemoveWhitelist.get().contains(itemName)) {
            for (String pattern : config.regexRemove.get()) {
                try {
                    compiled.add(Pattern.compile(pattern));
                } catch (PatternSyntaxException e) {
                    JETT.LOGGER.error("[JETT] Invalid regex in regexRemove: \"{}\" — {}",
                            pattern, e.getMessage());
                }
            }
        }

        if (compiled.isEmpty()) return;

        // 仿 CRT 过滤逻辑
        List<ITextComponent> kept = new ArrayList<>();
        int removedCount = 0;
        for (ITextComponent line : event.getToolTip()) {
            String text = line.getString();
            boolean matched = false;
            for (Pattern p : compiled) {
                if (p.matcher(text).find()) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                kept.add(line);
            } else {
                removedCount++;
                debugLog("  [regexRemove] Removed line: \"{}\"", text);
            }
        }

        debugLog("  [regexRemove] {} pattern(s) compiled, removed {} line(s), kept {} line(s)",
                compiled.size(), removedCount, kept.size());

        event.getToolTip().clear();
        event.getToolTip().addAll(kept);
    }

    /**
     * 检查配置 key 是否匹配当前物品。
     * 支持逗号分隔的多物品 key（如 "minecraft:diamond,diamond_block,emerald"）。
     */
    private static boolean matchesItemKey(String configKey, String itemName) {
        if (configKey.isEmpty() || itemName.isEmpty()) return false;
        for (String part : configKey.split(",")) {
            if (part.trim().equals(itemName)) return true;
        }
        return false;
    }
}

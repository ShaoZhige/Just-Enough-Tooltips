package com.shao.jett;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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

    // ================================================================
    //  Main pipeline / 主处理管道
    // ================================================================

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemName = rl != null ? rl.toString() : "<unknown>";
        debugLog("Processing tooltip for: {}", itemName);

        removeItemName(event, stack);
        addGlobalTooltip(event, stack);

        if (isWhitelisted(stack)) {
            debugLog("  Item is whitelisted, skipping most processing");
            addCustomTooltips(event, stack);
            return;
        }

        removeSlotHeaders(event, stack);
        mergeAttributeModifiers(event, stack);
        hideAttributesByConfig(event, stack);
        addCustomTooltips(event, stack);
        removeByRegex(event, stack);
    }

    // ================================================================
    //  Step 1 — Hide item name / 隐藏物品名称
    // ================================================================

    private void removeItemName(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null || event.getToolTip().isEmpty()) return;

        String itemName = rl.toString();
        Config config = JETT.CONFIG;

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

        if (config.hideItemNames.get().contains(itemName)) {
            String removed = event.getToolTip().get(0).getString();
            event.getToolTip().remove(0);
            debugLog("  [removeItemName] Specific hide: removed \"{}\" from {}", removed, itemName);
        }
    }

    // ================================================================
    //  Step 2 — Global tooltip / 全局提示文字
    // ================================================================

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
            event.getToolTip().add(target, Component.literal(text));
        } else {
            event.getToolTip().add(Component.literal(text));
        }
        debugLog("  [globalTooltip] Added '{}' at position {}", text, pos >= 0 ? pos : "end");
    }

    // ================================================================
    //  Step 3 — Whitelist check / 白名单检查
    // ================================================================

    private boolean isWhitelisted(ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return rl != null && JETT.CONFIG.itemWhitelist.get().contains(rl.toString());
    }

    // ================================================================
    //  Step 4 — Hide slot headers / 隐藏槽位标题
    // ================================================================

    private void removeSlotHeaders(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl != null && JETT.CONFIG.slotHeaderWhitelist.get().contains(rl.toString())) {
            debugLog("  [removeSlotHeaders] Whitelisted: {}", rl);
            return;
        }

        Config config = JETT.CONFIG;
        List<String> removed = new ArrayList<>();
        event.getToolTip().removeIf(line -> {
            if (line.getContents() instanceof TranslatableContents tc) {
                String key = tc.getKey();
                if (key.equals("item.modifiers.mainhand") && config.hideSlotHeaderMainhand.get()) { removed.add(key); return true; }
                if (key.equals("item.modifiers.offhand")  && config.hideSlotHeaderOffhand.get())  { removed.add(key); return true; }
                if (key.equals("item.modifiers.head")     && config.hideSlotHeaderHead.get())     { removed.add(key); return true; }
                if (key.equals("item.modifiers.chest")    && config.hideSlotHeaderChest.get())    { removed.add(key); return true; }
                if (key.equals("item.modifiers.legs")     && config.hideSlotHeaderLegs.get())     { removed.add(key); return true; }
                if (key.equals("item.modifiers.feet")     && config.hideSlotHeaderFeet.get())     { removed.add(key); return true; }
                if (key.equals("item.minecraft.firework_rocket.flight") && config.hideFireworkFlight.get()) { removed.add(key); return true; }
                if (key.equals("potion.whenDrank") && config.hidePotionEffectHeader.get()) { removed.add(key); return true; }
            }
            return false;
        });
        if (!removed.isEmpty()) {
            debugLog("  [removeSlotHeaders] Removed: {}", removed);
        }
    }

    // ================================================================
    //  Step 5 — Merge multi-modifier attributes / 合并多修饰符
    // ================================================================

    private void mergeAttributeModifiers(ItemTooltipEvent event, ItemStack stack) {
        if (!JETT.CONFIG.mergeModifiers.get()) return;

        Map<String, Double> pathTotal = new LinkedHashMap<>();
        Map<String, Integer> pathCount = new LinkedHashMap<>();
        Map<String, Boolean> pathMultiply = new LinkedHashMap<>();
        Map<String, String> pathName = new LinkedHashMap<>();

        ItemAttributeModifiers attribs = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry e : attribs.modifiers()) {
            ResourceLocation rl = ForgeRegistries.ATTRIBUTES.getKey(e.attribute().value());
            if (rl == null) continue;
            String path = rl.getPath();
            AttributeModifier.Operation op = e.modifier().operation();
            if (op == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    || op == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                debugLog("  [mergeModifiers] Skipping {} (multiply op)", path);
                pathMultiply.put(path, true);
                continue;
            }
            pathTotal.merge(path, e.modifier().amount(), Double::sum);
            pathCount.merge(path, 1, Integer::sum);
            pathName.computeIfAbsent(path, k ->
                    Component.translatable("attribute.name." + k).getString());
        }

        class Plan { String path; String name; double total; int pos; boolean isDefault; }
        List<Plan> plans = new ArrayList<>();
        for (String path : pathCount.keySet()) {
            if (pathCount.get(path) <= 1 || Boolean.TRUE.equals(pathMultiply.get(path))) continue;
            Plan p = new Plan();
            p.path = path;
            p.name = pathName.get(path);
            p.total = pathTotal.get(path);
            p.isDefault = path.equals("generic.attack_damage") || path.equals("generic.attack_speed");
            if (path.equals("generic.attack_damage")) p.total += 1;
            else if (path.equals("generic.attack_speed")) p.total += 4;
            p.pos = event.getToolTip().size();
            for (int i = 0; i < event.getToolTip().size(); i++) {
                if (event.getToolTip().get(i).getString().trim().endsWith(p.name)) { p.pos = i; break; }
            }
            plans.add(p);
        }
        if (plans.isEmpty()) return;
        plans.sort((a, b) -> Integer.compare(b.pos, a.pos));

        debugLog("  [mergeModifiers] Merging {} attribute(s)...", plans.size());
        for (Plan p : plans) {
            debugLog("    {}: {} modifiers -> total={}, isDefault={}", p.path, pathCount.get(p.path), p.total, p.isDefault);
            for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
                if (event.getToolTip().get(i).getString().trim().endsWith(p.name)) {
                    event.getToolTip().remove(i);
                }
            }
            String numStr = formatMergedValue(p.total, p.isDefault);
            ChatFormatting color = p.isDefault ? ChatFormatting.DARK_GREEN
                    : (p.total >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED);
            event.getToolTip().add(Math.min(p.pos, event.getToolTip().size()),
                    Component.literal(color + numStr + " " + p.name));
        }

        // Ensure attack damage is above attack speed
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
                Component spd = event.getToolTip().remove(spdIdx);
                Component dmg = event.getToolTip().remove(dmgIdx > spdIdx ? dmgIdx - 1 : dmgIdx);
                event.getToolTip().add(spdIdx, dmg);
                event.getToolTip().add(dmgIdx, spd);
            }
        }
    }

    private static String formatMergedValue(double value, boolean isDefault) {
        if (isDefault) {
            String indent = " ";
            if (value == (long) value) return indent + (long) value;
            return indent + String.format("%.1f", value);
        }
        if (value == (long) value) {
            return (value >= 0 ? "+" : "") + (long) value;
        }
        return (value >= 0 ? "+" : "") + String.format("%.1f", value);
    }

    // ================================================================
    //  Step 6 — Hide attributes by config / 隐藏属性值
    // ================================================================

    private void hideAttributesByConfig(ItemTooltipEvent event, ItemStack stack) {
        Set<String> hiddenNames = collectHiddenAttributeNames(stack);
        if (hiddenNames.isEmpty()) return;

        debugLog("  [hiddenAttributes] Hiding attribute names: {}", hiddenNames);
        event.getToolTip().removeIf(line -> {
            String text = line.getString();
            for (String name : hiddenNames) {
                if (text.endsWith(name)) return true;
            }
            return false;
        });
    }

    private Set<String> collectHiddenAttributeNames(ItemStack stack) {
        List<? extends String> hidden = JETT.CONFIG.hiddenAttributes.get();
        if (hidden.isEmpty()) return new HashSet<>();

        Set<String> names = new HashSet<>();
        ItemAttributeModifiers attribs = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : attribs.modifiers()) {
            ResourceLocation rl = ForgeRegistries.ATTRIBUTES.getKey(entry.attribute().value());
            if (rl != null && hidden.contains(rl.getPath())) {
                names.add(Component.translatable("attribute.name." + rl.getPath()).getString());
            }
        }
        return names;
    }

    // ================================================================
    //  Step 7 — Custom tooltips / 自定义提示文字
    // ================================================================

    private void addCustomTooltips(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation itemRl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRl == null) return;

        String itemName = itemRl.toString();
        List<? extends String> entries = JETT.CONFIG.customTooltips.get();

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

            if (colon > 0) {
                try {
                    pos = Integer.parseInt(raw.substring(0, colon));
                    if (pos < 0) pos = -1;
                    text = raw.substring(colon + 1);
                } catch (NumberFormatException ignored) {
                }
            }
            if (text.isEmpty()) continue;

            if (pos >= 0) {
                groups.computeIfAbsent(pos, k -> new ArrayList<>()).add(text);
            } else {
                appended.add(text);
            }
        }

        int offset = 0;
        for (int pos : groups.keySet().stream().sorted().collect(Collectors.toList())) {
            List<String> texts = groups.get(pos);
            int target = Math.min(pos + offset, event.getToolTip().size());
            event.getToolTip().addAll(target, texts.stream().map(Component::literal).collect(Collectors.toList()));
            offset += texts.size();
        }

        for (String text : appended) {
            event.getToolTip().add(Component.literal(text));
        }

        int total = groups.values().stream().mapToInt(List::size).sum() + appended.size();
        if (total > 0) {
            debugLog("  [customTooltips] Added {} line(s): groupPositions={}, appended={}",
                    total, groups.keySet().stream().sorted().collect(Collectors.toList()), appended.size());
        }
    }

    private static boolean matchesItemKey(String configKey, String itemName) {
        if (configKey.isEmpty() || itemName.isEmpty()) return false;
        for (String part : configKey.split(",")) {
            if (part.trim().equals(itemName)) return true;
        }
        return false;
    }

    // ================================================================
    //  Step 8 — Regex removal / 正则删除
    // ================================================================

    private void removeByRegex(ItemTooltipEvent event, ItemStack stack) {
        Config config = JETT.CONFIG;
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemName = rl != null ? rl.toString() : "";

        List<Pattern> compiled = new ArrayList<>();

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

        List<Component> kept = new ArrayList<>();
        int removedCount = 0;
        for (Component line : event.getToolTip()) {
            String text = line.getString();
            boolean matched = false;
            for (Pattern p : compiled) {
                if (p.matcher(text).find()) { matched = true; break; }
            }
            if (!matched) {
                kept.add(line);
            } else {
                removedCount++;
                debugLog("  [regexRemove] Removed line: \"{}\"", text);
            }
        }

        debugLog("  [regexRemove] {} pattern(s), removed {} line(s), kept {} line(s)",
                compiled.size(), removedCount, kept.size());

        event.getToolTip().clear();
        event.getToolTip().addAll(kept);
    }
}

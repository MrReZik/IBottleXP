package me.mrrezik.ibottlexp.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // UUID -> (bottleId -> expiry timestamp)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    // Anti-abuse: UUID -> [count, second-timestamp]
    private final Map<UUID, long[]> antiAbuse = new HashMap<>();

    /**
     * Проверяет, есть ли активный кулдаун.
     * @return оставшееся время в секундах, или 0 если кулдауна нет
     */
    public long getRemainingCooldown(UUID uuid, String bottleId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        Long expiry = map.get(bottleId);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Устанавливает кулдаун для игрока на конкретную бутылочку.
     */
    public void setCooldown(UUID uuid, String bottleId, int seconds) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(bottleId, System.currentTimeMillis() + (seconds * 1000L));
    }

    /**
     * Глобальный кулдаун (независимо от бутылочки).
     */
    public long getRemainingGlobalCooldown(UUID uuid) {
        return getRemainingCooldown(uuid, "__global__");
    }

    public void setGlobalCooldown(UUID uuid, int seconds) {
        setCooldown(uuid, "__global__", seconds);
    }

    /**
     * Anti-abuse: возвращает true если игрок злоупотребляет (слишком много использований в секунду).
     */
    public boolean checkAntiAbuse(UUID uuid, int maxPerSecond) {
        long now = System.currentTimeMillis();
        long[] data = antiAbuse.getOrDefault(uuid, new long[]{0, now});

        // Если прошла секунда — сбрасываем счётчик
        if (now - data[1] >= 1000) {
            data[0] = 1;
            data[1] = now;
            antiAbuse.put(uuid, data);
            return false;
        }

        data[0]++;
        antiAbuse.put(uuid, data);
        return data[0] > maxPerSecond;
    }

    /**
     * Возвращает текущий счётчик использований за секунду.
     */
    public long getAbuseCount(UUID uuid) {
        long[] data = antiAbuse.get(uuid);
        return data == null ? 0 : data[0];
    }
}

package com.creeperface.nukkitx.colormatch.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.Location;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.*;
import cn.nukkit.plugin.Plugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CreeperFace
 */
public class BossBar {

    public static final int WITHER_ID = 52;

    private Plugin plugin;
    @Getter
    private Map<String, Player> players = new HashMap<>();
    public final long id;
    private int health = 1;
    private int maxHealth = 600;
    private String text = "";

    private EntityMetadata metadata;
    private NukkitRandom random = new NukkitRandom();

    public BossBar(Plugin plugin) {
        this.plugin = plugin;
        this.id = 2197383491L;
        this.metadata = (new EntityMetadata()).putString(Entity.DATA_NAMETAG, "").putLong(0, 196640L).putLong(38, -1L).putFloat(54, 0.0F).putFloat(55, 0.0F).putFloat(39, 0.0F);

        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, BossBar.this::update, 10, 10);
    }

    public void addPlayer(Player p) {
        this.players.put(p.getName().toLowerCase(), p);
        Location pos = p.add(this.getDirectionVector(p).normalize().multiply(-15));

        AddEntityPacket pk = new AddEntityPacket();
        pk.type = WITHER_ID;
        pk.entityRuntimeId = this.id;
        pk.entityUniqueId = this.id;
        pk.x = (float) pos.x;
        pk.y = (float) (pos.y - 7);
        pk.z = (float) pos.z;
        pk.speedX = 0;
        pk.speedY = 0;
        pk.speedZ = 0;
        pk.yaw = 0;
        pk.pitch = 0;
        pk.metadata = this.metadata;
        pk.attributes = new Attribute[]{Attribute.getAttribute(4).setMaxValue((float) this.maxHealth).setValue((float) this.getHealth())};

        UpdateAttributesPacket pk1 = new UpdateAttributesPacket();
        pk1.entityId = this.id;
        pk1.entries = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue((float) this.maxHealth).setValue((float) this.getHealth())};
        p.dataPacket(pk);
        p.dataPacket(pk1);

        BossEventPacket pk2 = new BossEventPacket();
        pk2.type = BossEventPacket.TYPE_SHOW;
        pk2.healthPercent = (float) health / maxHealth;
        pk2.title = this.text;
        pk2.bossEid = id;

        p.dataPacket(pk2);
    }

    public void removePlayer(Player p) {
        this.removePlayer(p.getName());
        if (p.isOnline()) {
            RemoveEntityPacket pk = new RemoveEntityPacket();
            pk.eid = this.id;
            p.dataPacket(pk);

            BossEventPacket pk2 = new BossEventPacket();
            pk2.bossEid = this.id;
            pk2.type = BossEventPacket.TYPE_HIDE;
            p.dataPacket(pk2);
        }

    }

    public void removePlayer(String name) {
        this.players.remove(name.toLowerCase());
    }

    public void update() {
        this.players.values().forEach(this::update);
    }

    public void update(Player p) {
        this.update(p, false);
    }

    public void update(Player p, boolean respawn) {
        Location pos = p.add(this.getDirectionVector(p).normalize().multiply(-15));

        MoveEntityAbsolutePacket pk2 = new MoveEntityAbsolutePacket();
        pk2.eid = this.id;
        pk2.x = (double) ((float) pos.x);
        pk2.y = (double) ((float) (pos.y - 30));
        pk2.z = (double) ((float) pos.z);
        pk2.yaw = (double) ((float) p.yaw);
        pk2.headYaw = (double) ((float) p.yaw);
        pk2.pitch = (double) ((float) p.pitch);

        p.dataPacket(pk2);
    }

    public void setHealth(int health) {
        this.health = Math.max(health, 1);
    }

    public void setMaxHealth(int health) {
        this.maxHealth = Math.max(health, 1);
    }

    public void updateText(String text) {
        this.text = text;
    }

    public void updateData() {
        BossEventPacket pk = new BossEventPacket();
        pk.type = BossEventPacket.TYPE_UPDATE;
        pk.healthPercent = (float) health / maxHealth;
        pk.title = this.text;
        pk.bossEid = id;

        plugin.getServer().batchPackets(players.values().toArray(new Player[0]), new DataPacket[]{pk});
    }

    public Vector3 getDirectionVector(Player p) {
        double pitch = 1.5707963267948966D;
        double yaw = (p.getYaw() + (double) this.random.nextRange(-10, 10) + 90) * 3.141592653589793D / 180.0D;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double z = Math.sin(pitch) * Math.sin(yaw);
        double y = Math.cos(pitch);
        return (new Vector3(x, y, z)).normalize();
    }

    public long getId() {
        return this.id;
    }

    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.team;

import com.spleefleague.gameapi.queue.RatedGameQueue;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 *
 * @author jonas
 */
public class TeamSpleefQueue extends RatedGameQueue<TeamSpleefArena, SpleefPlayer> {
    
    private final Collection<TeamSpleefArena> queuedArenas;
    private final Map<TeamSpleefArena, Map<Integer, Set<SpleefPlayer>>> queues;
    
    public TeamSpleefQueue(Consumer<Match> matchConsumer, Function<SpleefPlayer, Integer> ratingFunction) {
        super(matchConsumer, ratingFunction);
        this.queuedArenas = new HashSet<>();
        this.queues = new HashMap<>();
        Map<Integer, Set<SpleefPlayer>> defaultQueue = new HashMap<>();
        defaultQueue.put(null, new HashSet<>());
        this.queues.put(null, defaultQueue);
    }
    
    @Override
    public void registerArena(TeamSpleefArena arena) {
        if (arena.isQueued()) {
            this.queuedArenas.add(arena);
        }
        Map<Integer, Set<SpleefPlayer>> slots = new HashMap<>();
        if (arena.getTeamSizes() != null) {
            for (int i = 0; i < arena.getTeamSizes().length; i++) {
                slots.put(i, new HashSet<>());
            }
        }
        slots.put(null, new HashSet<>());
        this.queues.put(arena, slots);
    }

    @Override
    public void unregisterArena(TeamSpleefArena arena) {
        this.queuedArenas.remove(arena);
        this.queues.remove(arena);
    }

    @Override
    public void queuePlayer(SpleefPlayer player) {
        queuePlayer(player, null);
    }

    @Override
    public void queuePlayer(SpleefPlayer player, TeamSpleefArena queue) {
        dequeuePlayer(player);
        this.queues.get(queue).get(null).add(player);
    }
    
    public void queuePlayer(SpleefPlayer player, TeamSpleefArena queue, int slot) {
        if(slot >= queue.getTeamSizes().length || slot < 0) {
            throw new IllegalArgumentException(slot + " is not a valid slot for arena: " + queue.getName());
        }
        dequeuePlayer(player);
        this.queues.get(queue).get(slot).add(player);
    }

    @Override
    public void dequeuePlayer(SpleefPlayer player) {
        queues.values()
                .stream()
                .flatMap(m -> m.values().stream())
                .forEach(s -> s.remove(player));
    }

    @Override
    public boolean isQueued(SpleefPlayer player) {
        return queues.values()
                .stream()
                .flatMap(m -> m.values().stream())
                .anyMatch(s -> s.contains(player));
    }

    @Override
    public Collection<TeamSpleefArena> getRegisteredArenas() {
        return queuedArenas;
    }
    
    public Map<TeamSpleefArena, Set<SpleefPlayer>> getArenaQueues() {
        return queues
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue()
                            .values()
                            .stream()
                            .flatMap(s -> s.stream())
                            .collect(Collectors.toSet())
                ));
    }
    
    public Map<TeamSpleefArena, Map<Integer, Set<SpleefPlayer>>> getTeamQueues() {
        return queues;
    }
    
    @Override
    protected Match nextMatch(boolean forceFullTeam) {
        if(!forceFullTeam) return null;
        List<TeamSpleefArena> arenas = new ArrayList<>(getRegisteredArenas());
        Collections.shuffle(arenas);
        return arenas
                .stream()
                .map(this::findMatchInArenaQueue)
                .filter(m -> m != null)
                .findFirst()
                .orElse(null);
    }
    
    private Match findMatchInArenaQueue(TeamSpleefArena arena) {
        if(arena == null) return null;
        if (!queues.containsKey(arena)) return null;
        int freePlayers = queues.get(arena).get(null).size() + queues.get(null).get(null).size();
        int[] required = arena.getTeamSizes();
        if (required == null) return null;
        for (int i = 0; freePlayers >= 0 && i < required.length; i++) {
            int queued = queues.get(arena).getOrDefault(i, Collections.emptySet()).size();
            if(required[i] > queued) {
                freePlayers -= required[i] - queued;
            }
        }
        if(freePlayers < 0) return null;
        List<SpleefPlayer> matchPlayers = new ArrayList<>();
        for (int i = 0; i < required.length; i++) {
            int slotSizeLeft = required[i];
            slotSizeLeft -= addFromQueue(queues.get(arena).get(i), matchPlayers, slotSizeLeft);
            if(slotSizeLeft == 0) continue;
            slotSizeLeft -= addFromQueue(queues.get(arena).get(null), matchPlayers, slotSizeLeft);
            if(slotSizeLeft == 0) continue;
            slotSizeLeft -= addFromQueue(queues.get(null).get(null), matchPlayers, slotSizeLeft);
            if(slotSizeLeft > 0) {
                throw new RuntimeException("Not enough players found to create match!");
            }
            else if(slotSizeLeft < 0) {
                throw new RuntimeException("Broken queueing algorithm!");
            }
        }
        return new Match(arena, matchPlayers);
    }
    
    private int addFromQueue(Set<SpleefPlayer> source, List<SpleefPlayer> target, int amount) {
        Set<SpleefPlayer> players = source
                .stream()
                .limit(amount)
                .collect(Collectors.toSet());
        source.removeAll(players);
        target.addAll(players);
        return players.size();
    }
}

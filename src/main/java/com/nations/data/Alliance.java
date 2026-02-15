package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class Alliance {
    private String name;
    private String leaderNation;
    private Set<String> members = new HashSet<>();
    private Set<String> pendingInvites = new HashSet<>();

    public Alliance(String name, String leaderNation) {
        this.name = name;
        this.leaderNation = leaderNation;
        this.members.add(leaderNation);
    }

    public String getName() { return name; }
    public String getLeaderNation() { return leaderNation; }
    public Set<String> getMembers() { return members; }
    public Set<String> getPendingInvites() { return pendingInvites; }

    public void addMember(String nation) { members.add(nation); }
    public void removeMember(String nation) { members.remove(nation); }
    public boolean hasMember(String nation) { return members.contains(nation); }

    public void invite(String nation) { pendingInvites.add(nation); }
    public boolean hasInvite(String nation) { return pendingInvites.contains(nation); }
    public void removeInvite(String nation) { pendingInvites.remove(nation); }

    public boolean areAllied(String nation1, String nation2) {
        return members.contains(nation1) && members.contains(nation2);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("leader", leaderNation);

        JsonArray membersArr = new JsonArray();
        for (String m : members) membersArr.add(m);
        json.add("members", membersArr);

        JsonArray invArr = new JsonArray();
        for (String i : pendingInvites) invArr.add(i);
        json.add("invites", invArr);

        return json;
    }

    public static Alliance fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String leader = json.get("leader").getAsString();
        Alliance a = new Alliance(name, leader);
        a.members.clear();
        for (var el : json.getAsJsonArray("members")) a.members.add(el.getAsString());
        if (json.has("invites")) {
            for (var el : json.getAsJsonArray("invites")) a.pendingInvites.add(el.getAsString());
        }
        return a;
    }
}

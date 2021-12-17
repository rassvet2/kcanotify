package com.antest1.kcanotify;

import static com.antest1.kcanotify.KcaApiData.STYPE_AO;
import static com.antest1.kcanotify.KcaApiData.STYPE_AV;
import static com.antest1.kcanotify.KcaApiData.STYPE_BBV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CAV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CL;
import static com.antest1.kcanotify.KcaApiData.STYPE_CLT;
import static com.antest1.kcanotify.KcaApiData.STYPE_CT;
import static com.antest1.kcanotify.KcaApiData.STYPE_CV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVB;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVE;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVL;
import static com.antest1.kcanotify.KcaApiData.STYPE_DD;
import static com.antest1.kcanotify.KcaApiData.STYPE_DE;
import static com.antest1.kcanotify.KcaApiData.T2_ANTISUB_PATROL;
import static com.antest1.kcanotify.KcaApiData.T2_ANTI_AIR_DEVICE;
import static com.antest1.kcanotify.KcaApiData.T2_AUTOGYRO;
import static com.antest1.kcanotify.KcaApiData.T2_BOMBER;
import static com.antest1.kcanotify.KcaApiData.T2_FIGHTER;
import static com.antest1.kcanotify.KcaApiData.T2_GUN_LARGE;
import static com.antest1.kcanotify.KcaApiData.T2_GUN_LARGE_II;
import static com.antest1.kcanotify.KcaApiData.T2_GUN_MEDIUM;
import static com.antest1.kcanotify.KcaApiData.T2_GUN_SMALL;
import static com.antest1.kcanotify.KcaApiData.T2_MACHINE_GUN;
import static com.antest1.kcanotify.KcaApiData.T2_RADAR_LARGE;
import static com.antest1.kcanotify.KcaApiData.T2_RADAR_SMALL;
import static com.antest1.kcanotify.KcaApiData.T2_RADER_LARGE_II;
import static com.antest1.kcanotify.KcaApiData.T2_SANSHIKIDAN;
import static com.antest1.kcanotify.KcaApiData.T2_SEA_BOMBER;
import static com.antest1.kcanotify.KcaApiData.T2_SEA_SCOUT;
import static com.antest1.kcanotify.KcaApiData.T2_SONAR;
import static com.antest1.kcanotify.KcaApiData.T2_SONAR_LARGE;
import static com.antest1.kcanotify.KcaApiData.T2_SUB_GUN;
import static com.antest1.kcanotify.KcaApiData.T2_TORPEDO_BOMBER;
import static com.antest1.kcanotify.KcaApiData.getKcShipDataById;
import static com.antest1.kcanotify.KcaApiData.getUserItemStatusById;
import static com.antest1.kcanotify.KcaApiData.getUserShipDataById;
import static com.antest1.kcanotify.KcaConstants.FORMATION_C1;
import static com.antest1.kcanotify.KcaConstants.FORMATION_C2;
import static com.antest1.kcanotify.KcaConstants.FORMATION_C3;
import static com.antest1.kcanotify.KcaConstants.FORMATION_C4;
import static com.antest1.kcanotify.KcaConstants.FORMATION_DEF;
import static com.antest1.kcanotify.KcaConstants.FORMATION_DIA;
import static com.antest1.kcanotify.KcaConstants.FORMATION_DLN;
import static com.antest1.kcanotify.KcaConstants.FORMATION_ECH;
import static com.antest1.kcanotify.KcaConstants.FORMATION_LAB;
import static com.antest1.kcanotify.KcaConstants.FORMATION_LAH;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

import android.content.Context;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// TODO: replace all "slot_ex" with "exslot"

/**
 * all hard-coded ids are copied from @kcwiki/kancolle-data
 *
 * @see <a href="https://github.com/kcwiki/kancolle-data/blob/master/api/api_start2.json">@kcwiki/kancolle-data</a>
 */
public class KcCalc {

    private static boolean isInitialized = false;
    private static Gson gson = new Gson();

    // region common
    public static void init(Context context) {

        isInitialized = true;
    }

    private static void checkIsInitialized() {
        if (!isInitialized) throw new IllegalStateException("KcCalc has not been initialized");
    }

    /**
     * check json object {@code json} contains all of {@code keys}
     *
     * @param json json object to check
     * @param keys keys to check
     * @throws IllegalArgumentException if json is null or there are missing keys
     */
    private static void require(JsonObject json, String... keys) {
        if (json == null) {
            throw new IllegalArgumentException("json is null");
        }

        List<String> missing = null;
        for (String key : keys) {
            if (!json.has(key)) {
                missing = missing != null ? missing : new ArrayList<>();
                missing.add(key);
            }
        }

        if (missing != null && !missing.isEmpty() /* always */) {
            String msg = "required keys missing, missing: " + missing + ", "
                    + "required: " + Arrays.toString(keys) + ", "
                    + "json: " + json;
            throw new IllegalArgumentException(msg);
        }
    }

    private static boolean any(int x, int... args) {
        for (int y : args) {
            if (x == y) return true;
        }
        return false;
    }

    /**
     * @param userData required: "slot", "slot_ex"
     * @return an unmodifiable id list that iterates for each slot and slot_ex
     */
    private static SlotItemList slots(JsonObject userData) {

        return new SlotItemList(userData);
    }

    /**
     * @param userData     required: "slot", "slot_ex"
     * @param userDataKeys comma separated keys to extract from user/member data
     * @param kcDataKeys   comma separated keys to extract from kc/master data
     * @return an unmodifiable json list, skips id == -1 (empty slot) and some error occurred. <br>
     * contains null when {@code KcaApiData#getUserItemStatusById(int, String, String)} returns null.
     * @see KcaApiData#getUserItemStatusById(int, String, String)
     */
    private static Iterable<JsonObject> slots(JsonObject userData, String userDataKeys, String kcDataKeys) {

        return slots(userData).query(userDataKeys, kcDataKeys);
    }

    public static final class SlotItemList extends AbstractList<Integer> {

        private final int[] slot;
        private final int slot_ex;

        /**
         * @param userData required: "slot", "slot_ex"
         */
        public SlotItemList(JsonObject userData) {
            slot = userData.has("slot")
                    ? gson.fromJson(userData.getAsJsonArray("slot"), int[].class)
                    : new int[0];
            slot_ex = userData.has("slot_ex") ? userData.get("slot_ex").getAsInt() : 0 /* not opened */;
        }

        @Override
        public Integer get(int i) {
            if (i < slot.length) return slot[i];
            if (slot_ex != 0) return slot_ex;
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return slot.length + (slot_ex != 0 ? 1 : 0 /* not opened */);
        }

        public Iterable<Integer> noEmpty() {
            return Iterables.filter(this, id -> id != -1);
        }

        public Iterable<JsonObject> query(String userDataKeys, String kcDataKeys) {
            return Iterables.transform(noEmpty(), id -> getUserItemStatusById(id, userDataKeys, kcDataKeys));
        }

        /**
         * @return true if any slot item matches to one of types
         */
        public boolean any(int... types) {
            return Iterables.any(query("", "type"), item -> KcCalc.any(item.get("type").getAsInt(), types));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("SlotItemList [");
            for (Iterator<Integer> iter = iterator(); iter.hasNext(); ) {
                int item_id = iter.next();
                sb.append(item_id != -1 ? String.valueOf(item_id) : "empty");
                if (iter.hasNext()) sb.append(", ");
            }
            return sb.append("]").toString();
        }
    }
    // endregion

    // region AA
    // region generic anti-air calculation
    public static double getAdjustedAA(int user_ship_id) {
        return getAdjustedAA(getUserShipDataById(user_ship_id, "slot,slot_ex,tyku"));
    }

    /**
     * Calculate adjusted anti-air score (加重対空)
     *
     * @param userData required: "slot", "slot_ex", "tyku",
     * @return adjusted AA score
     * @see <a href="https://wikiwiki.jp/kancolle/%E5%AF%BE%E7%A9%BA%E7%A0%B2%E7%81%AB#AntiAircraft">対空砲火(wikiwiki)</a>
     * @see <a href="https://kancolle.fandom.com/wiki/Combat/Aerial_Combat#System_Mechanics">Aerial Combat Stage 2(fandom)</a>
     * @see <a href="https://en.kancollewiki.net/Aerial_Combat#Adjusted_Anti-Air">Adjusted Anti-Air(kancollewiki)</a>
     */
    public static double getAdjustedAA(JsonObject userData) {
        require(userData, "slot", "slot_ex", "tyku");

        // aaa means adjusted anti-air value
        double aaa_score = userData.get("tyku").getAsInt();

        boolean has_item = false;
        for (JsonObject itemData : slots(userData, "level", "type,tyku")) {
            has_item = true;
            if (itemData == null) continue;

            int type2 = itemData.getAsJsonArray("type").get(2).getAsInt(); // for type name
            int type3 = itemData.getAsJsonArray("type").get(3).getAsInt(); // for icon
            int item_aa = itemData.get("tyku").getAsInt();
            int level = itemData.get("level").getAsInt();

            double itemFactor = getItemFactorForAdjustedAA(type2, type3);
            double improvementFactor = getImprovementFactorForAdjustedAA(type2, type3, item_aa);
            aaa_score += itemFactor * item_aa + improvementFactor * sqrt(level);
        }

        // round down to n or 2n
        int a = has_item ? 2 : 1;
        return a * floor(aaa_score / a);
    }

    private static double getItemFactorForAdjustedAA(int type2, int type3) {
        if (type2 == T2_MACHINE_GUN) return 6;
        if (type3 == 16 /* AA_GUN */ || type2 == T2_ANTI_AIR_DEVICE) return 4;
        if (type2 == T2_RADAR_SMALL || type2 == T2_RADAR_LARGE || type2 == T2_RADER_LARGE_II)
            return 3;
        return 0;
    }

    private static double getImprovementFactorForAdjustedAA(int type2, int type3, int item_aa) {
        if (type2 == T2_MACHINE_GUN) {
            return (item_aa >= 8) ? 6 : 4;
        }
        if (type3 == 16 /* AA_GUN */) {
            return (item_aa >= 8) ? 3 : 2;
        }
        if (type2 == T2_ANTI_AIR_DEVICE) {
            return 2;
        }
        return 0;
    }

    /**
     * Calculate fleet anti-air score (艦隊防空)
     *
     * @param fleet     user ship id list of the fleet
     * @param formation the formation
     * @return fleet aa score
     * @see <a href="https://wikiwiki.jp/kancolle/%E5%AF%BE%E7%A9%BA%E7%A0%B2%E7%81%AB#AntiAircraft">対空砲火(wikiwiki)</a>
     * @see <a href="https://kancolle.fandom.com/wiki/Combat/Aerial_Combat#System_Mechanics">Aerial Combat Stage 2(fandom)</a>
     * @see <a href="https://en.kancollewiki.net/Aerial_Combat#Adjusted_Anti-Air">Adjusted Anti-Air(kancollewiki)</a>
     * @see KcaConstants#FORMATION_LAH
     */
    public static double getFleetAA(int[] fleet, int formation) {

        double sum_of_bonus = 0.0;

        for (int user_ship_id : fleet) {
            JsonObject userData = getUserShipDataById(user_ship_id, "slot,slot_ex");

            double aa_bonus = 0.0;

            for (JsonObject itemData : slots(userData, "level", "id,type,tyku")) {
                if (itemData == null) continue;

                int kc_item_id = itemData.get("id").getAsInt();
                int type2 = itemData.getAsJsonArray("type").get(2).getAsInt(); // for type name
                int type3 = itemData.getAsJsonArray("type").get(3).getAsInt(); // for icon
                int item_aa = itemData.get("tyku").getAsInt();
                int level = itemData.get("level").getAsInt();

                double itemFactor = getItemFactorForFleetAA(type2, type3, kc_item_id);
                double improvementFactor = getImprovementFactorForFleetAA(type2, type3, item_aa);
                aa_bonus += itemFactor * item_aa + improvementFactor * sqrt(level);
            }

            sum_of_bonus += floor(aa_bonus);
        }

        return floor(getFormationFactorForFleetAA(formation) * sum_of_bonus) * 2.0 / 1.3;
    }

    private static double getItemFactorForFleetAA(int type2, int type3, int kc_item_id) {
        if (type2 == T2_SANSHIKIDAN) {
            return 0.6;
        }

        if (type2 == T2_RADAR_SMALL || type2 == T2_RADAR_LARGE || type2 == T2_RADER_LARGE_II) {
            return 0.4;
        }

        if (type3 == 16 /* AA_GUN */ || type2 == T2_ANTI_AIR_DEVICE) return 0.35;
        if (kc_item_id == 9 /* 46cm三連装砲 */) return 0.25;

        if (any(type2, T2_GUN_SMALL, T2_GUN_MEDIUM, T2_GUN_LARGE, T2_GUN_LARGE_II, T2_SUB_GUN,
                T2_MACHINE_GUN, T2_FIGHTER, T2_BOMBER, T2_SEA_SCOUT)) {
            return 0.2;
        }

        return 0;
    }

    private static double getImprovementFactorForFleetAA(int type2, int type3, int item_aa) {
        if (type3 == 16 /* AA_GUN */) {
            return (item_aa >= 8) ? 3 : 2;
        }
        if (type2 == T2_ANTI_AIR_DEVICE) {
            return 2;
        }
        if (type2 == T2_RADAR_SMALL || type2 == T2_RADAR_LARGE || type2 == T2_RADER_LARGE_II) {
            return 1.5;
        }
        return 0;
    }

    private static double getFormationFactorForFleetAA(int formation) {
        switch (formation) {
            case FORMATION_DLN: // 複縦陣
                return 1.2;
            case FORMATION_DIA: // 輪形陣
                return 1.6;
            case FORMATION_DEF: // 警戒神
                return 1.1;
            case FORMATION_C1:
                return 1.1;
            case FORMATION_C3:
                return 1.5;
            case FORMATION_LAH: // 単縦陣
            case FORMATION_ECH: // 梯形陣
            case FORMATION_LAB: // 単横陣
            case FORMATION_C2:
            case FORMATION_C4:
            default:
                return 1.0;
        }
    }
    // endregion

    /**
     * Calculate the probability of the Anti-Air Cut-Ins (対空CI)
     *
     * @return
     */
    public static double getAACIRate() {
        // TODO
    }

    /**
     * Calculate the probability of the Anti-Air Propellant Barrage (対空噴進弾幕)
     * <p>
     * trigger_rate = (0.9 * luck + Adjusted_AA) / 281 + launchers_bonus + ise_class_bonus
     * launchers_bonus = (n - 1) * 0.15 // where n is number of equipped launcher
     * ise_class_bonus = 25 if Ise-Class (Hyuga-Kai, Kaini, Ise-Kai, Kaini)
     *
     * @param user_ship_id user/member ship id
     * @return the Anti-Air Propellant Barrage trigger rate
     * @see <a href="https://wikiwiki.jp/kancolle/12cm30%E9%80%A3%E8%A3%85%E5%99%B4%E9%80%B2%E7%A0%B2%E6%94%B9%E4%BA%8C#n67c3285">12cm30連装噴進砲改二(wikiwiki)</a>
     * @see <a href="https://kancolle.fandom.com/wiki/12cm_30-tube_Rocket_Launcher_Kai_Ni">12cm 30-tube Rocket Launcher Kai Ni(fandom)</a>
     * @see <a href="https://en.kancollewiki.net/Aerial_Combat#Rocket_Barrage">Anti-Air Rocket Barrage(kancollewiki)</a>
     */
    public static double getAAPBTriggerRate(int user_ship_id) {
        JsonObject userData = getUserShipDataById(user_ship_id, "ship_id,slot,slot_ex,luck,tyku");
        int kc_ship_id = userData.get("ship_id").getAsInt();
        JsonObject kcData = getKcShipDataById(kc_ship_id, "stype,ctype");

        if (kcData == null) return 0.0;

        if (!canTriggerAAPB(kcData.get("stype").getAsInt())) {
            return 0.0;
        }

        boolean isIseClass = kcData.get("ctype").getAsInt() == 2; /* Ise-class */

        int launchers = 0;
        for (JsonObject itemData : slots(userData, "", "id")) {

            if (itemData != null && itemData.get("id").getAsInt() == 274 /* 12cm30連想噴進砲改二 */)
                launchers++;
        }

        if (launchers == 0) return 0.0;

        int luck = userData.get("luck").getAsInt();
        double adjustedAA = getAdjustedAA(userData);

        return (0.9 * luck + adjustedAA) / 281 + (launchers - 1) * 0.15 + (isIseClass ? 0.25 : 0);
    }

    private static boolean canTriggerAAPB(int stype) {
        // BBV: 航空戦艦, CAV: 航空巡洋艦
        // CVL: 軽空母, CV: 正規空母
        // CVB: 正規空母, CVE: 護衛空母
        // AV: 水上機母艦
        return any(stype, STYPE_BBV, STYPE_CAV, STYPE_CVL, STYPE_CV, STYPE_CVB, STYPE_CVE, STYPE_AV);
    }

    // endregion

    // region ASW
    /**
     * check the ship can Opening Anti-Submarine Warfare Shelling (対潜先制爆雷攻撃)
     *
     * @param userData required: "ship_id", "slot", "slot_ex", "taisen")
     * @param kcData   required: "stype", "ctype"
     * @return true if the ship can Opening ASW
     * @see <a href="https://wikiwiki.jp/kancolle/%E5%AF%BE%E6%BD%9C%E6%94%BB%E6%92%83#trigger_conditions">対潜先制爆雷攻撃(wikiwiki)</a>
     * @see <a href="https://en.kancollewiki.net/Combat/Battle_Opening#Opening_ASW_Shelling_(OASW)">Opening ASW(kancollewiki)</a>
     */
    public static boolean canOpeningASW(JsonObject userData, JsonObject kcData) {
        require(userData, "ship_id", "slot", "slot_ex", "taisen");
        require(kcData, "stype", "ctype");

        int kc_ship_id = userData.get("ship_id").getAsInt();
        if (canAlwaysOASW(kc_ship_id)) return true;

        int stype = kcData.get("stype").getAsInt();
        int ship_asw = userData.get("taisen").getAsInt();
        SlotItemList slots = slots(userData);
        boolean has_sonar = slots.any(T2_SONAR, T2_SONAR_LARGE);

        if (isOASWBorder100(stype) && ship_asw >= 100 && has_sonar) {
            return true;
        }

        if (stype == STYPE_DE) {
            if (has_sonar && ship_asw >= 60) return true;

            int sum_of_equip_asw = 0;
            for (JsonObject itemData : slots.query("", "tais")) {
                sum_of_equip_asw += itemData.get("tais").getAsInt();
            }

            // [incorrect] I think it should include bonus asw, but IDK how to do it.
            // bonus asw does not include to it. see wikiwiki > bottom of table > 3rd item > 1st item

            if (sum_of_equip_asw >= 4) return true;
        }

        if (isTaiyouClass(kc_ship_id)) {

            for (JsonObject itemData : slots.query("", "type,tais")) {
                if (isAircraftOASWable(itemData, true, 1)) return true;
            }
        }

        if (stype == STYPE_CVL && !isAttackerCVL(kc_ship_id)) {
            if (ship_asw >= 50 && has_sonar) {
                for (JsonObject itemData : slots.query("", "type,tais")) {
                    if (isAircraftOASWable(itemData, false, 7)) return true;
                }
            }
            if (ship_asw >= 65) {
                for (JsonObject itemData : slots.query("", "type,tais")) {
                    if (isAircraftOASWable(itemData, false, 7)) return true;
                }
            }
            if (ship_asw >= 100 && has_sonar) {
                for (JsonObject itemData : slots.query("", "type,tais")) {
                    if (isAircraftOASWable(itemData, true, 1)) return true;
                }
            }
        }

        if (kc_ship_id == 554 /* 日向改二 */) {
            int autogyro_score = 0;
            for (JsonObject itemData : slots.query("", "id,type")) {
                autogyro_score += isAutogyro(itemData);
            }

            if (autogyro_score >= 2) return true;
        }

        if (isSeaBomberOASWable(kc_ship_id) && ship_asw >= 100 && has_sonar) {
            for (JsonObject itemData : slots.query("", "type")) {
                int type2 = itemData.getAsJsonArray("type").get(2).getAsInt();
                if (type2 == T2_SEA_BOMBER || type2 == T2_ANTISUB_PATROL) return true;
            }
        }

        return false;
    }

    private static boolean canAlwaysOASW(int kc_ship_id) {

        // TODO: add when new ship added who can innately OASW
        // consider replace with ctype check when new ship (e.g. new J-class or Fletcher-class)
        // but I don't know how to judge kai/kai-ni of the ship
        return any(
                kc_ship_id,
                141, // 五十鈴改二
                478, // 龍田改二
                624, // 夕張改二丁
                394, 893, // Jervis改, Janus改
                561, 681, // Samuel B.Roberts/改
                596, 692, 628, 629, // Fletcher/改/改 Mod.2/Mk.II
                562, 689 // Johnston/改
        );
    }

    /**
     * @param stype ship type
     * @return true if OASW Border of the ship type is 100.
     */
    private static boolean isOASWBorder100(int stype) {
        return any(stype, STYPE_DD, STYPE_CL, STYPE_CT, STYPE_CLT, STYPE_AO);
    }

    /**
     * @return if the ship is Taiyou Kai/Kai-ni, Shinyou Kai/Kai-ni, Kaga Kai-ni Go,
     */
    private static boolean isTaiyouClass(int kc_ship_id) {

        // also contains Kaga Kai-ni Go
        return any(
                kc_ship_id,
                380, 529, // 大鷹改/改二
                381, 536, // 神鷹改/改二
                646 // 加賀改二護
        );
    }

    private static boolean isAircraftOASWable(JsonObject itemData, boolean includeBomber, int asw_border) {
        require(itemData, "type,tais");

        int type2 = itemData.getAsJsonArray("type").get(2).getAsInt();

        if (type2 == T2_TORPEDO_BOMBER || (includeBomber && type2 == T2_BOMBER)) {
            return itemData.get("tais").getAsInt() >= asw_border;
        }

        return type2 == T2_ANTISUB_PATROL || type2 == T2_AUTOGYRO;
    }

    private static boolean isAttackerCVL(int kc_ship_id) {

        return kc_ship_id == 508 /* 鈴谷航改二 */ || kc_ship_id == 509 /* 熊野航改二 */;
    }

    /**
     * @return 1 if auto-gyro, 2 if helicopter, otherwise 0
     */
    private static int isAutogyro(JsonObject itemData) {
        require(itemData, "id", "type");

        int id = itemData.get("id").getAsInt();
        int type2 = itemData.getAsJsonArray("type").get(2).getAsInt();

        if (type2 == T2_AUTOGYRO) {
            if (id == 326 /* S-51J */ || id == 327 /* S-51J改 */) {
                return 2;
            } else {
                return 1;
            }
        }

        return 0;
    }

    private static boolean isSeaBomberOASWable(int kc_ship_id) {

        return any(
                kc_ship_id,
                411, // 扶桑改二
                412, // 山城改二
                626 // 神州丸改
        );
    }

    // endregion

    public static final class NightCI {

        public NightCI(

        )
    }

    /**
     * @param user_ship_id
     * @return
     * @see <a href="https://wikiwiki.jp/kancolle/%E5%A4%9C%E6%88%A6#nightcutin1">夜戦</a>
     * @see <a href="https://kancolle.fandom.com/wiki/Combat/Night_Battle">Night Battle</a>
     */
    public static double getNightCIRate(
            int user_ship_id,
            ) {
        JsonObject userData = getUserShipDataById(user_ship_id, "ship_id,lv,luck,slot,slot_ex");
        int kc_ship_id = userData.get("ship_id").getAsInt();
        int stype = getKcShipDataById(kc_ship_id, "stype").get("stype").getAsInt();
    }

    private static double getNightCIRate(double base, int bonus, int factor) {
        return (base + bonus) / factor;
    }

    private static void can

    /**
     * @param isFlagship              is the activator flagship?
     * @param isDamaged               is the activator middle damaged?
     * @param isFriendSearchLight     is friend fleet searchlight activated?
     * @param isEnemySearchLight      is enemy fleet searchlight activated?
     * @param isFriendStarShell       is friend fleet star-shell activated?
     * @param isEnemyStarShell        is enemy fleet star-shell activated?
     * @param hasSkilledLookouts      is the skilled lookouts (熟練見張員) equipped?
     * @param hasTorpedoSquadLookouts is the torpedo squadron skilled lookouts (水雷戦隊熟練見張員) equipped?
     * @return night ci bonus
     */
    private static int getNightCIBonus(
            boolean isFlagship,
            boolean isDamaged,
            boolean isFriendSearchLight,
            boolean isEnemySearchLight,
            boolean isFriendStarShell,
            boolean isEnemyStarShell,
            boolean hasSkilledLookouts,
            boolean hasTorpedoSquadLookouts
    ) {
        int bonus = 0;
        if (isFlagship) bonus += 15;
        if (isDamaged) bonus += 18;
        if (isFriendSearchLight) bonus += 7;
        if (isEnemySearchLight) bonus -= 5;
        if (isFriendStarShell) bonus += 4;
        if (isEnemyStarShell) bonus -= 10;
        if (hasSkilledLookouts) bonus += 5;
        if (hasTorpedoSquadLookouts) bonus += 9;
        return bonus;
    }

    /**
     * @param lv   the level of the invoker
     * @param luck the luck of the invoker
     * @return night ci term of formula
     */
    private static double getNightCITerm(int lv, int luck) {
        if (luck < 50) {
            return (double) floor(15 + luck + 0.75 * sqrt(lv));
        } else {
            return (double) floor(15 + 50 + sqrt(luck - 50) + 0.8 * sqrt(lv));
        }
    }
}

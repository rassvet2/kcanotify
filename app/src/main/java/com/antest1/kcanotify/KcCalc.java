package com.antest1.kcanotify;

import static com.antest1.kcanotify.KcaApiData.STYPE_AO;
import static com.antest1.kcanotify.KcaApiData.STYPE_AV;
import static com.antest1.kcanotify.KcaApiData.STYPE_BBV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CAV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CV;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVB;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVE;
import static com.antest1.kcanotify.KcaApiData.STYPE_CVL;
import static com.antest1.kcanotify.KcaApiData.T2_ANTI_AIR_DEVICE;
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
import static com.antest1.kcanotify.KcaApiData.T2_SEA_SCOUT;
import static com.antest1.kcanotify.KcaApiData.T2_SUB_GUN;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class KcCalc {

    private static boolean isInitialized = false;
    private static Gson gson = new Gson();

    public static void init(Context context) {

        isInitialized = true;
    }

    private static void checkIsInitialized() {
        if (!isInitialized) throw new IllegalStateException("KcCalc has not been initialized");
    }

    // region generic anti-air calculation

    public static double getAdjustedAA(int user_ship_id) {
        return getAdjustedAA(getUserShipDataById(user_ship_id, "slot,slot_ex,tyku"));
    }

    /**
     * Calculate adjusted anti-air score (加重対空)
     *
     * @param userData required: "slot", "slot_ex", "tyku",
     * @return adjusted AA score
     */
    public static double getAdjustedAA(JsonObject userData) {
        int[] slot = gson.fromJson(userData.get("slot"), int[].class);
        int slot_ex = userData.get("slot_ex").getAsInt();
        int ship_aa = userData.get("tyku").getAsInt();

        // aaa means adjusted anti-air value
        double aaa_score = ship_aa;

        boolean has_item = false;
        for (int i = 0; i < slot.length + 1; i++) {
            int item_id = (i < slot.length) ? slot[i] : slot_ex;

            if (item_id == -1) continue;
            has_item = true;

            JsonObject itemData = getUserItemStatusById(item_id, "level", "type,tyku");
            if (itemData == null) continue;

            int type2 = itemData.getAsJsonArray("type").get(2).getAsInt(); // for type name
            int type3 = itemData.getAsJsonArray("type").get(3).getAsInt(); // for icon
            int item_aa = itemData.get("tyku").getAsInt();
            int level = itemData.get("level").getAsInt();

            double itemFactor = getItemFactorForAdjustedAA(type2, type3);
            double improvementFactor = getImprovementFactorForAdjustedAA(type2, type3, item_aa);
            aaa_score += itemFactor * item_aa + improvementFactor * sqrt(level);
        }

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
     * @param fleet user ship id list of the fleet
     * @param formation the formation
     * @see KcaConstants#FORMATION_LAH
     * @return fleet aa score
     */
    public static double getFleetAA(int[] fleet, int formation) {

        double sum_of_bonus = 0.0;

        for (int user_ship_id : fleet) {
            JsonObject userData = getUserShipDataById(user_ship_id, "slot,slot_ex");

            int[] slot = gson.fromJson(userData.get("slot"), int[].class);
            int slot_ex = userData.get("slot_ex").getAsInt();

            double aa_bonus = 0.0;

            for (int i = 0; i < slot.length + 1; i++) {
                int user_item_id = (i < slot.length) ? slot[i] : slot_ex;

                if (user_item_id == -1) continue;

                JsonObject itemData = getUserItemStatusById(user_item_id, "level", "id,type,tyku");
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

        if (type2 == T2_GUN_SMALL || type2 == T2_GUN_MEDIUM || type2 == T2_GUN_LARGE
                || type2 == T2_GUN_LARGE_II || type2 == T2_SUB_GUN || type2 == T2_MACHINE_GUN
                || type2 == T2_FIGHTER || type2 == T2_BOMBER || type2 == T2_SEA_SCOUT) {
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
     *
     * @param user_ship_id
     * @return
     */
    public static double getAAPBRate(int user_ship_id) {
        JsonObject userData = getUserShipDataById(user_ship_id, "ship_id,slot,slot_ex,luck,tyku,");
        int kc_ship_id = userData.get("ship_id").getAsInt();
        JsonObject kcData = getKcShipDataById(kc_ship_id, "stype,ctype");

        if (kcData == null) return 0.0;

        switch (kcData.get("stype").getAsInt()) {
            case STYPE_BBV: // 航空戦艦
            case STYPE_CAV: // 航空巡洋艦
            case STYPE_CVL: // 軽空母
            case STYPE_CV: // 正規空母
            case STYPE_CVB: // 正規空母
            case STYPE_CVE: // 護衛空母
            case STYPE_AV: // 水上機母艦
                break;
            default:
                return 0.0;
        }

        boolean isIseClass = kcData.get("ctype").getAsInt() == 2; /* Ise-class */

        int[] slot = gson.fromJson(userData.get("slot"), int[].class);
        int slot_ex = userData.get("slot_ex").getAsInt();

        int launchers = 0;
        for (int i = 0; i < slot.length + 1; i++) {
            int user_item_id = (i < slot.length) ? slot[i] : slot_ex;

            if (user_item_id == -1) continue;

            int kc_item_id = getUserItemStatusById(user_item_id, "", "id").get("id").getAsInt();

            if (kc_item_id == 274 /* 12cm30連想噴進砲改二 */) launchers++;
        }

        if (launchers == 0) return 0.0;

        int luck = userData.get("luck").getAsInt();
        double adjustedAA = getAdjustedAA(userData);

        return ((0.9 * luck + adjustedAA) / 281 + (launchers - 1) * 15 + (isIseClass ? 25 : 0)) / 100.0;
    }

    public static boolean canOpeningASW() {

    }

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

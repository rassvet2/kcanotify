package com.antest1.kcanotify;

import androidx.annotation.NonNull;

import com.google.common.base.Joiner;

import java.util.Objects;

/**
 * the property name is {@code {PREFIX}_{NAME}}
 * where {@code PREFIX} is such as {@code Item/Ship/MstItem/MstShip}
 * and {@code NAME} is what does the value of {@code api_{KEY}} means
 * <br>
 * the value is the keys of the json of kancolle data. all of them are removed the {@code api_} prefix.
 *
 * <p>
 * <pre>
 * {@code
 * JSON.stringify(Object.fromEntries(Object.entries(temp1.model).map(([k, v]) => [k.substring(1), v]).filter(([k,v]) => ['ship', 'slot'].includes(k))))
 * }
 * {@code
 * jq '.ship._map[]._o' < kcdump.json | jq -s > mem_ship.json
 * jq '.ship._mapMst[]._o' < kcdump.json | jq -s > mst_ship.json
 * jq '.ship._map[]._o' < kcdump.json | jq -s > mem_ship.json
 * }
 * {@code
 * \n?^\s+([-0-9]+,?)$\n?
 * }</pre>
 * they will helps you...
 * </p>
 *
 * @see <a href="https://kancolletool.github.io/docs/api">Api Documentation (outdated?)</a>
 * @see <a href="https://github.com/kcwiki/kancolle-data/blob/master/api/api_start2.ts">api_start2 responce type</a>
 * @see <a href="https://github.com/KagamiChan/kcsapi.ts/blob/master/api_get_member/ship3/response.ts">api_get_member/ship3 responce type (outdated?)</a>
 * @see <a href="https://github.com/KagamiChan/kcsapi.ts/blob/master/api_get_member/slot_item/response.ts">api_get_member/slot_item responce type (outdated?)</a>
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class KcJsonKeys {

    // region mem_item
    /**
     * value: {@code number/int}
     */
    public static final String Item_Id = "id";
    /**
     * value: {@code number/int}
     */
    public static final String Item_Level = "level";
    /**
     * value: {@code number/int}, optional
     */
    public static final String Item_AirLv = "alv";
    /**
     * value: {@code number/int}
     */
    public static final String Item_Locked = "locked";
    /**
     * value: {@code number/int}
     */
    public static final String Item_MstId = "slotitem_id";
    // endregion

    // region mst_item
    /**
     * value: {@code number}, unused
     */
    public static final String MstItem_Atap = "atap";
    /**
     * value: {@code number}, unused
     */
    public static final String MstItem_Baku = "bakk";
    /**
     * value: {@code number/int}, bomber
     */
    public static final String MstItem_BomberAtk = "baku";
    /**
     * value: {@code number[4]}
     */
    public static final String MstItem_DisassemblyMaterial = "broken";
    /**
     * value: {@code number/int}, optional, aircraft-only
     */
    public static final String MstItem_LABCost = "cost";
    /**
     * value: {@code number/int}, optional, aircraft-only
     */
    public static final String MstItem_LABRadius = "distance";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Firepower = "houg";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Evasion = "houk";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Accuracy = "houm";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Id = "id";
    /**
     * value: {@code string}, unused
     */
    public static final String MstItem_Info = "info";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Range = "leng";
    /**
     * value: {@code number/int}, unused
     */
    public static final String MstItem_Luck = "luck";
    /**
     * value: {@code string}
     */
    public static final String MstItem_Name = "name";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Torpedo = "raig";
    /**
     * value: {@code number/int}, unused
     */
    public static final String MstItem_Raik = "raik";
    /**
     * value: {@code number/int}, used some items (e.g. radar, enemy items, aircraft) but unknown usage
     * TODO: what does it mean?
     */
    public static final String MstItem_Raim = "raim";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Rarity = "rare";
    /**
     * value: {@code number/int}, unused
     */
    public static final String MstItem_Sakb = "sakb";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_LOS = "saku";
    /**
     * value: {@code number/int}, unused
     */
    public static final String MstItem_Speed = "soku";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_SortNo = "sortno";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_Armor = "souk";
    /**
     * value: {@code number/int}, unused
     */
    public static final String MstItem_HP = "taik";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_ASW = "tais";
    /**
     * value: {@code number/int}
     */
    public static final String MstItem_AA = "tyku";
    /**
     * value: {@code number/int[5]} {@code [category, card?, type, icon, plane?]}
     *
     * <pre>{@code
     * Object.values(temp1.model.slot._mapMst)
     *   .filter(({ _o }) => _o.api_type[0] == 1) // change here
     *   .map((x) => [x._o.api_type[2], x])
     *   .reduce((acc, [k, v]) => { acc[k] ??= []; acc[k].push(v); return acc }, {})
     * }</pre>
     * <p>
     * known:
     * category: 1: guns and subguns, 2: torpedos, 3: carrier-based aircrafts,
     * 4: special bullets and machine guns, 5: scouts, sea-(scout, bomber, fighter), (small, large)radars
     * and so on, so on, so on...
     * type: {@link KcaApiData#T2_GUN_SMALL} etc.
     * icon: {@link R.mipmap#item_1} etc.
     */
    public static final String MstItem_Type = "type";
    /**
     * value: {@code string/int}, unused
     */
    public static final String MstItem_UseBull = "usebull";
    /**
     * value: {@code number}, optional, unknown usage
     * TODO: what does it mean?
     */
    public static final String MstItem_Version = "version";
    // endregion

    // region mem_ship
    /**
     * value: {@code number}
     */
    public static final String Ship_BGRarity = "backs";
    /**
     * value: {@code number}
     */
    public static final String Ship_RemBull = "bull";
    /**
     * value: {@code number}
     */
    public static final String Ship_Cond = "cond";
    /**
     * value: {@code number[total, next, ?]}
     */
    public static final String Ship_Exp = "exp";
    /**
     * value: {@code number}
     */
    public static final String Ship_RemFuel = "fuel";
    /**
     * value: {@code number}
     */
    public static final String Ship_Id = "id";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_Evasion = "kaihi";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_Firepower = "karyoku";
    /**
     * value: {@code number[firepower, torpedo, anti-air, armor, luck, hp, asw]}, unknown usage
     */
    public static final String Ship_Modernization = "kyouka";
    /**
     * value: {@code number}
     */
    public static final String Ship_Range = "leng";
    /**
     * value: {@code number}
     */
    public static final String Ship_Locked = "locked";
    /**
     * value: {@code number}
     */
    public static final String Ship_LockedEquip = "locked_equip";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_Luck = "lucky";
    /**
     * value: {@code number}
     */
    public static final String Ship_Lv = "lv";
    /**
     * value: {@code number}
     */
    public static final String Ship_MaxHP = "maxhp";
    /**
     * value: {@code number[steel, fuel]}
     */
    public static final String Ship_RepairCost = "ndock_item";
    /**
     * value: {@code number}
     */
    public static final String Ship_RepairTime = "ndock_time";
    /**
     * value: {@code number}
     */
    public static final String Ship_NowHP = "nowhp";
    /**
     * value: {@code number[5]}
     */
    public static final String Ship_SlotRem = "onslot";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_Torpedo = "raisou";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_LOS = "sakuteki";
    /**
     * value: {@code number}, optional
     */
    public static final String Ship_SallyArea = "sally_area";
    /**
     * value: {@code number}
     */
    public static final String Ship_MstId = "ship_id";
    /**
     * value: {@code number[5]}
     */
    public static final String Ship_Slot = "slot";
    /**
     * value: {@code number}
     */
    public static final String Ship_SlotEx = "slot_ex";
    /**
     * value: {@code number}
     */
    public static final String Ship_SlotNum = "slotnum";
    /**
     * value: {@code number}
     */
    public static final String Ship_Speed = "soku";
    /**
     * value: {@code number}
     */
    public static final String Ship_SortNo = "sortno";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_Armor = "soukou";
    /**
     * value: {@code number}
     */
    public static final String Ship_Rarity = "srate";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_AA = "taiku";
    /**
     * value: {@code number[cur, max]}
     */
    public static final String Ship_ASW = "taisen";
    // endregion

    // region mst_ship
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_UpgradeBullCost = "afterbull";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_UpgradeFuelCost = "afterfuel";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_UpgradeReqLv = "afterlv";
    /**
     * value: {@code string}, optional
     */
    public static final String MstShip_UpgradeDestId = "aftershipid";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_Rarity = "backs";
    /**
     * value: {@code number[4]}, optional
     */
    public static final String MstShip_DeconstructionMaterial = "broken";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_BuildTime = "buildtime";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_BullMax = "bull_max";
    /**
     * value: {@code number}
     */
    public static final String MstShip_Class = "ctype";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_FuelMax = "fuel_max";
    /**
     * value: {@code string}, optional
     */
    public static final String MstShip_EncountMsg = "getmes";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_Firepower = "houg";
    /**
     * value: {@code number}
     */
    public static final String MstShip_Id = "id";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_Range = "leng";
    /**
     * value: {@code number[min, ?]}, optional, 2nd may not be a max
     */
    public static final String MstShip_Luck = "luck";
    /**
     * value: {@code number[5]}, optional
     */
    public static final String MstShip_SlotCapacity = "maxeq";
    /**
     * value: {@code string}
     */
    public static final String MstShip_Name = "name";
    /**
     * value: {@code number[4]}, optional
     */
    public static final String MstShip_ModernizationGain = "powup";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_Torpedo = "raig";
    /**
     * value: {@code number}
     */
    public static final String MstShip_SlotCount = "slot_num";
    /**
     * value: {@code number}
     */
    public static final String MstShip_Speed = "soku";
    /**
     * value: {@code number}
     */
    public static final String MstShip_SortId = "sort_id";
    /**
     * value: {@code number}, optional
     */
    public static final String MstShip_SortNo = "sortno";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_Armor = "souk";
    /**
     * value: {@code number}
     */
    public static final String MstShip_ShipType = "stype";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_HP = "taik";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_ASW = "tais";
    /**
     * value: {@code number[min, max]}, optional
     */
    public static final String MstShip_AA = "tyku";
    /**
     * value: {@code number}, optional, unknown usage
     * TODO: what does it mean?
     */
    public static final String MstShip_VoiceF = "voicef";
    /**
     * value: {@code string}
     */
    public static final String MstShip_Reading = "yomi";
    // endregion

    // region utilities
    private static final String DECKINFO_REQ_LIST = csl(
            Ship_Id, Ship_MstId, Ship_Lv, Ship_Exp,
            Ship_Slot, Ship_SlotEx, Ship_SlotRem,
            Ship_Cond, Ship_MaxHP, Ship_NowHP, Ship_SallyArea,
            Ship_Luck, Ship_AA, Ship_ASW);

    private static final Joiner CslJoiner = Joiner.on(',');

    /**
     * make arguments into comma-separated list
     */
    public static String csl(String... args) {
        return Objects.requireNonNull(CslJoiner).join(args);
    }
    // endregion
}

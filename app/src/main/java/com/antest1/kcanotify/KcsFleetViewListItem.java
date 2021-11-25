package com.antest1.kcanotify;

import static com.antest1.kcanotify.KcaApiData.getShipTranslation;
import static com.antest1.kcanotify.KcaApiData.getShipTypeAbbr;
import static com.antest1.kcanotify.KcaConstants.PREF_KCA_HP_FORMAT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;

import java.util.Locale;

public class KcsFleetViewListItem extends LinearLayout {
    private static final String TAG = "FleetViewItem";

    private final LinearLayout container;
    private final TextView tv_name;
//    private final TextView tv_condmark;
    private final TextView tv_stype;
    private final TextView tv_lv;
    private final TextView tv_exp;
    private final TextView tv_hp;
    private final TextView tv_cond;

    private final SharedPreferences pref;

    private static final String[] hp_format_list = {
            "HP %1$d/%2$d",         // HP 35/37
            "HP %1$d/%2$d +%3$s %4$s",  // HP 35/37 +2 3:24
            "HP %1$d+%3$s/%2$d %4$s",   // HP 35+2/37 3:24
            "HP %1$d/%2$d +%3$s",   // HP 35/37 +2
            "HP %1$d+%3$s/%2$d",    // HP 35+2/37
            "HP %1$d/%2$d %4$s"     // HP 35/37 3:24
    };

    private ShipInfo info;

    private boolean isAkashiActive = false;

    public KcsFleetViewListItem(Context context) {
        this(context, null);
    }

    public KcsFleetViewListItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KcsFleetViewListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_fleet_list_item, this);

//        container = findViewById(R.id.container);
        container = this;
        tv_name = findViewById(R.id.name);
//        tv_condmark = findViewById(R.id.condmark);
        tv_stype = findViewById(R.id.stype);
        tv_lv = findViewById(R.id.lv);
        tv_exp = findViewById(R.id.exp);
        tv_hp = findViewById(R.id.hp);
        tv_cond = findViewById(R.id.cond);

        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
    }

    /**
     * @param data maindata.get(i).getAsJsonObject()
     */
    public void setData(JsonObject data) {
        setData(jsonToShipInfo(data));
    }
    public void setData(ShipInfo info) {
        this.info = info;

        Context appContext = getContext().getApplicationContext();

        tv_name.setText(getShipTranslation(info.name, false));
        tv_stype.setText(getShipTypeAbbr(info.stype));
        tv_stype.setBackgroundColor(ContextCompat.getColor(appContext, R.color.transparent));
        tv_stype.setTextColor(ContextCompat.getColor(appContext, R.color.colorAccent));

        if (info.sally_area > 0) {
            int colorRes = KcaUtils.getId("colorStatSallyArea".concat(String.valueOf(info.sally_area)), R.color.class);
            tv_stype.setBackgroundColor(ContextCompat.getColor(appContext, colorRes));
            tv_stype.getBackground().setAlpha(192);
            tv_stype.setTextColor(ContextCompat.getColor(appContext, R.color.white));
        }

        tv_lv.setText(makeLvString(info.lv));
        tv_exp.setText(makeExpString(info.exp));
//        tv_hp.setText(makeHpString(info.now_hp, info.max_hp));
        updateHP();

        int cond = info.cond;
        tv_cond.setText(makeCondString(cond));
        if (cond > 49) {
            tv_cond.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetShipKira));
            tv_cond.setTextColor(ContextCompat.getColor(appContext, R.color.colorPrimaryDark));
        } else if (cond / 10 >= 4) {
            tv_cond.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetInfoBtn));
            tv_cond.setTextColor(ContextCompat.getColor(appContext, R.color.white));
        } else if (cond / 10 >= 3) {
            tv_cond.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetInfoBtn));
            tv_cond.setTextColor(ContextCompat.getColor(appContext, R.color.colorFleetShipFatigue1));
        } else if (cond / 10 == 2) {
            tv_cond.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetShipFatigue1));
            tv_cond.setTextColor(ContextCompat.getColor(appContext, R.color.white));
        } else {
            tv_cond.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetShipFatigue2));
            tv_cond.setTextColor(ContextCompat.getColor(appContext, R.color.white));
        }

        int now_hp = info.now_hp, max_hp = info.max_hp;
        if (now_hp * 4 <= max_hp) {
            container.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetWarning));
            tv_hp.setTextColor(ContextCompat.getColor(appContext, R.color.colorHeavyDmgState));
        } else if (now_hp * 2 <= max_hp) {
            container.setBackgroundColor(Color.TRANSPARENT);
            tv_hp.setTextColor(ContextCompat.getColor(appContext, R.color.colorModerateDmgState));
        } else if (now_hp * 4 <= max_hp * 3) {
            container.setBackgroundColor(Color.TRANSPARENT);
            tv_hp.setTextColor(ContextCompat.getColor(appContext, R.color.colorLightDmgState));
        } else if (now_hp != max_hp) {
            container.setBackgroundColor(Color.TRANSPARENT);
            tv_hp.setTextColor(ContextCompat.getColor(appContext, R.color.colorNormalState));
        } else {
            container.setBackgroundColor(Color.TRANSPARENT);
            tv_hp.setTextColor(ContextCompat.getColor(appContext, R.color.colorFullState));
        }

        if (KcaDocking.checkShipInDock(info.ship_id)) {
            container.setBackgroundColor(ContextCompat.getColor(appContext, R.color.colorFleetInRepair));
        }

        container.setVisibility(View.VISIBLE);
    }

    public void setAkashiTimer(boolean isActive) {
        isAkashiActive = isActive;
        if (info != null) updateHP();
    }

    private void updateHP() {
        int now = info.now_hp, max = info.max_hp;
        int lv = info.lv, stype = info.stype;
        int damage = max - now;
        if (isAkashiActive && damage > 0 && now * 2 > max) {

            int elapsed = KcaAkashiRepairInfo.getAkashiElapsedTimeInSecond();
            int repaired = Math.min(damage, KcaDocking.getRepairedHp(lv, stype, elapsed));
            int next = (repaired < damage) ? KcaDocking.getNextRepair(lv, stype, elapsed) : 0;

            String str = String.format(
                    Locale.ENGLISH,
                    hp_format_list[getHPFormatId()],
                    info.now_hp, info.max_hp, repaired,
                    KcaUtils.getTimeStr(next, true)
            );
            tv_hp.setText(str);
        } else {

            tv_hp.setText(String.format(Locale.ENGLISH, hp_format_list[0], now, max));
        }
    }

    public ShipInfo getShipInfo() {
        return info;
    }

    private int getHPFormatId() {
        try {
            return Integer.parseInt(pref.getString(PREF_KCA_HP_FORMAT, "1"));
        } catch (Exception e) {
            return 1;
        }
    }

    // region static
    private static String makeLvString(int lv) {
        return KcaUtils.format("Lv %d", lv);
    }

    private static String makeExpString(int exp) {
        return KcaUtils.format("next: %d", exp);
    }

    private static String makeCondString(int cond) {
        return KcaUtils.format("%d", cond);
    }

    public static ShipInfo jsonToShipInfo(JsonObject data) {
        JsonObject userData = data.getAsJsonObject("user");
        JsonObject kcData = data.getAsJsonObject("kc");

        return new ShipInfo(
                userData.get("id").getAsInt(),
                kcData.get("name").getAsString(),
                kcData.get("stype").getAsInt(),
                userData.get("lv").getAsInt(),
                userData.getAsJsonArray("exp").get(1).getAsInt(),
                userData.get("nowhp").getAsInt(), userData.get("maxhp").getAsInt(),
                userData.get("cond").getAsInt(),
                userData.has("sally_area") ? userData.get("sally_area").getAsInt() : 0
        );
    }
    // endregion

    // region saved instance state
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        return new ShipInfoState(superState, info, isAkashiActive);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ShipInfoState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        ShipInfoState state0 = (ShipInfoState) state;
        super.onRestoreInstanceState(state0.getSuperState());

        isAkashiActive = state0.isAkashiActive;
        setData(state0.info);
    }

    public static final class ShipInfo implements Parcelable {
        final int ship_id;
        final String name;
        final int stype;
        final int lv, exp;
        final int now_hp, max_hp;
        final int cond;
        final int sally_area;

        public ShipInfo(
                int ship_id, String name, int stype,
                int lv, int exp,
                int now_hp, int max_hp, int cond,
                int sally_area
        ) {

            this.ship_id = ship_id;
            this.name = name;
            this.stype = stype;
            this.lv = lv;
            this.exp = exp;
            this.now_hp = now_hp;
            this.max_hp = max_hp;
            this.cond = cond;
            this.sally_area = sally_area;
        }

        public ShipInfo(Parcel source) {

            this.ship_id = source.readInt();
            this.name = source.readString();
            this.stype = source.readInt();
            this.lv = source.readInt();
            this.exp = source.readInt();
            this.now_hp = source.readInt();
            this.max_hp = source.readInt();
            this.cond = source.readInt();
            this.sally_area = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(ship_id);
            dest.writeString(name);
            dest.writeInt(stype);
            dest.writeInt(lv);
            dest.writeInt(exp);
            dest.writeInt(now_hp);
            dest.writeInt(max_hp);
            dest.writeInt(cond);
            dest.writeInt(sally_area);
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<ShipInfo> CREATOR = new Parcelable.Creator<ShipInfo>() {
            public ShipInfo createFromParcel(Parcel in) {
                return new ShipInfo(in);
            }

            public ShipInfo[] newArray(int size) {
                return new ShipInfo[size];
            }
        };
    }

    private static class ShipInfoState extends View.BaseSavedState {
        final ShipInfo info;
        final boolean isAkashiActive;

        public ShipInfoState(
                Parcelable superState,
                ShipInfo info,
                boolean isAkashiActive
        ) {
            super(superState);

            this.info = info;
            this.isAkashiActive = isAkashiActive;
        }

        public ShipInfoState(Parcel source) {
            super(source);

            info = new ShipInfo(source);
            isAkashiActive = source.readInt() == 1; // to avoid using read/writeBoolean
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            info.writeToParcel(out, flags);
            out.writeInt(isAkashiActive ? 1 : 0); // to avoid using read/writeBoolean
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<ShipInfoState> CREATOR = new Parcelable.Creator<ShipInfoState>() {
            public ShipInfoState createFromParcel(Parcel in) {
                return new ShipInfoState(in);
            }

            public ShipInfoState[] newArray(int size) {
                return new ShipInfoState[size];
            }
        };
    }
    // endregion
}
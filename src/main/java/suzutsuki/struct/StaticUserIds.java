package suzutsuki.struct;

import org.json.JSONObject;

public class StaticUserIds {
    public final String saya;
    public final String takase;
    public final String rattley;
    public final String alex;
    public final String yanga;
    public final String keita;

    public StaticUserIds(JSONObject data) {
        this.saya = data.getString("saya");
        this.takase = data.getString("takase");
        this.rattley = data.getString("rattley");
        this.alex = data.getString("alex");
        this.yanga = data.getString("yanga");
        this.keita = data.getString("keita");
    }
}

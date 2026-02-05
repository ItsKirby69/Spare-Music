package sparemusic.ui;

import arc.Core;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class SPSettings {
    public static void load() {
        ui.settings.addCategory("@setting.music-settings-title.title", "sparemusic-settings-icon", t -> {
            t.pref(new Title("@setting.music-toggles-title"));
            t.row();
            
            t.pref(new SubTitle("@setting.tooltip-erekir"));
            t.checkPref("@setting.music-enable-erekir-music", true);

            t.pref(new TableSetting("github-linko", new Table(c -> {
                c.button(Icon.github, new ImageButton.ImageButtonStyle(), () ->{
                    String url = "https://github.com/ItsKirby69/Spare-Music";
                    if(!Core.app.openURI(url)){
                        ui.showInfoFade("@linkfail");
                        Core.app.setClipboardText(url);
                    }
                }).size(50f).tooltip("@setting.tooltip-github").right().bottom();    
            })));
        });
    }

    public static class TableSetting extends Setting {
        public Table t;

        public TableSetting(String name, Table table){
            super(name);
            this.t = table;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(t).fillX().row();
        }
    }

    public static class Title extends Setting {
        public Title(String text){
            super("");
            this.title = text;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(title).color(Pal.accent).padTop(25f).padRight(110f).padBottom(-5).left().pad(5);
            table.row();
            table.image().color(Pal.accent).height(3f).padRight(110f).padBottom(25f).left().fillX().padBottom(5f);
            table.row();
        }
    }

    public static class SubTitle extends Setting {
        public SubTitle(String text){
            super("");
            this.title = text;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(title).color(Pal.gray).padTop(25f).padRight(110f).padBottom(-5).left().pad(5);
            table.row();
        }
    }
}

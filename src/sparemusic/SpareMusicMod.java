package sparemusic;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.mod.*;
import sparemusic.content.SPMusic;
import sparemusic.gen.*;
import sparemusic.ui.SPSettings;

import static arc.Core.bundle;

import arc.Events;

public class SpareMusicMod extends Mod{

	public SpareMusicMod() {
		Log.info("[gold]♪ ♫ ♬ [][brick]SpareMusic[] [gold]is loaded!");

    @Override
    public void loadContent(){
		Seq<String> subtitles = new Seq<>(bundle.get("subtitle.lines").split("/"));
		Vars.mods.list().each(mod -> {
			if(mod.main == this){
				mod.meta.subtitle = "[brick]" + subtitles.random();
			}
		});

        EntityRegistry.register();
    }
}

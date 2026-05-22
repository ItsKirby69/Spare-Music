package sparemusic;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import sparemusic.content.*;
import sparemusic.gen.*;
import sparemusic.ui.SPSettings;

import static arc.Core.*;

import arc.Events;

// TODO WHY DO MUSIC SUDDENLY NOT WORK??
public class SpareMusicMod extends Mod{

	public SPMusicManager musicManager;

	public SpareMusicMod() {
		Log.info("[gold]♪ ♫ ♬ [][#fff596]SpareMusic[] [gold]is loaded!");

        // Events.on(EventType.MusicRegisterEvent.class, e ->
        //     SPMusic.load()
        // );
	}

	@Override
	public void init() {
		SPSettings.load();

		Events.on(MusicRegisterEvent.class, e -> {
			SPMusicLoader.load();
			musicManager = new SPMusicManager(Vars.control.sound);
			musicManager.setVanilla();
		});

		Events.on(WorldLoadEvent.class, e -> {
			musicManager.onWorldLoad();
		});

		Events.run(Trigger.update, () -> {
			if(musicManager != null) musicManager.update();
		});

		Events.on(ClientLoadEvent.class, e -> {
			if(!SPMusicManager.debug) return;
			Log.info("[@] Loaded weathers: ", Vars.content.weathers().size);
			Vars.content.weathers().each(w -> Log.info(" - @ (@)", w.name, w.minfo.mod == null ? "vanilla" : w.minfo.mod.name));
		});
	}

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

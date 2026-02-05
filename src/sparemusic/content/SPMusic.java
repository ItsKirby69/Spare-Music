package sparemusic.content;

import static arc.Core.settings;
import static mindustry.Vars.*;

import arc.Events;
import arc.audio.Music;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*; 
import mindustry.Vars;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Teams.TeamData;
import mindustry.type.*;
import mindustry.type.Weather.WeatherEntry;

// TODO, make erekir music sets for each type (ambient, dark, boss)
// TODO, make vanilla grimdark set
// TODO, make serpulo weather music set (Snowing, raining)
// TODO, make erekir weather music set

/**
 * Holds all music logics for custom music. Greatly referenced from Omaloon's mod in OIMusics class.
 */
public class SPMusic{
    private static final ObjectMap<String, Seq<Music>> musicSets = new ObjectMap<>();
    private static ObjectMap<Weather, Seq<Music>> weatherMusicSets = new ObjectMap<>();

    public static Music
    /** Theia Specific Music */
    theia1, theia2, // Ambient music
    theiaDark1, theiaDark2, // Dark music
    theiaBoss1, theiaBoss2, // Boss music

    /** Erekir Specific Music */
    erekir1, erekir2, // Ambient music
    erekirDark1, erekirDark2, // Dark music
    erekirBoss1, erekirBoss2, // Boss music

    /** Additional music for vanilla */
    fine2, ambient1, ambient2, // Ambient music
    grim1, grim2, // Grim dark music

    // Weather specific music
    rain1, rain2;

    public static void load(){
        initMusics();
        initMusicSets();
        setupEventHandlers();
    }

    static void initMusics(){
        // Single musics
        // String[] ambientTracks = {"ambient1", "ambient2"};
        String[] rainTracks = {"rain1", "rain2"};
        String[] grimTracks = {"grim1", "grim2"};

        // Les exemples
        String[] erekirTracks = {"erekir1", "erekir2"};
        String[] erekirDarkTracks = {"erekirDark1", "erekirDark2"};
        String[] erekirBossTracks = {"erekirBoss1", "erekirBoss2"};

        String[] theiaTracks = {"theia", "theia"};
        String[] theiaDarkTracks = {"theiaDark1", "theiaDark2"};
        String[] theiaBossTracks = {"theiaBoss1", "theiaBoss2"};

        loadMusicSet("grim/", grimTracks);
        loadMusicSet("", rainTracks);

        // loadMusicSet("erekir/", erekirTracks);
        // loadMusicSet("erekir/", erekirDarkTracks);
        // loadMusicSet("erekir/", erekirBossTracks);

        // loadMusicSet("theia/", theiaTracks);
        // loadMusicSet("theia/", theiaDarkTracks);
        // loadMusicSet("theia/", theiaBossTracks);
    }

    /** Sets up event handlers for updating music based on game events. */
    private static void setupEventHandlers(){
        Events.run(EventType.Trigger.update, SPMusic::specialMusics);
        Events.on(WorldLoadEvent.class, e -> {
            // updateLandMusic();
            updatePlanetMusic();
        });
    }

    /** Custom Weather specific music as well as GrimDark music conditions. Otherwise reset tracks (based on planet ofc) */
    private static void specialMusics(){
        if(!Vars.state.isGame() || Vars.state.boss() != null) return;
        String prefix = getPlanetPrefix();

        // Plays weather specific music (priority over Grim music)
        Seq<Music> weather = currentWeatherMusic();
        if(weather != null){
            Vars.control.sound.ambientMusic.set(weather);
            Vars.control.sound.darkMusic.set(weather);
            return;
        }

        // Plays Grimdark music (priority over Dark music)
        if(isGrim()){
            setMusicSet(prefix + "Ambient", Vars.control.sound.ambientMusic);
            setMusicSet("grimDark", Vars.control.sound.darkMusic);
            return;
        }

        setMusicSet(prefix + "Ambient", Vars.control.sound.ambientMusic);
        setMusicSet(prefix + "Dark", Vars.control.sound.darkMusic);

    }

    /** Places music in musicSets, all of it */
    public static void initMusicSets(){
        musicSets.put("vanillaAmbient", new Seq<>(Vars.control.sound.ambientMusic));
        musicSets.put("vanillaDark", new Seq<>(Vars.control.sound.darkMusic));
        musicSets.put("vanillaBoss", new Seq<>(Vars.control.sound.bossMusic));

        // WIP: Erekir sets
        // musicSets.put("erekirAmbient", Seq.with(erekir1, erekir2));
        // musicSets.put("erekirDark", Seq.with(erekirDark1, erekirDark2));
        // musicSets.put("erekirBoss", Seq.with(erekirBoss1, erekirBoss2));

        // Theia specific music
        // musicSets.put("theiaAmbient", Seq.with(theia1, theia2));
        // musicSets.put("theiaDark", Seq.with(theiaDark1, theiaDark2));
        // musicSets.put("theiaBoss", Seq.with(theiaBoss1, theiaBoss2));

        // Then comes custom music
        musicSets.put("grimDark", Seq.with(grim1, grim2));

        // Weather specific music
        addWeatherMusic(Weathers.rain.name, Seq.with(rain1, rain2));
        addWeatherMusic("minedusty-heavy-rain", Seq.with(rain1, rain2));
        // weatherMusicSets.put(Weathers.sandstorm, Seq.with(sand1));
    }

    /** Update Ambient, Dark, and Boss music sets based on planet */
    private static void updatePlanetMusic(){
        String prefix = getPlanetPrefix();
        boolean minedustyTheia= prefix == "theia";

        // if(minedustyTheia){
        //     // Log.info("Loading theia specific music!");
        //     // setMusicSet("theiaAmbient", Vars.control.sound.ambientMusic);
        //     // setMusicSet("theiaDark", Vars.control.sound.darkMusic);
        //     // setMusicSet("theiaBoss", Vars.control.sound.bossMusic);
        //     return;
        // }

        if(Vars.state.rules.planet != Planets.sun){
            setMusicSet(prefix + "Ambient", Vars.control.sound.ambientMusic);
            setMusicSet(prefix + "Dark", Vars.control.sound.darkMusic);
            setMusicSet(prefix + "Boss", Vars.control.sound.bossMusic);
        }else{
            mixMusic();
        }
    }

    /** Gets prefix based on planet */
    private static final String getPlanetPrefix(){
        Planet plant = Vars.state.rules.planet;
        // If MineDusty is loaded + planet is Theia
        if(Vars.mods.locateMod("minedusty") != null && plant.name.equals("minedusty-theia")){ return "theia"; }
        if(!settings.getBool("@setting.music-enable-erekir-music")){ return "vanilla";}
        return plant == Planets.erekir ? "erekir" : "vanilla";
    }

    /** Whether to play grimdark music */
    static boolean isGrim(){
        var data = player.team().data();
        if (data.hasCore() && data.core().healthf() < 0.5f){
            return true;
        }

        return Mathf.chance((float)(Math.log10((state.wave - 30f)/10f) + 1) / 3f);
    } 

    @Nullable static Seq<Music> currentWeatherMusic(){
        if(Vars.state.rules.weather.isEmpty()) return null;
        
        for(WeatherEntry entry : Vars.state.rules.weather){
            if(entry.weather.isActive() && weatherMusicSets.containsKey(entry.weather)){
                return weatherMusicSets.get(entry.weather);
            }
        }
        return null;
    }

    private static void addWeatherMusic(String weatherName, Seq<Music> music){
        Weather w = content.weathers().find(e -> e.name.equals(weatherName));
        if(w != null){
            weatherMusicSets.put(w, music);
        }
    }

    /** Mixes vanilla and erekir music sets. */
    private static void mixMusic(){
        // Disables erekir music
        if(!settings.getBool("@setting.music-enable-erekir-music")){
            setMusicSet("vanillaAmbient", Vars.control.sound.ambientMusic);
            setMusicSet("vanillaDark", Vars.control.sound.darkMusic);
            setMusicSet("vanillaBoss", Vars.control.sound.bossMusic);
            return;
        }
        mixMusicSets("vanillaAmbient", "erekirAmbient", Vars.control.sound.ambientMusic);
        mixMusicSets("vanillaDark", "erekirDark", Vars.control.sound.darkMusic);
        mixMusicSets("vanillaBoss", "erekirBoss", Vars.control.sound.bossMusic);
    }

    /**
     * Mixes two music sets and assigns the result to a target set.
     * @param target Target sequence to store the mixed music.
     */
    private static void mixMusicSets(String vanillaSetName, String modSetName, Seq<Music> target){
        Seq<Music> vanillaSet = musicSets.get(vanillaSetName);
        Seq<Music> modSet = musicSets.get(modSetName);
        if(vanillaSet != null && modSet != null){
            target.clear();
            target.addAll(vanillaSet);
            target.addAll(modSet);
        }
    }
    /**
     * Loads a set of music tracks from a specified base path.
     * @param basePath Base path for the music files.
     * @param trackNames Array of track names to load.
     */
    private static void loadMusicSet(String basePath, String[] trackNames){
        for(String track : trackNames){
            try{
                Music music = loadMusic(basePath + track);
                SPMusic.class.getField(track).set(null, music);
            }catch(Exception e){
                Log.err("Failed to load music: " + track, e);
            }
        }
    }

    /**
     * Sets a music set to a target sequence.
     * @param setName Name of the music set to use.
     * @param target Target sequence to update.
     */
    private static void setMusicSet(String setName, Seq<Music> target){
        Seq<Music> set = musicSets.get(setName);
        if(set != null){
            target.set(set);
        }
    }

    /** Loads music file from game asset. */
    private static Music loadMusic(String name){
        return Vars.tree.loadMusic(name);
    }
    
}

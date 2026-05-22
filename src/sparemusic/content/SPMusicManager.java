package sparemusic.content;

import static arc.Core.settings;
import static mindustry.Vars.*;

import arc.audio.Music;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.audio.SoundControl;
import mindustry.type.Planet;
import mindustry.type.Weather.WeatherEntry;

public class SPMusicManager {
    
    private SoundControl sound;
    public static boolean debug = true;

    // Again, heavily inspired by Erekir-Music-v8 by TeamOct
    private Seq<Music> vAmbient, vDark, vBoss;

    public SPMusicManager(SoundControl sound){
        this.sound = sound;
    }

    /** Get copy of vanilla music sets */
    public void setVanilla(){
        vAmbient = sound.ambientMusic.copy();
        vDark = sound.darkMusic.copy();
        vBoss = sound.bossMusic.copy();
    }

    /** Switch music sets when the world loads. */
    public void onWorldLoad(){
        applyBaseSets(currentPlanet());
        if(debug) logSets();
    }

    /** Debug logs for all sets when landing on a sector. */
    private void logSets(){
        String planet = currentPlanet();
        Log.info("= Music for [gold]@[] =", planet);
        logSet("ambient", resolve(planet, "ambient"));
        logSet("dark", resolve(planet, "dark"));
        logSet("boss", resolve(planet, "boss"));

        Seq<Music> grim = resolveOther(planet, "grimdark");
        if(grim != null){
            logSet("grimdark", grim);
        }else{
            Log.info("  grimdark -> none");
        }

        Log.info("Weathers:");
        SPMusicLoader.allSets.each((modname, modSets) -> {
            modSets.each((key, tracks) -> {
                if(key.startsWith(planet + "/weathers/") || key.startsWith("global/weathers/")){
                    logSet(key, tracks);
                }
            });
        });

        Log.info("Weather groups:");
        MusicConfig.weatherGroups.each((group, weathers) -> 
            Log.info("  @ -> @", group, weathers.toString(", "))
        );
    }

    private void logSet(String name, Seq<Music> set){
        Log.info("  @ -> @ tracks", name, set.size);
        set.each(m -> Log.info("    - @", m.toString()));
    }

    /** 
     * Updates the hardcoded music sets with given conditions and injections.
     * Resolves each music set by loading the planet specific of the music set
     * plus global music set.
     */
    public void update(){
        if(!state.isGame()) return;
        
        String planet = currentPlanet();

        // Plays weather specific music (priority over Grim music)
        Seq<Music> weatherSet = resolveWeatherMusic(planet);
        if(weatherSet != null){
            sound.ambientMusic.set(weatherSet);
            sound.darkMusic.set(weatherSet);
            return;
        }

        // Plays Grimdark music (priority over Dark music)
        if(state.boss() == null && isGrim()){
            Seq<Music> grim = resolveOther(planet, "grimdark");
            if(grim !=null){
                sound.darkMusic.set(grim);
                return;
            }
        }

        /** Applies the base music sets (ambient dark boss) */
        applyBaseSets(planet);
    }

    private void applyBaseSets(String planet){
        sound.ambientMusic.set(resolve(planet, "ambient"));
        sound.darkMusic.set(resolve(planet, "dark"));
        sound.bossMusic.set(resolve(planet, "boss"));
    }

    /** 
     * Resolves base music sets with descending cases.
     * First checks for custom planet music, then from the global list, then vanilla stuff.
     */
    private Seq<Music> resolve(String planet, String cat){
        // Mix all planet-specific if enabled
        if(settings.getBool("interplanetary-music")) return resolveInterplanetary(cat);

        Seq<Music> result = new Seq<>();

        // Planet-Specific
        if(settings.getBool("planet-music")){
            result.addAll(SPMusicLoader.get(planet + "/" + cat));
        }

        // Global
        if(settings.getBool("global-music")){
            result.addAll(SPMusicLoader.get("global/" + cat));
        }

        // Vanilla fallback
        if(settings.getBool("vanilla-music") || !result.any()){
            result.addAll(vanillaSet(cat));
        }

        return result;
    }

    /** Resolves all music for given category. From all planets, global list and vanilla sets. */
    private Seq<Music> resolveInterplanetary(String cat){
        Seq<Music> result = new Seq<>();

        SPMusicLoader.allSets.each((modname, modSets) -> {
            modSets.each((key, tracks) -> {
                // Add anything by category except for global list
                if(key.endsWith("/" + cat) && !key.startsWith("global/")){
                    result.addAll(tracks);
                }
            });
        });

        if(settings.getBool("global-music")){
            result.addAll(SPMusicLoader.get("global/" + cat));
        }

        if(settings.getBool("vanilla-music") || !result.any()){
            result.addAll(vanillaSet(cat));
        }

        return result;
    }

    /** Resolve for other music sets. Made for grimDark */
    private Seq<Music> resolveOther(String planet, String cat){
        Seq<Music> result = new Seq<>();

        // Mix all planet-specific if enabled
        if(settings.getBool("planet-music")){
            if(settings.getBool("interplanetary-music")){
                SPMusicLoader.allSets.each((modname, modSets) -> {
                    modSets.each((key, tracks) -> {
                        // Add anything by category except for global list
                        if(key.endsWith("/" + cat) && !key.startsWith("global/")){
                            result.addAll(tracks);
                        }
                    });
                });
            }else{
                result.addAll(SPMusicLoader.get(planet + "/" + cat));
            }
        }

        if(settings.getBool("global-music")){
            result.addAll(SPMusicLoader.get("global/" + cat));
        }

        // If nothing is found, naturally play default sets (dark/ambient)
        return result.any() ? result : null; 
    }

    private Seq<Music> resolveWeatherMusic(String planet){
        if(!settings.getBool("weather-music")) return null;
        if(state.rules.weather == null) return null;

        Seq<Music> result = new Seq<>();

        for(WeatherEntry entry : state.rules.weather){
            if((entry.weather == null) || !entry.weather.isActive()) continue;
            String weatherName = entry.weather.name;

            Seq<String> candidates = new Seq<>();
            candidates.add(weatherName);
            candidates.addAll(weatherGroup(weatherName));
            candidates.add("all");

            for(String candidate : candidates){
                // Interplanetary mode: check all planets
                if(settings.getBool("interplanetary-music")){
                    SPMusicLoader.allSets.each((modname, modSets) -> {
                        modSets.each((key, tracks) -> {
                            // Add anything by candidate weather except for global list
                            if(key.endsWith("/weathers/" + candidate) && !key.startsWith("global/")){
                                result.addAll(tracks);
                            }
                        });
                    });
                }
                
                if(settings.getBool("planet-music")){
                    result.addAll(SPMusicLoader.get(planet + "/weathers/" + candidate));
                }

                if(settings.getBool("global-music")){
                    result.addAll(SPMusicLoader.get("global/weathers/" + candidate));
                }
            }
        }

        return result.any() ? result : null; 
    }

    /** Returns all weather groups containing the given weather. */
    private Seq<String> weatherGroup(String weatherName){
        Seq<String> result = new Seq<>();
        MusicConfig.weatherGroups.each((group, weathers) -> {
            if(weathers.contains(weatherName)) result.add(group);
        });
        return result;
    }

    private Seq<Music> vanillaSet(String cat){
        return switch (cat){
            case "ambient" -> vAmbient;
            case "dark" -> vDark;
            case "boss" -> vBoss;
            default -> new Seq<>();
        };
    }

    /** Get the current planet's full id */
    private String currentPlanet(){
        Planet planet = state.rules.planet;
        if(planet == null) return "serpulo";
        return planet.name;
    }

    /** 
     * Whether to play grimdark music.
     * Plays when player core below 75% and after wave 15 chance multiplied by number of 
     * enemy spawns.
    */
    static boolean isGrim(){
        var data = player.team().data();
        if (data.hasCore() && data.core().healthf() < 0.75f){
            return true;
        }

        if(Mathf.chance(Vars.spawner.countSpawns() * (float)(Math.log10((state.wave - 15f)/23f) + 1) / 5f)){
            return true;
        }

        return false;
    }
}

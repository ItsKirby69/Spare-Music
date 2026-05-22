package sparemusic.content;

import static mindustry.Vars.mods;

import arc.audio.Music;
import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.mod.Mods.LoadedMod;

public class SPMusicLoader {

    /**
     * Loaded music sets. This is formatted in the files like:
        music/
        ├── <planet>/
        │   ├── ambient/
        │   ├── dark/
        │   ├── boss/
        │   ├── grimdark/
        │   └── weathers/
        │       ├── rain/
        │       └── all/
        └── global/
            ├── ambient/
            └── etc
     * The top directory should either be a sepcific planet or global for music playing everywhere
     * The sub directory should be the sets, weathers having sub-sets for specific weather, all weather, or custom weather tags (like wet)
     */
    public static final ObjectMap<String, ObjectMap<String, Seq<Music>>> allSets = new ObjectMap<>();

    // Heavily inspired from Erekir-Music-v8 by TeamOct
    public static void load(){
        allSets.clear();

        for(LoadedMod mod : Vars.mods.orderedMods()){
            // Fi musicDir = Vars.tree.get("assets/music");
            Fi musicDir = mod.root.child("music");
            if(!musicDir.exists()) continue;
            // Fi musicRoot = (Vars.mods.locateMod("sparemusic")).root.child("music"); // Original

            // Ref. Thank you smolkeys
            //new Music(Vars.mods.getMod("sparemusic").root.child("music").child("global").child("grimdark").child("grim2.ogg")).play();

            Log.info("Loading music from mod: @ (@)", mod.name, musicDir.absolutePath());
            ObjectMap<String, Seq<Music>> modSets = new ObjectMap<>();

            for(Fi planet : musicDir.list()){
                if(!planet.isDirectory()) continue;
                for(Fi cat : planet.list()){
                    if(!cat.isDirectory()) continue;
                    if(cat.name().equals("weathers")){
                        for(Fi weather : cat.list()){
                            if(!weather.isDirectory()) continue;

                            String key = planet.name() + "/weathers/" + weather.name();
                            modSets.put(key, loadTracks(musicDir, key));
                        }
                    }else{
                        String key = planet.name() + "/" + cat.name();
                        modSets.put(key, loadTracks(musicDir, key));
                    }
                }
            }

            allSets.put(mod.name, modSets);

            if(mod.name.equals("sparemusic")){
                addConfigs(modSets);
            }
        }
    }

    /** 
     * Adds music in MusicConfig to the sets directly. 
     * As in music that would be included in other sets listed there.
    */
    // TODO make this more accessible for other mods?
    private static void addConfigs(ObjectMap<String, Seq<Music>> sets){
        MusicConfig.includes.each((target, musics) -> {
            Seq<Music> targetSet = sets.get(target, new Seq<>());
            for(String musicPath : musics){
                try{
                    Music music = loadMusic(musicPath);
                    if(music != null){
                        targetSet.add(music);
                    }else{
                        Log.warn("Null music in includes (wrong path?) @", musicPath);
                    }
                }catch(Exception e){
                    Log.warn("Source set not found: @", musicPath);
                }
            }
            sets.put(target, targetSet);
        });
    }

    /**
     * Loads all music under a given subdirectory of a mod music set.
     */
    private static Seq<Music> loadTracks(Fi musicDir, String diskPath){
        Seq<Music> result = new Seq<>();
        Fi dirPath = musicDir;
        for(String seg : diskPath.split("/")){
            dirPath = dirPath.child(seg);
        }
        if(!dirPath.exists()){
            Log.warn("Directory for music doesn't exist: @", dirPath.absolutePath());
            return result;
        }

        for(Fi file : dirPath.list()){
            if(!file.extEquals("ogg") && !file.extEquals("mp3")) continue;
            // Music music = loadMusic(diskPath + "/" + file.nameWithoutExtension());
            try{
                result.add(new Music(file));
            }catch(Exception e){
                Log.warn("Failed to load '@': @", file.path(), e.getMessage());
            }
        }
        return result;
    }

    private static Music loadMusic(String name){
        return Vars.tree.loadMusic(name);
    }

    /** Returns tracks given key across all mods. */
    public static Seq<Music> get(String key){
        Seq<Music> combined = new Seq<>();
        for(ObjectMap<String, Seq<Music>> modSets : allSets.values()){
            Seq<Music> s = modSets.get(key);
            if(s != null) combined.addAll(s);
        }
        return combined;
    }

    /** Returns tracks given key from a specific mod. */
    public static Seq<Music> get(String modname, String key){
        ObjectMap<String, Seq<Music>> modSets = allSets.get(modname);
        if(modSets == null) return new Seq<>();
        return modSets.get(key, new Seq<>());
    }

    /** Checks if any mod has at least one track given key. */
    public static boolean has(String key){
        for(ObjectMap<String, Seq<Music>> modSets : allSets.values()){
            Seq<Music> s = modSets.get(key);
            if(s != null && s.any()) return true;
        }
        return false;
    }

    /** Checks if specific mod has at least one track given key. */
    public static boolean has(String modname, String key){
        ObjectMap<String, Seq<Music>> modSets = allSets.get(modname);
        if(modSets == null) return false;
        Seq<Music> s = modSets.get(key);
        return s != null && s.any();
    }
}

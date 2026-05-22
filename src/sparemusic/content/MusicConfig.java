package sparemusic.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;

/** Configuration for music groups to allow music to appear in several sets */
public class MusicConfig {

    /**
     * Create weather groups for music to appear when any of the weathers are playing. 
     * Below is an example having music placed in a folder named "breezy" in the weathers folder
     * which will allow that music to appear when either weather plays.
     * Format: "breezy", Seq.with("minedusty-clouds", "rain")
     */
    public static final ObjectMap<String, Seq<String>> weatherGroups = ObjectMap.of(
        "wet", Seq.with("rain", "minedusty-heavy-rain")
    );

    /**
     * Put already existing tracks to other sets (folders)
     * Below is an example of placing music1 (no extension) from
     * serpulo's ambient music set into erekir's dark music set
     * Format: "erekir/dark/", Seq.with("serpulo/ambient/music1")
     */
    public static final ObjectMap<String, Seq<String>> includes = ObjectMap.of(
        
    );
}

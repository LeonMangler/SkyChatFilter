# SkyChatFilter
Lightweight Minecraft-Bungeecord chat filter to guard against spam, swearing, caps and advertising

There are many chat message filtering solutions, however most of them are either easily bypassed or have lots of false positives. SkyChatFilter combines multiple techniques using unconventional algorithms which all work together to create an amazingly powerful chat filter with little annoyance for players and staff.
Most significant features:
* Meticulous repetition detection. This uses string-similarity and message memorization to make sure that similar chat messages cannot be repeated by the same player in a certain period of time.
* Confirmation-driven advertising detection. Messages that are detected as advertising using regular expressions will be put on hold to be confirmed by a staff member.
* Flood protection. Players can't send too many chat messages in a short period of time. The threshold can be lower for messages sent directly after joining the server.
* Caps filter. Messages can be adjusted or cancelled if the percentage of uppercase letters is too big. Short messages can be excluded.
* Swearword filter. Messages are checked against a customizable list of swearwords.
* Full customizability. All thresholds and features can be changed or turned off. All messages are customizable and can be different depending on the player's server. There are exempt permissions for every check.

More documentation is <a href="https://www.spigotmc.org/resources/skychatfilter.65501/">here</a>.

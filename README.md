# MineNorth VIP (Forge 1.20.1)

Grades VIP attribués **par commande** (il n'y a pas de commande `/vip`), avec préfixe devant le nom et avantages lus par les autres mods MineNorth.

## Commandes (OP, niveau 2)
- `/grade donner <joueur> <grade> [jours]` : sans durée = permanent.
- `/grade retirer <joueur>`
- `/grade info [joueur]`
- `/grade liste` : grades de la config et joueurs gradés.
- `/grade recharger` : relit `config/minenorth_vip.json`.

## Configuration `config/minenorth_vip.json`
Chaque grade : `id`, `nom`, `prefixe` (codes `§` acceptés, affiché dans le chat et la liste des joueurs), `avantages` (nom -> valeur).
Par défaut : `vip` (`casinoMax` x2) et `vip_plus` (`casinoMax` x5).

## Liens avec les autres mods
- **API** : `MineNorth.grades()` (`GradeService`) : `grade(server, uuid)` et `perk(server, uuid, "nom", valeurParDéfaut)`. Sans ce mod, tout reste à la valeur par défaut.
- **Casino** : avantage `casinoMax` = multiplicateur des mises maximales (machine à sous, roulette, blackjack), appliqué côté serveur et affiché dans l'interface.
- D'autres mods peuvent lire leurs propres avantages (remises en boutique, etc.) avec `perk(...)`.

Les grades expirés sont retirés automatiquement. Données sauvegardées dans le monde (`minenorth_vip`).

## Compiler
`gradlew build` (builder `minenorth-api` d'abord). Le jar est dans `build/libs/`.

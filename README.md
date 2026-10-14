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
- **Remises** : avantage `remise` (en %) appliqué sur les achats en boutique (`minenorth_shops`), les véhicules et l'assurance (garage) et les permis / duplicatas. Par défaut : VIP -10 %, VIP+ -20 %. Le prix affiché dans les catalogues reste le prix normal, la remise est appliquée au paiement.
- **Casino VIP** : avantage `casinoVip` = 1 donne accès à la **machine à sous VIP** (dorée, un 4e modèle ; les 3 autres machines restent libres d'accès).
- **Imprimante dorée** (`minenorth_printer:printer_gold`) : avantage `imprimanteOr` = 1 (VIP+ par défaut) pour la poser et l'ouvrir ; elle imprime 2x plus vite et 1,5x plus de billets. Si le propriétaire perd son grade, elle s'arrête.
- D'autres mods peuvent lire leurs propres avantages (remises en boutique, etc.) avec `perk(...)`.

Les grades expirés sont retirés automatiquement. Données sauvegardées dans le monde (`minenorth_vip`).

## Compiler
`gradlew build` (builder `minenorth-api` d'abord). Le jar est dans `build/libs/`.
